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
package com.google.code.configprocessor;

import static com.google.code.configprocessor.util.IOUtils.*;
import static org.apache.commons.lang.StringUtils.*;

import java.io.*;
import java.util.*;

import org.apache.commons.lang.*;
import org.apache.tools.ant.*;

import com.google.code.configprocessor.expression.*;
import com.google.code.configprocessor.io.*;
import com.google.code.configprocessor.log.*;
import com.google.code.configprocessor.maven.*;
import com.google.code.configprocessor.parsing.*;
import com.google.code.configprocessor.processing.*;
import com.google.code.configprocessor.processing.properties.*;
import com.google.code.configprocessor.processing.xml.*;

public class ConfigProcessor {

	private static final String DEFAULT_ENCODING = "UTF-8";

	private String encoding;
	private int lineWidth;
	private int indentSize;
	private Map<String, String> namespaceContexts;
	private boolean useOutputDirectory;
	private File baseDir;
	private File outputDirectory;
	private LogAdapter log;
	private FileResolver fileResolver;
	private List<ParserFeature> parserFeatures;
	private boolean failOnMissingXpath;

	private File actualOutputDirectory;

	public ConfigProcessor(String encoding,
						   int indentSize,
						   int lineWidth,
						   Map<String, String> namespaceContexts,
						   File baseDir,
						   File outputDirectory,
						   boolean useOutputDirectory,
						   LogAdapter log,
						   FileResolver fileResolver,
						   List<ParserFeature> parserFeatures,
						   boolean failOnMissingXpath) {
		this.encoding = encoding;
		this.indentSize = indentSize;
		this.lineWidth = lineWidth;
		this.namespaceContexts = namespaceContexts;
		this.baseDir = baseDir;
		this.outputDirectory = outputDirectory;
		this.useOutputDirectory = useOutputDirectory;
		this.log = log;
		this.fileResolver = fileResolver;
		this.parserFeatures = parserFeatures;
		this.failOnMissingXpath = failOnMissingXpath;
	}

	public void init() throws IOException {
		if (useOutputDirectory) {
			if (!outputDirectory.exists()) {
				if (!outputDirectory.mkdirs()) {
					throw new IOException("Could not create outputDirectory [" + outputDirectory + "]");
				}
			}
			actualOutputDirectory = outputDirectory;
		}
		if (encoding == null) {
			getLog().warn("Encoding has not been set, using default [" + DEFAULT_ENCODING + "].");
			encoding = DEFAULT_ENCODING;
		}

		getLog().debug("Using output directory [" + actualOutputDirectory + "]");
		getLog().debug("File encodig is [" + encoding + "]");
	}

	public void execute(ExpressionResolver resolver, Transformation transformation) throws ConfigProcessorException, IOException {
		String input = transformation.getInput();
		Action action = getAction(transformation);
		String configIdentifier = getConfigIdentifier(transformation);

		if (input != null && input.contains("*")) {
			// input parameter specifies a wildcard pattern
			if (!StringUtils.isBlank(transformation.getOutput())) {
				throw new ConfigProcessorException("Cannot specify output file if wildcard pattern based input is given");
			}
			getLog().info("Using wildcard pattern based input [" + input + "]");
			List<File> inputFiles = getMatchingFiles(input);
			for (File inputFile : inputFiles) {
				String type = getInputType(transformation, inputFile);
				File outputFile;
				if (actualOutputDirectory != null) {
					// calculate a relative path below the output directory based on the input file
					outputFile = new File(actualOutputDirectory, baseDir.toURI().relativize(inputFile.toURI()).getPath());
					createOutputFile(outputFile);
				} else {
					outputFile = inputFile;
				}
				process(resolver, inputFile.getPath(), inputFile, outputFile, configIdentifier, action, type);
			}
		} else {
			File inputFile = fileResolver.resolve(transformation.getInput());
			if (!inputFile.exists()) {
				throw new ConfigProcessorException("Input file [" + inputFile + "] does not exist");
			}
			// use input file as output file if output is not set
			File output;
			if (StringUtils.isBlank(transformation.getOutput())) {
				output = inputFile;
			} else {
				output = new File(actualOutputDirectory, transformation.getOutput());
				createOutputFile(output);
			}
			String type = getInputType(transformation, inputFile);
			process(resolver, transformation.getInput(), inputFile, output, configIdentifier, action, type);
		}
	}

