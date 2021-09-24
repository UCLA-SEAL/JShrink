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
package com.google.code.configprocessor.processing.xml;

import static com.google.code.configprocessor.processing.xml.XmlActionProcessor.*;

import java.util.*;

import org.junit.*;

import com.google.code.configprocessor.*;
import com.google.code.configprocessor.processing.*;

public class XmlModifyActionProcessingAdvisorTest extends AbstractXmlActionProcessingAdvisorTest {

	private static final String XML_PATH = "/com/google/code/configprocessor/data/modify-xml-target-config.xml";

	@Override
	public void setup() throws Exception {
		document = XmlHelper.parse(getClass().getResourceAsStream(XML_PATH), Collections.<ParserFeature>emptyList());
	}
	
	@Test
	public void modifySingleElement() throws Exception {
		ModifyAction action = new ModifyAction("/root/property1", "<test-property>test-value</test-property>");
		XmlModifyActionProcessingAdvisor advisor = new XmlModifyActionProcessingAdvisor(action, expressionResolver, namespaceContext, Collections.<ParserFeature>emptyList());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + "<root>" + LINE_SEPARATOR + " <test-property>test-value</test-property>" + LINE_SEPARATOR + " <property2/>" + LINE_SEPARATOR + " <property3 attribute=\"value3\">value3</property3>" + LINE_SEPARATOR + " <property4 attribute=\"value4\">value4</property4>" + LINE_SEPARATOR + " <property5>" + LINE_SEPARATOR + "  <nested1 a=\"1\"/>" + LINE_SEPARATOR + " </property5>" + LINE_SEPARATOR + " <property6 a=\"test@test.com\">" + LINE_SEPARATOR + "  <nested2>test@test.com</nested2>" + LINE_SEPARATOR + " </property6>" + LINE_SEPARATOR + " <!-- This email will be modified too: test@test.com -->" + LINE_SEPARATOR + "</root>" + LINE_SEPARATOR;
		executeTest(advisor, expected);
	}

	@Test
	public void modifySubtree() throws Exception {
		ModifyAction action = new ModifyAction("/root/property5", "<test-property>test-value</test-property>");
		XmlModifyActionProcessingAdvisor advisor = new XmlModifyActionProcessingAdvisor(action, expressionResolver, namespaceContext, Collections.<ParserFeature>emptyList());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + "<root>" + LINE_SEPARATOR + " <property1>value1</property1>" + LINE_SEPARATOR + " <property2/>" + LINE_SEPARATOR + " <property3 attribute=\"value3\">value3</property3>" + LINE_SEPARATOR + " <property4 attribute=\"value4\">value4</property4>" + LINE_SEPARATOR + " <test-property>test-value</test-property>" + LINE_SEPARATOR + " <property6 a=\"test@test.com\">" + LINE_SEPARATOR + "  <nested2>test@test.com</nested2>" + LINE_SEPARATOR + " </property6>" + LINE_SEPARATOR + " <!-- This email will be modified too: test@test.com -->" + LINE_SEPARATOR + "</root>" + LINE_SEPARATOR;
		executeTest(advisor, expected);
	}

	@Test
	public void modifyAttribute() throws Exception {
		ModifyAction action = new ModifyAction("/root/property5/nested1/@a", "test-value");
		XmlModifyActionProcessingAdvisor advisor = new XmlModifyActionProcessingAdvisor(action, expressionResolver, namespaceContext, Collections.<ParserFeature>emptyList());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + "<root>" + LINE_SEPARATOR + " <property1>value1</property1>" + LINE_SEPARATOR + " <property2/>" + LINE_SEPARATOR + " <property3 attribute=\"value3\">value3</property3>" + LINE_SEPARATOR + " <property4 attribute=\"value4\">value4</property4>" + LINE_SEPARATOR + " <property5>" + LINE_SEPARATOR + "  <nested1 a=\"test-value\"/>" + LINE_SEPARATOR + " </property5>" + LINE_SEPARATOR + " <property6 a=\"test@test.com\">" + LINE_SEPARATOR + "  <nested2>test@test.com</nested2>" + LINE_SEPARATOR + " </property6>" + LINE_SEPARATOR + " <!-- This email will be modified too: test@test.com -->" + LINE_SEPARATOR + "</root>" + LINE_SEPARATOR;
		executeTest(advisor, expected);
	}

	@Test
	public void modifyAttributeToEmpty() throws Exception {
		ModifyAction action = new ModifyAction("/root/property5/nested1/@a", null);
		XmlModifyActionProcessingAdvisor advisor = new XmlModifyActionProcessingAdvisor(action, expressionResolver, namespaceContext, Collections.<ParserFeature>emptyList());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + "<root>" + LINE_SEPARATOR + " <property1>value1</property1>" + LINE_SEPARATOR + " <property2/>" + LINE_SEPARATOR + " <property3 attribute=\"value3\">value3</property3>" + LINE_SEPARATOR + " <property4 attribute=\"value4\">value4</property4>" + LINE_SEPARATOR + " <property5>" + LINE_SEPARATOR + "  <nested1 a=\"\"/>" + LINE_SEPARATOR + " </property5>" + LINE_SEPARATOR + " <property6 a=\"test@test.com\">" + LINE_SEPARATOR + "  <nested2>test@test.com</nested2>" + LINE_SEPARATOR + " </property6>" + LINE_SEPARATOR + " <!-- This email will be modified too: test@test.com -->" + LINE_SEPARATOR + "</root>" + LINE_SEPARATOR;
		executeTest(advisor, expected);
	}
	
	@Test
	public void modifyFindReplace() throws Exception {
		ModifyAction action = new ModifyAction();
		action.setFind("[\\w\\-]+@\\w+\\.\\w+");
		action.setReplace("my-email@server.com");
		XmlModifyActionProcessingAdvisor advisor = new XmlModifyActionProcessingAdvisor(action, expressionResolver, namespaceContext, Collections.<ParserFeature>emptyList());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + "<root>" + LINE_SEPARATOR + " <property1>value1</property1>" + LINE_SEPARATOR + " <property2/>" + LINE_SEPARATOR + " <property3 attribute=\"value3\">value3</property3>" + LINE_SEPARATOR + " <property4 attribute=\"value4\">value4</property4>" + LINE_SEPARATOR + " <property5>" + LINE_SEPARATOR + "  <nested1 a=\"1\"/>" + LINE_SEPARATOR + " </property5>" + LINE_SEPARATOR + " <property6 a=\"my-email@server.com\">" + LINE_SEPARATOR + "  <nested2>my-email@server.com</nested2>" + LINE_SEPARATOR + " </property6>" + LINE_SEPARATOR + " <!-- This email will be modified too: my-email@server.com -->" + LINE_SEPARATOR + "</root>" + LINE_SEPARATOR;
		executeTest(advisor, expected);
	}
	
}
