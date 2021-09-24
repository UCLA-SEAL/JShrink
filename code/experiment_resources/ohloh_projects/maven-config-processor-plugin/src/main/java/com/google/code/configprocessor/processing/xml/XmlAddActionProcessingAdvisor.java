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
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.google.code.configprocessor.*;
import com.google.code.configprocessor.expression.*;
import com.google.code.configprocessor.processing.*;

public class XmlAddActionProcessingAdvisor extends AbstractXmlActionProcessingAdvisor {

	private AddAction action;
	private String textFragment;
	private boolean prefixAndSuffixTextFragment;

    public XmlAddActionProcessingAdvisor(AddAction action,
                                         String fileContent,
                                         ExpressionResolver expressionResolver,
                                         NamespaceContext namespaceContext,
                                         List<ParserFeature> parserFeatures) throws ParsingException {
        this(action, fileContent, expressionResolver, namespaceContext, parserFeatures, true);
    }

	public XmlAddActionProcessingAdvisor(AddAction action,
	                                     String fileContent,
	                                     ExpressionResolver expressionResolver,
	                                     NamespaceContext namespaceContext,
	                                     List<ParserFeature> parserFeatures,
                                         boolean failOnMissingXpath) throws ParsingException {
		super(action, expressionResolver, namespaceContext, parserFeatures, failOnMissingXpath);

		this.action = action;
		if (fileContent == null) {
			this.textFragment = resolve(action.getValue());
			// The call to modify node later on will throw a NullPointerException if textFragment resolves to null. 
			// It's easier for a user to track down the problem with a explanatory exception message.
			if (textFragment == null) {
				throw new ParsingException(String.format( "Action value %s resolved to null on lookup.", action.getValue())); 
			}
			this.prefixAndSuffixTextFragment = true;
		} else {
			this.textFragment = fileContent;
			this.prefixAndSuffixTextFragment = false;
		}

		if (action.getBefore() != null) {
			compile(action.getBefore());
		} else if (action.getAfter() != null) {
			compile(action.getAfter());
		} else if (action.getInside() != null) {
			compile(action.getInside());
		} else {
			if (XmlHelper.representsNodeElement(textFragment)) {
				throw new ParsingException("Add action must specify [before], [after] or [inside] attribute");
			}
			if (action.getName() == null) {
				throw new ParsingException("Add action must specify [name] when appending attributes");
			}
			compile(action.getName());
		}
	}

	public void process(Document document) throws ParsingException {
		if (XmlHelper.representsNodeElement(textFragment)) {
			Node node = evaluateForSingleNode(document, false, false);
			if (node != null) addNode(document, node);
		} else {
			Node node = evaluateForSingleNode(document, true, false);
            if (node != null) addAttribute(document, node);
		}
	}

	protected void addNode(Document document, Node node) throws ParsingException {
		Node parent;

		try {
			Document fragment = XmlHelper.parse(textFragment, prefixAndSuffixTextFragment, getParserFeatures());

			Node referenceNode;
			if (action.getBefore() != null) {
				parent = node.getParentNode();
				referenceNode = node;
			} else if (action.getAfter() != null) {
				parent = node.getParentNode();
				referenceNode = node.getNextSibling();
				if (referenceNode == null) {
					referenceNode = node;
				}
			} else if (action.getInside() != null) {
				parent = node;
				referenceNode = node;
			} else {
				throw new ParsingException("Unknown add action");
			}

			NodeList nodeList;
			if (action.isIgnoreRoot()) {
				nodeList = fragment.getFirstChild().getChildNodes();
			} else {
				nodeList = fragment.getChildNodes();
			}
			
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node importedNode = document.importNode(nodeList.item(i), true);
				if (action.getInside() == null) {
					parent.insertBefore(importedNode, referenceNode);
				} else {
					parent.appendChild(importedNode);
				}
			}
		} catch (SAXException e) {
			throw new ParsingException(e);
		} catch (ParserConfigurationException e) {
			throw new ParsingException(e);
		}
	}

	protected void addAttribute(Document document, Node node) throws ParsingException {
		try {
			List<Attr> attributes = XmlHelper.parseAttributes(textFragment, getParserFeatures());

			NamedNodeMap nodeMap = node.getAttributes();
			for (Attr attr : attributes) {
				Attr importedAttr = (Attr) document.importNode(attr, false);
				nodeMap.setNamedItemNS(importedAttr);
			}
		} catch (SAXException e) {
			throw new ParsingException(e);
		} catch (ParserConfigurationException e) {
			throw new ParsingException(e);
		}
	}
}