	protected Action getAction(Transformation transformation) throws ConfigProcessorException, IOException {
		if (transformation.getConfig() == null && transformation.getRules() == null) {
			throw new ConfigProcessorException("Transformation config file or rules must be set");
		} else if (transformation.getConfig() != null && transformation.getRules() != null) {
			throw new ConfigProcessorException("Cannot specify transformation config file and rules at the same time");
		}

		Action action;
		Reader configReader;
		if (transformation.getConfig() == null) {
			StringWriter writer = new StringWriter();
			AntrunXmlPlexusConfigurationWriter xmlWriter = new AntrunXmlPlexusConfigurationWriter();
			xmlWriter.write(transformation.getRules(), writer);
			String configContent = trimToNull(writer.toString());
			configContent = removeStart(configContent, "<rules>");
			configContent = removeEnd(configContent, "</rules>");
			StringBuilder sb = new StringBuilder(configContent.length() + XmlHelper.ROOT_PROCESSOR_START.length() + XmlHelper.ROOT_PROCESSOR_END.length());
			sb.append(XmlHelper.ROOT_PROCESSOR_START);
			sb.append(configContent);
			sb.append(XmlHelper.ROOT_PROCESSOR_END);
			configReader = new StringReader(sb.toString());
		} else {
			File config = fileResolver.resolve(transformation.getConfig());

			if (!config.exists()) {
				throw new ConfigProcessorException("Configuration file [" + config + "] does not exist");
			}

			configReader = new InputStreamReader(new FileInputStream(config), encoding);
		}

		try {
			ProcessingConfigurationParser parser = new ProcessingConfigurationParser();
			action = parser.parse(configReader);
		} catch (ParsingException e) {
			throw new ConfigProcessorException("Error parsing transformation config [" + getConfigIdentifier(transformation) + "]", e);
		} finally {
			close(configReader, getLog());
		}
		action.validate();

		return action;
	}

	protected String getConfigIdentifier(Transformation transformation) throws ConfigProcessorException {
		if (transformation.getConfig() == null) {
			StringWriter writer = new StringWriter();
			AntrunXmlPlexusConfigurationWriter xmlWriter = new AntrunXmlPlexusConfigurationWriter();
			try {
				xmlWriter.write(transformation.getRules(), writer);
			} catch (IOException e) {
				throw new ConfigProcessorException("Shouldn't happen because there is no I/O here", e);
			}
			return abbreviate(trimToNull(removeStart(writer.toString(), "<rules>")), 100);
		}
		return transformation.getConfig();
	}

	/**
	 * Scans all files below the given baseDirectory using the supplied pattern.
	 * All files matching the pattern are returned.
	 * The implementation is utilizing {@link DirectoryScanner} for pattern matching, e.g.
	 * it allows to use single ("*") and double wildcards ("**") for matching
	 * arbitrary characters or directories.
	 *
	 * Examples:
	 * <table>
	 * <tr>
	 * <td>
	 *
	 * <pre>
	 * *.xml
	 * </pre>
	 *
	 * </td>
	 * <td>matches all XML files in the base directory</td>
	 * </tr>
	 * <tr>
	 * <td>
	 *
	 * <pre>
	 * **\/*.xml
	 * </pre>
	 *
	 * </td>
	 * <td>matches all XML files in any subfolder</td>
	 * </tr>
	 * </table>
	 *
	 * @param pattern the directory and file name pattern that files shall match
	 * @return the {@link List} of {@link File}s that match the given pattern
	 * @throws ConfigProcessorException
	 */
	protected List<File> getMatchingFiles(String pattern) throws ConfigProcessorException {
		if (pattern == null || pattern.length() == 0) {
			throw new ConfigProcessorException("Invalid pattern	[" + pattern + "]");
		}
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(baseDir);
		scanner.setIncludes(new String[] { pattern });
		scanner.setCaseSensitive(false);
		scanner.scan();
		String[] fileNames = scanner.getIncludedFiles();
		List<File> files = new ArrayList<File>();
		for (String fileName : fileNames) {
			files.add(new File(baseDir, fileName));
		}
		return files;
	}

