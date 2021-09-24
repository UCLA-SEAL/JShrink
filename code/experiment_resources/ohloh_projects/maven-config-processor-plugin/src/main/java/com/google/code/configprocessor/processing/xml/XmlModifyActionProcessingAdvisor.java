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
import java.util.regex.*;

import javax.xml.namespace.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.google.code.configprocessor.*;
import com.google.code.configprocessor.expression.*;
import com.google.code.configprocessor.processing.*;

public class XmlModifyActionProcessingAdvisor extends AbstractXmlActionProcessingAdvisor {

	private String textFragment;
	private Pattern pattern;
	private String replace;

    public XmlModifyActionProcessingAdvisor(ModifyAction action, ExpressionResolver expressionResolver, NamespaceContext namespaceContext, List<ParserFeature> parserFeatures) throws ParsingException {
        this (action, expressionResolver, namespaceContext, parserFeatures, true);
    }
	public XmlModifyActionProcessingAdvisor(ModifyAction action, ExpressionResolver expressionResolver, NamespaceContext namespaceContext, List<ParserFeature> parserFeatures, boolean failOnMissingXpath) throws ParsingException {
		super(action, expressionResolver, namespaceContext, parserFeatures, failOnMissingXpath);

		if (action.getName() != null) {
			compile(action.getName());
			textFragment = resolve(action.getValue());
			// The call to modify node later on will throw a NullPointerException if textFragment resolves to null. 
			// It's easier for a user to track down the problem with a explanatory exception message.
			if (textFragment == null && action.getValue() != null) {
				throw new ParsingException(String.format("Action value %s resolved to null on lookup.", action.getValue())); 
			}
		}
		if (action.getFind() != null) {
			pattern = action.getPattern();
			replace = resolve(action.getReplace());
		}
	}

	public void process(Document document) throws ParsingException {
		try {
			if (pattern == null) {
				Node node = evaluateForSingleNode(document, true, true);

				if (node instanceof Attr) {
					modifyAttribute(document, (Attr) node);
				} else if (node != null){
					modifyNode(document, node);
				}
			} else {
				NodeList nodes = document.getChildNodes();
				findReplace(nodes);
			}
		} catch (SAXException e) {
			throw new ParsingException(e);
		} catch (ParserConfigurationException e) {
			throw new ParsingException(e);
		}
	}

	protected void modifyNode(Document document, Node oldNode) throws SAXException, ParserConfigurationException {
		Document fragment = XmlHelper.parse(textFragment, false, getParserFeatures());
		Node parent = oldNode.getParentNode();
		Node importedNode = document.importNode(fragment.getDocumentElement(), true);
		parent.replaceChild(importedNode, oldNode);
	}

	protected void modifyAttribute(Document document, Attr oldAttr) {
		oldAttr.setValue(textFragment);
	}
	
	protected void findReplace(NodeList nodeList) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			findReplaceOnNode(node);
			NamedNodeMap map = node.getAttributes();
			if (map != null) {
				for (int j = 0; j < map.getLength(); j++) {
					Node attributeNode = map.item(j);
					findReplaceOnNode(attributeNode);
					findReplace(attributeNode.getChildNodes());
				}
			}
			findReplace(node.getChildNodes());
		}
	}
	
	protected void findReplaceOnNode(Node node) {
		String value = node.getNodeValue();
		if (value != null) {
			Matcher matcher = pattern.matcher(value);
			String newValue = matcher.replaceAll(replace);
			node.setNodeValue(newValue);
		}
	}
}
