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

import org.codehaus.plexus.configuration.*;

/**
 * Configuration of a file transformation.
 * 
 * @author Leandro Aparecido
 */
public class Transformation {

	public static final String PROPERTIES_TYPE = "properties";
	public static final String XML_TYPE = "xml";

	/**
	 * File(s) to process.
	 * If a pattern-based String with wildcards (e.g. <code>**\/* .xml</code>) is supplied,
	 * all files below the base directory (the Maven or ANT project base directory) directory matching the pattern
	 * will be processed.
	 * 
	 * The implementation is utilizing {@link org.apache.tools.ant.DirectoryScanner} for the pattern
	 * matching,
	 * e.g. it allows to use single ("*") and double wildcards ("**")
	 * for matching arbitrary characters or directories.
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
	 * @parameter
	 * @required
	 */
	private String input;

	/**
	 * Output file to generate the result of processing.
	 * May be the same as input in order to override it.
	 * 
	 * @parameter
	 * @required
	 */
	private String output;

	/**
	 * Configuration file describing the processing to be performed.
	 * Either this property or rules must be set.
	 * 
	 * @parameter
	 */
	private String config;

	/**
	 * Type of the file to transform. If not specified, the plugin will try to auto-detect. Possible
	 * values: properties, xml.
	 * 
	 * @parameter
	 * @required
	 */
	private String type;

	/**
	 * Indicates if the plugin should replace values in ${} with properties of the maven
	 * environment.
	 * 
	 * @parameter default-value="true"
	 */
	private boolean replacePlaceholders;

	/**
	 * Transformation rules to be applied to the input file.
	 * Either this property or config must be set.
	 * 
	 * @parameter
	 * @since 2.2
	 */
	private PlexusConfiguration rules;

	public Transformation() {
		replacePlaceholders = true;
	}

	public String getInput() {
		return input;
	}

	public String getOutput() {
		return output;
	}

	public String getConfig() {
		return config;
	}

	public String getType() {
		return type;
	}

	public boolean isReplacePlaceholders() {
		return replacePlaceholders;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setReplacePlaceholders(boolean replacePlaceholders) {
		this.replacePlaceholders = replacePlaceholders;
	}

	public PlexusConfiguration getRules() {
		return rules;
	}

	public void setRules(PlexusConfiguration rules) {
		this.rules = rules;
	}
}
