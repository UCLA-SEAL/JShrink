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

public class XmlAddActionProcessingAdvisorTest extends AbstractXmlActionProcessingAdvisorTest {

	@Test
	public void addAfter() throws Exception {
		AddAction action = new AddAction(null, "<test-property>test-value</test-property>", "/root/property3", null);
		XmlAddActionProcessingAdvisor advisor = new XmlAddActionProcessingAdvisor(action, null, expressionResolver, namespaceContext, Collections.<ParserFeature>emptyList());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + "<root>" + LINE_SEPARATOR + " <property1>value1</property1>" + LINE_SEPARATOR + " <property2/>" + LINE_SEPARATOR + " <property3 attribute=\"value3\">value3</property3>" + LINE_SEPARATOR + " <test-property>test-value</test-property>" + LINE_SEPARATOR + " <property4 attribute=\"value4\">value4</property4>" + LINE_SEPARATOR + " <property5>" + LINE_SEPARATOR + "  <nested1 a=\"1\"/>" + LINE_SEPARATOR + " </property5>" + LINE_SEPARATOR + "</root>" + LINE_SEPARATOR;
		executeTest(advisor, expected);
	}

	@Test
	public void addBefore() throws Exception {
		AddAction action = new AddAction(null, "<test-property>test-value</test-property>", null, "/root/property1");
		XmlAddActionProcessingAdvisor advisor = new XmlAddActionProcessingAdvisor(action, null, expressionResolver, namespaceContext, Collections.<ParserFeature>emptyList());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + "<root>" + LINE_SEPARATOR + " <test-property>test-value</test-property>" + LINE_SEPARATOR + " <property1>value1</property1>" + LINE_SEPARATOR + " <property2/>" + LINE_SEPARATOR + " <property3 attribute=\"value3\">value3</property3>" + LINE_SEPARATOR + " <property4 attribute=\"value4\">value4</property4>" + LINE_SEPARATOR + " <property5>" + LINE_SEPARATOR + "  <nested1 a=\"1\"/>" + LINE_SEPARATOR + " </property5>" + LINE_SEPARATOR + "</root>" + LINE_SEPARATOR;
		executeTest(advisor, expected);
	}

	@Test
	public void addAfterNested() throws Exception {
		AddAction action = new AddAction(null, "<test-property>test-value</test-property>", "/root/property5/nested1", null);
		XmlAddActionProcessingAdvisor advisor = new XmlAddActionProcessingAdvisor(action, null, expressionResolver, namespaceContext, Collections.<ParserFeature>emptyList());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + "<root>" + LINE_SEPARATOR + " <property1>value1</property1>" + LINE_SEPARATOR + " <property2/>" + LINE_SEPARATOR + " <property3 attribute=\"value3\">value3</property3>" + LINE_SEPARATOR + " <property4 attribute=\"value4\">value4</property4>" + LINE_SEPARATOR + " <property5>" + LINE_SEPARATOR + "  <nested1 a=\"1\"/>" + LINE_SEPARATOR + "  <test-property>test-value</test-property>" + LINE_SEPARATOR + " </property5>" + LINE_SEPARATOR + "</root>" + LINE_SEPARATOR;
		executeTest(advisor, expected);
	}

	@Test
	public void addAttributeOnNodeWithoutAttributes() throws Exception {
		AddAction action = new AddAction("/root/property1", "test-attribute=\"test-value\"", null, null);
		XmlAddActionProcessingAdvisor advisor = new XmlAddActionProcessingAdvisor(action, null, expressionResolver, namespaceContext, Collections.<ParserFeature>emptyList());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + "<root>" + LINE_SEPARATOR + " <property1 test-attribute=\"test-value\">value1</property1>" + LINE_SEPARATOR + " <property2/>" + LINE_SEPARATOR + " <property3 attribute=\"value3\">value3</property3>" + LINE_SEPARATOR + " <property4 attribute=\"value4\">value4</property4>" + LINE_SEPARATOR + " <property5>" + LINE_SEPARATOR + "  <nested1 a=\"1\"/>" + LINE_SEPARATOR + " </property5>" + LINE_SEPARATOR + "</root>" + LINE_SEPARATOR;
		executeTest(advisor, expected);
	}

	@Test
	public void addAttributeOnNodeWithAttributes() throws Exception {
		AddAction action = new AddAction("/root/property5/nested1", "test-attribute=\"test-value\"", null, null);
		XmlAddActionProcessingAdvisor advisor = new XmlAddActionProcessingAdvisor(action, null, expressionResolver, namespaceContext, Collections.<ParserFeature>emptyList());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + "<root>" + LINE_SEPARATOR + " <property1>value1</property1>" + LINE_SEPARATOR + " <property2/>" + LINE_SEPARATOR + " <property3 attribute=\"value3\">value3</property3>" + LINE_SEPARATOR + " <property4 attribute=\"value4\">value4</property4>" + LINE_SEPARATOR + " <property5>" + LINE_SEPARATOR + "  <nested1 a=\"1\" test-attribute=\"test-value\"/>" + LINE_SEPARATOR + " </property5>" + LINE_SEPARATOR + "</root>" + LINE_SEPARATOR;
		executeTest(advisor, expected);
	}

