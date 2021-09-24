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

import static com.google.code.configprocessor.processing.properties.PropertiesActionProcessor.*;

import org.junit.*;

import com.google.code.configprocessor.processing.*;

public class PropertiesAddActionProcessingAdvisorTest extends AbstractPropertiesActionProcessingAdvisorTest {

	private static final String APPENDED_PROPERTIES_PATH = "/com/google/code/configprocessor/data/appended-properties-config.properties";
	
	@Test
	public void processAddBeforeFirst() throws Exception {
		Action action = new AddAction("test-property", "test-value", null, "property1.value");
		String expected = "test-property=test-value" + LINE_SEPARATOR + "property1.value=value1" + LINE_SEPARATOR + "property2.value=" + LINE_SEPARATOR + "# Comment" + LINE_SEPARATOR + "	property3.value=value3 \\" + LINE_SEPARATOR + "value 3 continuation" + LINE_SEPARATOR + "# property4.value=value4 \\" + LINE_SEPARATOR + "#value 4 continuation" + LINE_SEPARATOR + "#property5.value=value5" + LINE_SEPARATOR + "property6.value=value6=value" + LINE_SEPARATOR;
		
		executeTest(action, expected);
	}

	@Test
	public void processAddAfterProperty() throws Exception {
		Action action = new AddAction("test-property", "test-value", "property1.value", null);
		String expected = "property1.value=value1" + LINE_SEPARATOR + "test-property=test-value" + LINE_SEPARATOR + "property2.value=" + LINE_SEPARATOR + "# Comment" + LINE_SEPARATOR + "	property3.value=value3 \\" + LINE_SEPARATOR + "value 3 continuation" + LINE_SEPARATOR + "# property4.value=value4 \\" + LINE_SEPARATOR + "#value 4 continuation" + LINE_SEPARATOR + "#property5.value=value5" + LINE_SEPARATOR + "property6.value=value6=value" + LINE_SEPARATOR;

		executeTest(action, expected);
	}
	
	@Test
	public void processAddBeforeCommentedProperty() throws Exception {
		Action action = new AddAction("test-property", "test-value", null, "property3.value");
		String expected = "property1.value=value1" + LINE_SEPARATOR + "property2.value=" + LINE_SEPARATOR + "# Comment" + LINE_SEPARATOR + "test-property=test-value" + LINE_SEPARATOR + "	property3.value=value3 \\" + LINE_SEPARATOR + "value 3 continuation" + LINE_SEPARATOR + "# property4.value=value4 \\" + LINE_SEPARATOR + "#value 4 continuation" + LINE_SEPARATOR + "#property5.value=value5" + LINE_SEPARATOR + "property6.value=value6=value" + LINE_SEPARATOR;

		executeTest(action, expected);
	}

	@Test
	public void processAddAfterLast() throws Exception {
		Action action = new AddAction("test-property", "test-value", "property3.value", null);
		String expected = "property1.value=value1" + LINE_SEPARATOR + "property2.value=" + LINE_SEPARATOR + "# Comment" + LINE_SEPARATOR + "	property3.value=value3 \\" + LINE_SEPARATOR + "value 3 continuation" + LINE_SEPARATOR + "test-property=test-value" + LINE_SEPARATOR + "# property4.value=value4 \\" + LINE_SEPARATOR + "#value 4 continuation" + LINE_SEPARATOR + "#property5.value=value5" + LINE_SEPARATOR + "property6.value=value6=value" + LINE_SEPARATOR;

		executeTest(action, expected);
	}
	
	@Test
	public void processAppendFileBeforeFirst() throws Exception {
		Action action = new AddAction(APPENDED_PROPERTIES_PATH, null, "property1.value");
		String expected = "appended=1" + LINE_SEPARATOR + "property1.value=value1" + LINE_SEPARATOR + "property2.value=" + LINE_SEPARATOR + "# Comment" + LINE_SEPARATOR + "	property3.value=value3 \\" + LINE_SEPARATOR + "value 3 continuation" + LINE_SEPARATOR + "# property4.value=value4 \\" + LINE_SEPARATOR + "#value 4 continuation" + LINE_SEPARATOR + "#property5.value=value5" + LINE_SEPARATOR + "property6.value=value6=value" + LINE_SEPARATOR;
		
		executeTest(action, expected);
	}

