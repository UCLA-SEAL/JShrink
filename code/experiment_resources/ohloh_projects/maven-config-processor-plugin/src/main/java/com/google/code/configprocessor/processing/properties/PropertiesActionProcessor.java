/*
 * Copyright (C) 2009 Leandro de Oliveira Aparecido <lehphyro@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.code.configprocessor.processing.properties;

import java.io.*;
import java.util.*;

import com.google.code.configprocessor.*;
import com.google.code.configprocessor.expression.*;
import com.google.code.configprocessor.io.*;
import com.google.code.configprocessor.processing.*;
import com.google.code.configprocessor.processing.properties.model.*;
import com.google.code.configprocessor.util.*;

public class PropertiesActionProcessor implements ActionProcessor {

	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final int READ_AHEAD_BUFFER_SIZE = 1024 * 10;

	private String encoding;
	private FileResolver fileResolver;
	private ExpressionResolver expressionResolver;

	private Set<File> appendedFiles;

	public PropertiesActionProcessor(String encoding, FileResolver fileResolver, ExpressionResolver expressionResolver) {
		this.encoding = encoding;
		this.fileResolver = fileResolver;
		this.expressionResolver = expressionResolver;
		this.appendedFiles = new HashSet<File>();
	}

	public void process(Reader input, Writer output, Action action) throws ParsingException, IOException {
		BufferedReader reader = new BufferedReader(input);
		BufferedWriter writer = new BufferedWriter(output);
		process(reader, writer, action);
	}
	
	protected void process(BufferedReader reader, BufferedWriter writer, Action action) throws ParsingException, IOException {
		PropertiesActionProcessingAdvisor advisor = getAdvisorFor(action);

		// Start
		PropertiesFileItemAdvice advice = advisor.onStartProcessing();
		processAdvice(advice, null, writer, action);

		// Process
		PropertiesFileItem currentItem = null;
		String line;
		while ((line = reader.readLine()) != null) {
			if (isBlankLine(line)) {
				writer.append(LINE_SEPARATOR);
			} else {
				if (isComment(line)) {
					currentItem = readComment(reader, line);
				} else {
					currentItem = readPropertyMapping(reader, line);
				}

				advice = advisor.process(currentItem);
				processAdvice(advice, currentItem, writer, action);
			}
		}

		// End
		advice = advisor.onEndProcessing();
		processAdvice(advice, null, writer, action);

		writer.flush();
	}

	protected Comment readComment(BufferedReader reader, String line) throws IOException {
		if (line == null) {
			return new Comment("");
		}

		boolean shouldContinue = true;
		StringBuilder sb = new StringBuilder(line);

		String aux = line;
		while (shouldContinue && (aux != null) && aux.endsWith(PropertyMapping.PROPERTY_VALUE_LINE_SEPARATOR)) {
			reader.mark(READ_AHEAD_BUFFER_SIZE);
			aux = reader.readLine();

			if (aux == null) {
				shouldContinue = false;
			} else {
				if (isComment(aux)) {
					sb.append(LINE_SEPARATOR);
					sb.append(aux);
				} else {
					shouldContinue = false;
					reader.reset();
				}
			}
		}

		return new Comment(sb.toString());
	}

	protected PropertyMapping readPropertyMapping(BufferedReader reader, String line) throws IOException {
		StringBuilder sb = new StringBuilder(line);
		String aux = line;
		while (aux.endsWith(PropertyMapping.PROPERTY_VALUE_LINE_SEPARATOR)) {
			aux = reader.readLine();
			if (aux == null) {
				break;
			}
			sb.append(LINE_SEPARATOR);
			sb.append(aux);
		}

		PropertyMapping propertyMapping = new PropertyMapping();
		propertyMapping.parse(sb.toString(), false);

		return propertyMapping;
	}

	protected void processAdvice(PropertiesFileItemAdvice advice, PropertiesFileItem currentItem, BufferedWriter writer, Action action) throws ParsingException, IOException {
		switch (advice.getType()) {
			case DO_NOTHING:
				append(currentItem, writer);
				break;
			case REMOVE:
				break;
			case MODIFY:
				append(advice.getItem(), writer);
				break;
			case ADD_AFTER:
				append(currentItem, writer);
				append(advice.getItem(), writer);
				break;
			case ADD_BEFORE:
				append(advice.getItem(), writer);
				append(currentItem, writer);
				break;
			case APPEND_FILE_AFTER:
				append(currentItem, writer);
				appendFile(advice.getItem(), writer, action);
				break;
			case APPEND_FILE_BEFORE:
				appendFile(advice.getItem(), writer, action);
				append(currentItem, writer);
				break;
			default:
				throw new IllegalArgumentException("Unknown advice type: " + advice.getType());
		}
	}

	protected void append(PropertiesFileItem item, BufferedWriter writer) throws IOException {
		if (item != null) {
			writer.append(item.getAsText());
			writer.append(LINE_SEPARATOR);
		}
	}
	
	protected void appendFile(PropertiesFileItem item, BufferedWriter writer, Action action) throws ParsingException, IOException {
		FilePropertiesFileItem aux = (FilePropertiesFileItem)item;
		File file = fileResolver.resolve(aux.getFile());
		if (appendedFiles.add(file)) { // Prevent adding the same file twice
			InputStreamReader reader = new InputStreamReader(new FileInputStream(file), encoding);
			try {
				process(reader, writer, action);
			} finally {
				IOUtils.close(reader, null);
			}
		}
	}

	protected PropertiesActionProcessingAdvisor getAdvisorFor(Action action) {
		if (action instanceof AddAction) {
			return new PropertiesAddActionProcessingAdvisor((AddAction) action, expressionResolver);
		} else if (action instanceof ModifyAction) {
			return new PropertiesModifyActionProcessingAdvisor((ModifyAction) action, expressionResolver);
		} else if (action instanceof RemoveAction) {
			return new PropertiesRemoveActionProcessingAdvisor((RemoveAction) action, expressionResolver);
		} else if (action instanceof CommentAction) {
			return new PropertiesCommentActionProcessingAdvisor((CommentAction) action, expressionResolver);
		} else if (action instanceof UncommentAction) {
			return new PropertiesUncommentActionProcessingAdvisor((UncommentAction) action, expressionResolver);
		} else if (action instanceof NestedAction) {
			List<PropertiesActionProcessingAdvisor> advisors = new ArrayList<PropertiesActionProcessingAdvisor>();
			NestedAction nestedAction = (NestedAction) action;
			for (Action nested : nestedAction.getActions()) {
				advisors.add(getAdvisorFor(nested));
			}
			return new NestedPropertiesActionProcessingAdvisor(advisors);
		}
		throw new IllegalArgumentException("Unknown action: " + action);
	}

	protected boolean isBlankLine(String line) {
		return line.trim().length() == 0;
	}

	protected boolean isComment(String line) {
		String trimmedLine = line.trim();
		return trimmedLine.startsWith(Comment.PREFIX_1) || trimmedLine.startsWith(Comment.PREFIX_2);
	}

}