	@Test
	public void addFileInside() throws Exception {
		AddAction action = new AddAction();
		action.setInside("/root/property2");
		action.setIgnoreRoot(false);
		XmlAddActionProcessingAdvisor advisor = new XmlAddActionProcessingAdvisor(action, "<test-property>test-value</test-property>", expressionResolver, namespaceContext, Collections.<ParserFeature>emptyList());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + "<root>" + LINE_SEPARATOR + " <property1>value1</property1>" + LINE_SEPARATOR + " <property2>" + LINE_SEPARATOR + "  <test-property>test-value</test-property>" + LINE_SEPARATOR + " </property2>" + LINE_SEPARATOR + " <property3 attribute=\"value3\">value3</property3>" + LINE_SEPARATOR + " <property4 attribute=\"value4\">value4</property4>" + LINE_SEPARATOR + " <property5>" + LINE_SEPARATOR + "  <nested1 a=\"1\"/>" + LINE_SEPARATOR + " </property5>" + LINE_SEPARATOR + "</root>" + LINE_SEPARATOR;
		executeTest(advisor, expected);
	}
	
	@Test
	public void addFileInsideNested() throws Exception {
		AddAction action = new AddAction();
		action.setInside("/root/property5/nested1");
		action.setIgnoreRoot(false);
		XmlAddActionProcessingAdvisor advisor = new XmlAddActionProcessingAdvisor(action, "<test-property>test-value</test-property>", expressionResolver, namespaceContext, Collections.<ParserFeature>emptyList());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + "<root>" + LINE_SEPARATOR + " <property1>value1</property1>" + LINE_SEPARATOR + " <property2/>" + LINE_SEPARATOR + " <property3 attribute=\"value3\">value3</property3>" + LINE_SEPARATOR + " <property4 attribute=\"value4\">value4</property4>" + LINE_SEPARATOR + " <property5>" + LINE_SEPARATOR + "  <nested1 a=\"1\">" + LINE_SEPARATOR + "   <test-property>test-value</test-property>" + LINE_SEPARATOR + "  </nested1>" + LINE_SEPARATOR + " </property5>" + LINE_SEPARATOR + "</root>" + LINE_SEPARATOR;
		executeTest(advisor, expected);
	}

	@Test
	public void addFileIgnoreRoot() throws Exception {
		AddAction action = new AddAction();
		action.setAfter("/root/property5/nested1");
		action.setIgnoreRoot(true);
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + "<root>" + LINE_SEPARATOR + " <property1>value1</property1>" + LINE_SEPARATOR + " <property2/>" + LINE_SEPARATOR + " <property3 attribute=\"value3\">value3</property3>" + LINE_SEPARATOR + " <property4 attribute=\"value4\">value4</property4>" + LINE_SEPARATOR + " <property5>" + LINE_SEPARATOR + "  <nested1 a=\"1\"/>" + LINE_SEPARATOR + "  <test-property>test-value</test-property>" + LINE_SEPARATOR + " </property5>" + LINE_SEPARATOR + "</root>" + LINE_SEPARATOR;
		XmlAddActionProcessingAdvisor advisor = new XmlAddActionProcessingAdvisor(action, "<root><test-property>test-value</test-property></root>", expressionResolver, namespaceContext, Collections.<ParserFeature>emptyList());
		executeTest(advisor, expected);
	}

	@Test
	public void addFileDontIgnoreRoot() throws Exception {
		AddAction action = new AddAction();
		action.setAfter("/root/property5/nested1");
		action.setIgnoreRoot(false);
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + "<root>" + LINE_SEPARATOR + " <property1>value1</property1>" + LINE_SEPARATOR + " <property2/>" + LINE_SEPARATOR + " <property3 attribute=\"value3\">value3</property3>" + LINE_SEPARATOR + " <property4 attribute=\"value4\">value4</property4>" + LINE_SEPARATOR + " <property5>" + LINE_SEPARATOR + "  <nested1 a=\"1\"/>" + LINE_SEPARATOR + "  <root>" + LINE_SEPARATOR + "   <test-property>test-value</test-property>" + LINE_SEPARATOR + "  </root>" + LINE_SEPARATOR + " </property5>" + LINE_SEPARATOR + "</root>" + LINE_SEPARATOR;
		XmlAddActionProcessingAdvisor advisor = new XmlAddActionProcessingAdvisor(action, "<root><test-property>test-value</test-property></root>", expressionResolver, namespaceContext, Collections.<ParserFeature>emptyList());
		executeTest(advisor, expected);
	}
}