	/**
	 * Detects input file type.
	 * If {@link com.google.code.configprocessor.Transformation#getType()} is not null,
	 * this type is used, otherwise it is tried to guess the type from the given
	 * input File based on the file extension (.properties or .xml).
	 * If no type could be found or guessed, {@link Transformation#XML_TYPE} is used.
	 *
	 * @param transformation which can have an explicit type set
	 * @param input file from which the type can be guessed if transformation parameter does not
	 *        contain a type
	 * @return Input file type.
	 */
	protected String getInputType(Transformation transformation, File input) {
		String type;

		if (transformation.getType() == null) {
			if (input.getName().endsWith(".properties")) {
				type = Transformation.PROPERTIES_TYPE;
			} else if (input.getName().endsWith(".xml")) {
				type = Transformation.XML_TYPE;
			} else {
				if (getLog() != null) {
					getLog().warn(
							"Could not auto-detect type of input [" + input
									+ "], assuming it is XML. It is recommended that you configure it in your pom.xml (tag: transformations/transformation/type) to avoid errors");
				}
				type = Transformation.XML_TYPE;
			}
		} else {
			type = transformation.getType();
		}

		return type;
	}

	/**
	 * Processes a file.
	 *
	 * @param resolver
	 * @param inputName Symbolic name of the input file to read from.
	 * @param input Input file to read from.
	 * @param output Output file to write to.
	 * @param configName Symbolic name of the file containing rules to process the input.
	 * @param action Action to be performed on the input file.
	 * @param type Type of the input file. Properties, XML or null if it is to be auto-detected.
	 * @throws ConfigProcessorException If processing cannot be performed.
	 */
	protected void process(ExpressionResolver resolver, String inputName, File input, File output, String configName, Action action, String type) throws ConfigProcessorException {
		getLog().info("Processing file [" + inputName + "] using config [" + configName + "], outputing to [" + output + "]");

		InputStream inputStream = null;
		ByteArrayOutputStream outputStream = null;

		InputStreamReader inputStreamReader = null;
		OutputStreamWriter outputStreamWriter = null;
		try {
			inputStream = new FileInputStream(input);
			outputStream = new ByteArrayOutputStream();

			inputStreamReader = new InputStreamReader(inputStream, encoding);
			outputStreamWriter = new OutputStreamWriter(outputStream, encoding);

			ActionProcessor processor = getActionProcessor(resolver, type);
			processor.process(inputStreamReader, outputStreamWriter, action);
		} catch (ParsingException e) {
			throw new ConfigProcessorException("Error processing file [" + inputName + "] using configuration [" + configName + "]", e);
		} catch (IOException e) {
			throw new ConfigProcessorException("Error reading/writing files. Input is [" + inputName + "], configuration is [" + configName + "]", e);
		} finally {
			close(inputStreamReader, getLog());
		}
		FileOutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream(output);
			outputStream.writeTo(fileOut);
		} catch (FileNotFoundException e) {
			getLog().error("Error opening file [" + output + "]", e);
		} catch (IOException e) {
			getLog().error("Error writing file [" + output + "]", e);
		} finally {
			close(outputStreamWriter, getLog());
			close(fileOut, getLog());
		}
	}

	/**
	 * Obtain the action processor for the input.
	 *
	 * @param expressionResolver
	 * @param type Type of the input file. Properties or XML.
	 * @return ActionProcessor for the input file.
	 * @throws ConfigProcessorException If processing cannot be performed.
	 */
	protected ActionProcessor getActionProcessor(ExpressionResolver expressionResolver, String type) throws ConfigProcessorException {
		if (Transformation.XML_TYPE.equals(type)) {
			return new XmlActionProcessor(encoding, lineWidth, indentSize, fileResolver, expressionResolver, namespaceContexts, parserFeatures, failOnMissingXpath);
		} else if (Transformation.PROPERTIES_TYPE.equals(type)) {
			return new PropertiesActionProcessor(encoding, fileResolver, expressionResolver);
		} else {
			throw new ConfigProcessorException("Unknown file type [" + type + "]");
		}
	}

	/**
	 * Creates output file and required directories.
	 *
	 * @param output Output file to create.
	 * @throws ConfigProcessorException If processing cannot be performed.
	 */
	protected void createOutputFile(File output) throws ConfigProcessorException {
		try {
			File directory = output.getParentFile();
			getLog().debug(output.toString());
			if (!directory.exists()) {
				forceMkdirs(output.getParentFile());
			}
		} catch (IOException e) {
			throw new ConfigProcessorException(e.getMessage(), e);
		}
	}

	public LogAdapter getLog() {
		return log;
	}
}
