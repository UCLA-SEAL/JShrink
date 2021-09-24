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

import static org.junit.Assert.*;

import java.util.*;

import javax.xml.namespace.*;

import org.codehaus.plexus.component.configurator.expression.*;
import org.junit.*;
import org.w3c.dom.*;

import com.google.code.configprocessor.*;
import com.google.code.configprocessor.maven.*;

@Ignore
public class AbstractXmlActionProcessingAdvisorTest {

	protected MavenExpressionResolver expressionResolver;
	protected NamespaceContext namespaceContext;
	protected Document document;

	public AbstractXmlActionProcessingAdvisorTest() {
		expressionResolver = new MavenExpressionResolver(new DefaultExpressionEvaluator());
		namespaceContext = new EmptyNamespaceContext();
	}
	
	@Before
	public void setup() throws Exception {
		List<ParserFeature> features = Collections.emptyList();
		document = XmlHelper.parse(getClass().getResourceAsStream("/com/google/code/configprocessor/data/xml-target-config.xml"), features);
	}
	
	protected void executeTest(XmlActionProcessingAdvisor advisor, String expected) throws Exception {
		advisor.process(document);
		assertEquals(expected, XmlHelper.write(document, "UTF-8", 80, 1));
	}

}
