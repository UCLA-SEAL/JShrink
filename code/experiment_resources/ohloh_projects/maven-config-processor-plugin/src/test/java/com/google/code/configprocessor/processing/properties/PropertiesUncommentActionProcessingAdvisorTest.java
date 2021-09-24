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

public class PropertiesUncommentActionProcessingAdvisorTest extends AbstractPropertiesActionProcessingAdvisorTest {

	public static final String PROPERTIES_PATH = "/com/google/code/configprocessor/data/uncomment-properties-target-config.properties";

	@Override
	public void setup() {
		super.setup();
		input = getClass().getResourceAsStream(PROPERTIES_PATH);
	}
	
	@Test
	public void processUncomment() throws Exception {
		Action action = new UncommentAction("property4.value");
		String expected = "property1.value=value1" + LINE_SEPARATOR + "property2.value=" + LINE_SEPARATOR + "# Comment" + LINE_SEPARATOR + "	property3.value=value3 \\" + LINE_SEPARATOR + "value 3 continuation" + LINE_SEPARATOR + "property4.value=value4 \\" + LINE_SEPARATOR + "value 4 continuation" + LINE_SEPARATOR + "#property5.value=value5" + LINE_SEPARATOR + "property6.value=value6=value" + LINE_SEPARATOR + "#" + LINE_SEPARATOR + "#property7.value=value7" + LINE_SEPARATOR + "#  " + LINE_SEPARATOR;

		executeTest(action, expected);
	}

	@Test
	public void processUncommentAfterComment() throws Exception {
		Action action = new UncommentAction("property5.value");
		String expected = "property1.value=value1" + LINE_SEPARATOR + "property2.value=" + LINE_SEPARATOR + "# Comment" + LINE_SEPARATOR + "	property3.value=value3 \\" + LINE_SEPARATOR + "value 3 continuation" + LINE_SEPARATOR + "# property4.value=value4 \\" + LINE_SEPARATOR + "#value 4 continuation" + LINE_SEPARATOR + "property5.value=value5" + LINE_SEPARATOR + "property6.value=value6=value" + LINE_SEPARATOR + "#" + LINE_SEPARATOR + "#property7.value=value7" + LINE_SEPARATOR + "#  " + LINE_SEPARATOR;

		executeTest(action, expected);
	}

	@Test
	public void processUncommentPropertyWithEmptyValue() throws Exception {
		Action action = new UncommentAction("property2.value");
		String expected = "property1.value=value1" + LINE_SEPARATOR + "property2.value=" + LINE_SEPARATOR + "# Comment" + LINE_SEPARATOR + "	property3.value=value3 \\" + LINE_SEPARATOR + "value 3 continuation" + LINE_SEPARATOR + "# property4.value=value4 \\" + LINE_SEPARATOR + "#value 4 continuation" + LINE_SEPARATOR + "#property5.value=value5" + LINE_SEPARATOR + "property6.value=value6=value" + LINE_SEPARATOR + "#" + LINE_SEPARATOR + "#property7.value=value7" + LINE_SEPARATOR + "#  " + LINE_SEPARATOR;

		executeTest(action, expected);
	}

	/**
	 * Test for issue #11 (http://code.google.com/p/maven-config-processor-plugin/issues/detail?id=11)
	 * @throws Exception
	 */
	@Test
	public void processUncommentPropertyAfterEmptyComment() throws Exception {
		Action action = new UncommentAction("property7.value");
		String expected = "property1.value=value1" + LINE_SEPARATOR + "property2.value=" + LINE_SEPARATOR + "# Comment" + LINE_SEPARATOR + "	property3.value=value3 \\" + LINE_SEPARATOR + "value 3 continuation" + LINE_SEPARATOR + "# property4.value=value4 \\" + LINE_SEPARATOR + "#value 4 continuation" + LINE_SEPARATOR + "#property5.value=value5" + LINE_SEPARATOR + "property6.value=value6=value" + LINE_SEPARATOR + "#" + LINE_SEPARATOR + "property7.value=value7" + LINE_SEPARATOR + "#  " + LINE_SEPARATOR;
		
		executeTest(action, expected);
	}
}
