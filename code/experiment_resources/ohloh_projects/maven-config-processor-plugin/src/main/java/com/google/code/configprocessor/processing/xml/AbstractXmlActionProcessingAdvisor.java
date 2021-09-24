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

import java.util.*;

import javax.xml.namespace.*;
import javax.xml.xpath.*;

import org.w3c.dom.*;

import com.google.code.configprocessor.*;
import com.google.code.configprocessor.expression.*;
import com.google.code.configprocessor.processing.*;

public abstract class AbstractXmlActionProcessingAdvisor implements XmlActionProcessingAdvisor {

	private Action action;
	private ExpressionResolver expressionResolver;
	private NamespaceContext namespaceContext;
	private List<ParserFeature> parserFeatures;
    private boolean failOnMissingXpath;
	
	private String textExpression;
	private XPathExpression xpathExpression;

	public AbstractXmlActionProcessingAdvisor(Action action, ExpressionResolver expressionResolver, NamespaceContext namespaceContext, List<ParserFeature> parserFeatures) {
		this(action, expressionResolver, namespaceContext, parserFeatures, true);
	}

    public AbstractXmlActionProcessingAdvisor(Action action, ExpressionResolver expressionResolver, NamespaceContext namespaceContext, List<ParserFeature> parserFeatures, boolean failOnMissingXpath) {
        this.action = action;
        this.expressionResolver = expressionResolver;
        this.namespaceContext = namespaceContext;
        this.parserFeatures = parserFeatures;
        this.failOnMissingXpath = failOnMissingXpath;
    }
	
	public Action getAction() {
		return action;
	}

	protected void compile(String expression) throws ParsingException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(namespaceContext);
		try {
			xpathExpression = xpath.compile(resolve(expression));
			textExpression = expression;
		} catch (XPathExpressionException e) {
			throw new ParsingException(e);
		}
	}

	protected Node evaluateForSingleNode(Document document, boolean orphanOK, boolean attributeOk) throws ParsingException {
		Node node = evaluateForNode(document);

		if (!orphanOK && node != null) {
			Node parent = node.getParentNode();
			if (parent == null) {
				throw new ParsingException("Cannot manipulate node without a parent");
			}
		}

		if (!attributeOk && (node instanceof Attr)) {
			throw new ParsingException("Expression resolved to attribute. It must resolve to node element: " + textExpression);
		}

		return node;
	}

	private Node evaluateForNode(Document document) throws ParsingException {
		try {
			Node node = (Node) getXPathExpression().evaluate(document, XPathConstants.NODE);

			if (node == null && failOnMissingXpath) {
				throw new ParsingException("XPath expression did not find node(s): " + textExpression);
			}

			return node;
		} catch (XPathExpressionException e) {
			throw new ParsingException(e);
		}
	}

	protected String resolve(String value) {
		return expressionResolver.resolve(value, false);
	}

	protected XPathExpression getXPathExpression() {
		return xpathExpression;
	}
	
	protected List<ParserFeature> getParserFeatures() {
		return parserFeatures;
	}
}
