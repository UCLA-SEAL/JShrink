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

import static org.junit.Assert.*;

import java.io.*;

import org.codehaus.plexus.component.configurator.expression.*;
import org.junit.*;

import com.google.code.configprocessor.io.*;
import com.google.code.configprocessor.maven.*;
import com.google.code.configprocessor.processing.*;

@Ignore
public class AbstractPropertiesActionProcessingAdvisorTest {

	private static final String ENCODING = "ISO-8859-1";
	
	protected InputStream input;
	protected ByteArrayOutputStream output;
	protected ActionProcessor processor;
	
	public void setup() {
		processor = new PropertiesActionProcessor(ENCODING, new ClasspathFileResolver(), new MavenExpressionResolver(new DefaultExpressionEvaluator()));
		input = getClass().getResourceAsStream(PropertiesActionProcessorTest.PROPERTIES_PATH);
		output = new ByteArrayOutputStream();
	}

	protected void executeTest(Action action, String expected) throws Exception {
		setup();
		processor.process(new InputStreamReader(input), new OutputStreamWriter(output), action);
		assertEquals(expected, getOutput());
		
		setup();
		NestedAction nestedAction = new NestedAction();
		nestedAction.addAction(action);
		processor.process(new InputStreamReader(input), new OutputStreamWriter(output), nestedAction);
		assertEquals(expected, getOutput());
	}
	
	protected String getOutput() {
		return new String(output.toByteArray());
	}
}
