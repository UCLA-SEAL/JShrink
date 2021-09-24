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
package com.google.code.configprocessor.util;

import static org.junit.Assert.*;

import org.apache.commons.lang.*;
import org.junit.*;

public class PropertiesUtilsTest {

	@Test
	public void getAsTextSimple() {
		executeExportingTest("value", "value");
	}
	
	@Test
	public void getAsTextNullValue() {
		executeExportingTest(null, null);
	}
	
	@Test
	public void getAsTextEmptyValue() {
		executeExportingTest("", StringUtils.EMPTY);
	}
	
	@Test
	public void getAsTextEscapingBackslashes() {
		executeExportingTest("c:\\\\file.txt", "c:\\file.txt");
	}
	
	@Test
	public void getAsTextEscapingLineBreaks() {
		executeExportingTest("value1\\\rvalue2", "value1\rvalue2");
		executeExportingTest("value1\\\nvalue2", "value1\nvalue2");
		executeExportingTest("value1\\\r\nvalue2", "value1\r\nvalue2");
	}

	protected void executeExportingTest(String expected, String value) {
		assertEquals(expected, PropertiesUtils.escapePropertyValue(value));
	}

}
