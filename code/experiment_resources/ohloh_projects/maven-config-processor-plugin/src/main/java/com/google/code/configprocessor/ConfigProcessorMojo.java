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

import static com.google.code.configprocessor.util.PropertiesUtils.*;

import java.io.*;
import java.util.*;

import org.apache.maven.artifact.factory.*;
import org.apache.maven.artifact.repository.*;
import org.apache.maven.artifact.resolver.*;
import org.apache.maven.artifact.versioning.*;
import org.apache.maven.execution.*;
import org.apache.maven.plugin.*;
import org.apache.maven.project.*;
import org.apache.maven.project.path.*;
import org.codehaus.plexus.logging.*;
import org.codehaus.plexus.logging.console.*;

import com.google.code.configprocessor.io.*;
import com.google.code.configprocessor.log.*;
import com.google.code.configprocessor.maven.*;

/**
 * Generates modified configuration files according to configuration. Includes, excludes, modify, comment and uncomment properties.
 *
 * @author Leandro Aparecido
 * @phase process-resources
 * @goal process
 */
public class ConfigProcessorMojo extends AbstractMojo {

	/**
	 * Output directory of the generated files.
	 *
	 * @parameter default-value="${project.build.directory}"
	 * @required
	 * @since 1.0
	 */
	private File outputDirectory;

	/**
	 * Indicate if should prefix file paths with the outputDirectory configuration property.
	 *
	 * @parameter default-value="true"
	 * @required
	 * @since 1.0
	 */
	private boolean useOutputDirectory;

	/**
	 * Encoding to use when reading or writing files.
	 *
	 * @parameter default-value="${project.build.sourceEncoding}"
	 * @since 1.1
	 */
	private String encoding;

	/**
	 * Maximum line width of the generated files to use when formatting.
	 *
	 * @parameter default-value="80"
	 * @since 1.2
	 */
	private Integer lineWidth;

	/**
	 * Indentation size as the number of whitespaces to use when formatting.
	 *
	 * @parameter default-value="4"
	 * @since 1.2
	 */
	private Integer indentSize;

	/**
	 * File to load aditional specific properties for plugin execution.
	 *
	 * @parameter expression="${config-processor.properties}"
	 * @since 1.0
	 */
	private File specificProperties;

	/**
	 * File transformations to be performed.
	 *
	 * @parameter
	 * @since 1.0
	 */
	private List<Transformation> transformations;

	/**
	 * Namespace contexts for XPath expressions. Mapping in the form prefix => url
	 *
	 * @parameter
	 * @since 1.2
	 */
	private Map<String, String> namespaceContexts;

	/**
	 * Disables the plugin execution.
	 *
	 * @parameter expression="${config-processor.skip}" default-value="false"
	 * @since 1.7
	 */
	private boolean skip;

	/**
	 * Features to be set when parsing files.
	 *
	 * @parameter
	 * @since 1.9
	 */
	private List<ParserFeature> parserFeatures;

	/**
	 * The Maven Project Object.
	 *
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject mavenProject;

	/**
	 * The Maven Session Object.
	 *
	 * @parameter expression="${session}"
	 * @required
	 * @readonly
	 */
	private MavenSession mavenSession;

	/**
	 * The Mojo Execution Object.
	 *
	 * @parameter expression="${mojoExecution}"
	 * @required
	 * @readonly
	 */
	private MojoExecution mojoExecution;

	/**
	 * The ArtifactFactory Object.
	 *
	 * @component
	 */
	private ArtifactFactory artifactFactory;

	/**
	 * The ArtifactResolver Object.
	 *
	 * @component
	 */
	private ArtifactResolver artifactResolver;

	/**
	 * The local repository object.
	 *
	 * @parameter expression="${localRepository}"
	 * @required
	 */
	private ArtifactRepository localRepository;

	/**
	 * The remote repositories list.
	 *
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 */
	private List<ArtifactRepository> remoteRepositories;

	/**
	 * switch whether to fail when XPaths are not found within a XML document
	 *
	 * @parameter expression="${config-processor.failOnMissingXpath}" default-value="true" 	 *
	 * @since 2.1
	 */
	private boolean failOnMissingXpath = true;

    /**
     * The RuntimeInforamtion for the current instance of maven.
     * 
     * @component
     */
    private RuntimeInformation runtime;

	public ConfigProcessorMojo() {
		transformations = new ArrayList<Transformation>();
		parserFeatures = new ArrayList<ParserFeature>();
	}

	/**
	 * {@inheritDoc}
	 */
	public void execute() throws MojoExecutionException {
		LogAdapter logAdapter = new LogMaven(getLog());
		if (skip) {
			logAdapter.info("Skipping config processing");
		} else {
			try {
				FileResolver fileResolver = new MavenFileResolver(mavenProject, artifactFactory, artifactResolver, localRepository, remoteRepositories, logAdapter);
				ConfigProcessor processor = new ConfigProcessor(encoding, indentSize, lineWidth, namespaceContexts, mavenProject.getBasedir(), outputDirectory, useOutputDirectory, logAdapter, fileResolver, parserFeatures, failOnMissingXpath);
				processor.init();

				// issue 35 - Specificproperties in maven doesn't work
				ArtifactVersion mavenVersion = runtime.getApplicationVersion();
				if (specificProperties != null && mavenVersion.getMajorVersion() > 2) {
					throw new MojoExecutionException("specificProperties are not supported anymore by Maven, please specify them in the properties section of your pom.xml file");
				}
				Properties additionalProperties = loadIfPossible(specificProperties, logAdapter);

				for (Transformation transformation : transformations) {
					MavenExpressionResolver resolver = getExpressionResolver(transformation.isReplacePlaceholders(), additionalProperties);
					processor.execute(resolver, transformation);
				}
			} catch (Exception e) {
				throw new MojoExecutionException("Error during config processing", e);
			}
		}
	}

	/**
	 * Creates a expression resolver to replace placeholders.
	 *
	 * @param replacePlaceholders  True if placeholders must be replaced on output files.
	 * @param additionalProperties
	 * @return Created ExpressionResolver.
	 * @throws MojoExecutionException If processing cannot be performed.
	 */
	protected MavenExpressionResolver getExpressionResolver(boolean replacePlaceholders, Properties additionalProperties) throws MojoExecutionException {
		return new MavenExpressionResolver(new PluginParameterExpressionEvaluator(mavenSession, mojoExecution, new DefaultPathTranslator(), new ConsoleLogger(Logger.LEVEL_INFO, "ConfigProcessorMojo"),
				mavenProject, additionalProperties), replacePlaceholders);
	}

}
