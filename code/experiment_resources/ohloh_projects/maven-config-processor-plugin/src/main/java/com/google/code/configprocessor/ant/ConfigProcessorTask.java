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
package com.google.code.configprocessor.ant;

import static com.google.code.configprocessor.util.PropertiesUtils.*;

import java.io.*;
import java.util.*;

import org.apache.tools.ant.*;

import com.google.code.configprocessor.*;
import com.google.code.configprocessor.expression.*;
import com.google.code.configprocessor.io.*;
import com.google.code.configprocessor.log.*;

/**
 * Ant task Generates modified configuration files according to configuration.
 * Includes, excludes, modify, comment and uncomment properties.
 */
public class ConfigProcessorTask extends Task {

	private List<Transformation> transforms;
	private String encoding;
	private int indentSize;
	private int lineWidth;
	private List<NamespaceContext> namespaceContexts;
	private File outputDirectory;
	private boolean useOutputDirectory;
	private File specificProperties;
	private LogAdapter log;
	private List<ParserFeature> parserFeatures;
	private boolean failOnMissingXpath = true;

	public ConfigProcessorTask() {
		transforms = new ArrayList<Transformation>();
		namespaceContexts = new ArrayList<NamespaceContext>();
		log = new LogAnt(this);
		parserFeatures = new ArrayList<ParserFeature>();
	}
	
	@Override
	public void init() throws BuildException {
		super.init();
		indentSize = 4;
		lineWidth = 80;
		outputDirectory = getProject().getBaseDir();
		useOutputDirectory = true;
	}
	
	@Override
	public void execute() {
		try {
			Map<String, String> namespaceContextsMap = new HashMap<String, String>();
			for (NamespaceContext nsContext : namespaceContexts) {
				namespaceContextsMap.put(nsContext.getPrefix(), nsContext.getUrl());
			}
			ConfigProcessor processor = new ConfigProcessor(encoding, indentSize, lineWidth, namespaceContextsMap, getProject().getBaseDir(), outputDirectory, useOutputDirectory, log, new DefaultFileResolver(), parserFeatures, failOnMissingXpath);
			processor.init();
			
			Properties additionalProperties = loadIfPossible(specificProperties, log);
			
			for (Transformation transformation : transforms) {
					ExpressionResolver resolver = getExpressionResolver(transformation.isReplacePlaceholders(), additionalProperties);
					processor.execute(resolver, transformation);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException("Error during config processing", e);
		}
	}

	public Transformation createTransformation() {
		Transformation transformation = new Transformation();
		transforms.add(transformation);
		return transformation;
	}

	public NamespaceContext createNamespaceContext() {
		NamespaceContext namespaceContext = new NamespaceContext();
		namespaceContexts.add(namespaceContext);
		return namespaceContext;
	}
	
	public ParserFeature createParserFeature() {
		ParserFeature parserFeature = new ParserFeature();
		parserFeatures.add(parserFeature);
		return parserFeature;
	}

	protected ExpressionResolver getExpressionResolver(boolean replacePlaceholders, Properties additionalProperties) {
		return new AntExpressionResolver(getProject(), additionalProperties, replacePlaceholders);
	}
	
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setIndentSize(int indentSize) {
		this.indentSize = indentSize;
	}

	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = new File(outputDirectory);
	}

	public void setUseOutputDirectory(boolean useOutputDirectory) {
		this.useOutputDirectory = useOutputDirectory;
	}

	public void setSpecificProperties(File specificProperties) {
		this.specificProperties = specificProperties;
	}

	/**
	 * switch whether to fail when XPaths are not found within a XML document (default: true)
	 * @since 2.1
	 */
	public void setFailOnMissingXpath(boolean failOnMissingXpath) {
		this.failOnMissingXpath = failOnMissingXpath;
	}
	

	public static class NamespaceContext {
		private String prefix;
		private String url;

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}
	}

}
