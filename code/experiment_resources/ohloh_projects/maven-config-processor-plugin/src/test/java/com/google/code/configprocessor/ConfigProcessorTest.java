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

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;

public class ConfigProcessorTest {

	private ConfigProcessor configProcessor;

	@Before
	public void setup() throws Exception {
		configProcessor = new ConfigProcessor("UTF-8", 80, 4, null, null, null, false, null, null, null, true);
	}

	@Test(expected = ConfigProcessorException.class)
	public void testNullPattern() throws Exception {
		File baseDir = createStrictMock(File.class);
		expect(baseDir.isDirectory()).andReturn(true);
		expect(baseDir.exists()).andReturn(true);
		configProcessor.getMatchingFiles(null);
	}

	@Test(expected = ConfigProcessorException.class)
	public void testEmptyPattern() throws Exception {
		File baseDir = createStrictMock(File.class);
		expect(baseDir.isDirectory()).andReturn(true);
		expect(baseDir.exists()).andReturn(true);
		configProcessor.getMatchingFiles("");
	}

	@Test
	public void testGetTypeFromTransformation() throws Exception {
		Transformation transformation = new Transformation();
		transformation.setType(Transformation.PROPERTIES_TYPE);
		assertEquals(Transformation.PROPERTIES_TYPE, configProcessor.getInputType(transformation, null));
	}

	@Test
	public void testGuessTypeFromInputFile() throws Exception {
		Transformation transformation = new Transformation();
		assertEquals(Transformation.PROPERTIES_TYPE, configProcessor.getInputType(transformation, new File("test.properties")));
		assertEquals(Transformation.XML_TYPE, configProcessor.getInputType(transformation, new File("test.xml")));
		assertEquals(Transformation.XML_TYPE, configProcessor.getInputType(transformation, new File("test.something")));
	}
}
