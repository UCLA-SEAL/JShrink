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

public class XmlRemoveActionProcessingAdvisorTest extends AbstractXmlActionProcessingAdvisorTest {

	@Test
	public void removeSingleElement() throws Exception {
		RemoveAction action = new RemoveAction("/root/property1");
		XmlActionProcessingAdvisor advisor = new XmlRemoveActionProcessingAdvisor(action, expressionResolver, namespaceContext, Collections.<ParserFeature>emptyList());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + "<root>" + LINE_SEPARATOR + " <property2/>" + LINE_SEPARATOR + " <property3 attribute=\"value3\">value3</property3>" + LINE_SEPARATOR + " <property4 attribute=\"value4\">value4</property4>" + LINE_SEPARATOR + " <property5>" + LINE_SEPARATOR + "  <nested1 a=\"1\"/>" + LINE_SEPARATOR + " </property5>" + LINE_SEPARATOR + "</root>" + LINE_SEPARATOR;
		executeTest(advisor, expected);
	}

	@Test
	public void removeNestedElement() throws Exception {
		RemoveAction action = new RemoveAction("/root/property5/nested1");
		XmlActionProcessingAdvisor advisor = new XmlRemoveActionProcessingAdvisor(action, expressionResolver, namespaceContext, Collections.<ParserFeature>emptyList());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + "<root>" + LINE_SEPARATOR + " <property1>value1</property1>" + LINE_SEPARATOR + " <property2/>" + LINE_SEPARATOR + " <property3 attribute=\"value3\">value3</property3>" + LINE_SEPARATOR + " <property4 attribute=\"value4\">value4</property4>" + LINE_SEPARATOR + " <property5/>" + LINE_SEPARATOR + "</root>" + LINE_SEPARATOR;
		executeTest(advisor, expected);
	}
	
	@Test
	public void removeSubtree() throws Exception {
		RemoveAction action = new RemoveAction("/root/property5");
		XmlActionProcessingAdvisor advisor = new XmlRemoveActionProcessingAdvisor(action, expressionResolver, namespaceContext, Collections.<ParserFeature>emptyList());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + "<root>" + LINE_SEPARATOR + " <property1>value1</property1>" + LINE_SEPARATOR + " <property2/>" + LINE_SEPARATOR + " <property3 attribute=\"value3\">value3</property3>" + LINE_SEPARATOR + " <property4 attribute=\"value4\">value4</property4>" + LINE_SEPARATOR + "</root>" + LINE_SEPARATOR;
		executeTest(advisor, expected);
	}
	
	@Test
	public void removeAttribute() throws Exception {
		RemoveAction action = new RemoveAction("/root/property5/nested1/@a");
		XmlActionProcessingAdvisor advisor = new XmlRemoveActionProcessingAdvisor(action, expressionResolver, namespaceContext, Collections.<ParserFeature>emptyList());
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + LINE_SEPARATOR + "<root>" + LINE_SEPARATOR + " <property1>value1</property1>" + LINE_SEPARATOR + " <property2/>" + LINE_SEPARATOR + " <property3 attribute=\"value3\">value3</property3>" + LINE_SEPARATOR + " <property4 attribute=\"value4\">value4</property4>" + LINE_SEPARATOR + " <property5>" + LINE_SEPARATOR + "  <nested1/>" + LINE_SEPARATOR + " </property5>" + LINE_SEPARATOR + "</root>" + LINE_SEPARATOR;
		executeTest(advisor, expected);
	}
}