	@Test
	public void processAppendFileAfter() throws Exception {
		Action action = new AddAction(APPENDED_PROPERTIES_PATH, "property1.value", null);
		String expected = "property1.value=value1" + LINE_SEPARATOR + "appended=1" + LINE_SEPARATOR + "property2.value=" + LINE_SEPARATOR + "# Comment" + LINE_SEPARATOR + "	property3.value=value3 \\" + LINE_SEPARATOR + "value 3 continuation" + LINE_SEPARATOR + "# property4.value=value4 \\" + LINE_SEPARATOR + "#value 4 continuation" + LINE_SEPARATOR + "#property5.value=value5" + LINE_SEPARATOR + "property6.value=value6=value" + LINE_SEPARATOR;
		
		executeTest(action, expected);
	}

	@Test
	public void processAppendFileAfterLast() throws Exception {
		Action action = new AddAction(APPENDED_PROPERTIES_PATH, "property6.value", null);
		String expected = "property1.value=value1" + LINE_SEPARATOR + "property2.value=" + LINE_SEPARATOR + "# Comment" + LINE_SEPARATOR + "	property3.value=value3 \\" + LINE_SEPARATOR + "value 3 continuation" + LINE_SEPARATOR + "# property4.value=value4 \\" + LINE_SEPARATOR + "#value 4 continuation" + LINE_SEPARATOR + "#property5.value=value5" + LINE_SEPARATOR + "property6.value=value6=value" + LINE_SEPARATOR + "appended=1" + LINE_SEPARATOR;
		
		executeTest(action, expected);
	}

	@Test
	public void processAddFirst() throws Exception {
		AddAction action = new AddAction("test-property", "test-value");
		action.setFirst(true);
		String expected = "test-property=test-value" + LINE_SEPARATOR + "property1.value=value1" + LINE_SEPARATOR + "property2.value=" + LINE_SEPARATOR + "# Comment" + LINE_SEPARATOR + "	property3.value=value3 \\" + LINE_SEPARATOR + "value 3 continuation" + LINE_SEPARATOR + "# property4.value=value4 \\" + LINE_SEPARATOR + "#value 4 continuation" + LINE_SEPARATOR + "#property5.value=value5" + LINE_SEPARATOR + "property6.value=value6=value" + LINE_SEPARATOR;

		executeTest(action, expected);
	}

	@Test
	public void processAddLast() throws Exception {
		AddAction action = new AddAction("test-property", "test-value");
		action.setLast(true);
		String expected = "property1.value=value1" + LINE_SEPARATOR + "property2.value=" + LINE_SEPARATOR + "# Comment" + LINE_SEPARATOR + "	property3.value=value3 \\" + LINE_SEPARATOR + "value 3 continuation" + LINE_SEPARATOR + "# property4.value=value4 \\" + LINE_SEPARATOR + "#value 4 continuation" + LINE_SEPARATOR + "#property5.value=value5" + LINE_SEPARATOR + "property6.value=value6=value" + LINE_SEPARATOR + "test-property=test-value" + LINE_SEPARATOR;

		executeTest(action, expected);
	}

	@Test
	public void processAppendFileFirst() throws Exception {
		AddAction action = new AddAction();
		action.setFile(APPENDED_PROPERTIES_PATH);
		action.setFirst(true);
		String expected = "appended=1" + LINE_SEPARATOR + "property1.value=value1" + LINE_SEPARATOR + "property2.value=" + LINE_SEPARATOR + "# Comment" + LINE_SEPARATOR + "	property3.value=value3 \\" + LINE_SEPARATOR + "value 3 continuation" + LINE_SEPARATOR + "# property4.value=value4 \\" + LINE_SEPARATOR + "#value 4 continuation" + LINE_SEPARATOR + "#property5.value=value5" + LINE_SEPARATOR + "property6.value=value6=value" + LINE_SEPARATOR;
		
		executeTest(action, expected);
	}

	@Test
	public void processAppendFileLast() throws Exception {
		AddAction action = new AddAction();
		action.setFile(APPENDED_PROPERTIES_PATH);
		action.setLast(true);
		String expected = "property1.value=value1" + LINE_SEPARATOR + "property2.value=" + LINE_SEPARATOR + "# Comment" + LINE_SEPARATOR + "	property3.value=value3 \\" + LINE_SEPARATOR + "value 3 continuation" + LINE_SEPARATOR + "# property4.value=value4 \\" + LINE_SEPARATOR + "#value 4 continuation" + LINE_SEPARATOR + "#property5.value=value5" + LINE_SEPARATOR + "property6.value=value6=value" + LINE_SEPARATOR + "appended=1" + LINE_SEPARATOR;
		
		executeTest(action, expected);
	}

}
