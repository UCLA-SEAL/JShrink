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

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.apache.xml.serialize.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import com.google.code.configprocessor.*;

public class XmlHelper {

	public static final String NODE_START = "<";
	public static final String NODE_END = ">";
	public static final String CLOSING_NODE_START = "</";
	public static final String CLOSING_NODE_END = NODE_END;

	public static final String ROOT_TAG = "root";
	public static final String PROCESSOR_TAG = "processor";
	public static final String ROOT_PROCESSOR_START = NODE_START + PROCESSOR_TAG + NODE_END;
	public static final String ROOT_PROCESSOR_END = CLOSING_NODE_START + PROCESSOR_TAG + CLOSING_NODE_END;

	public static Document parse(String text, boolean prefixAndSuffix, List<ParserFeature> features) throws SAXException, ParserConfigurationException {
		String textToParse;

		if (prefixAndSuffix) {
			StringBuilder sb = new StringBuilder();
			sb.append(NODE_START).append(ROOT_TAG).append(NODE_END);
			sb.append(text);
			sb.append(CLOSING_NODE_START).append(ROOT_TAG).append(CLOSING_NODE_END);

			textToParse = sb.toString();
		} else {
			textToParse = text;
		}

		try {
			return newDocumentBuilder(features).parse(new InputSource(new StringReader(textToParse)));
		} catch (IOException e) {
			// Should never happen
			throw new RuntimeException(e);
		}
	}

	public static Document parse(Reader reader, List<ParserFeature> features) throws SAXException, ParserConfigurationException {
		try {
			return newDocumentBuilder(features).parse(new InputSource(reader));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Document parse(InputStream is, List<ParserFeature> features) throws SAXException, ParserConfigurationException {
		try {
			return newDocumentBuilder(features).parse(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<Attr> parseAttributes(String text, List<ParserFeature> features) throws SAXException, ParserConfigurationException {
		StringBuilder sb = new StringBuilder();

		sb.append(NODE_START).append(ROOT_TAG).append(" ");
		sb.append(text);
		sb.append(NODE_END);
		sb.append(CLOSING_NODE_START).append(ROOT_TAG).append(CLOSING_NODE_END);

		try {
			Document document = newDocumentBuilder(features).parse(new InputSource(new StringReader(sb.toString())));
			NamedNodeMap nodeMap = document.getFirstChild().getAttributes();
			List<Attr> attributes = new ArrayList<Attr>();

			for (int i = 0; i < nodeMap.getLength(); i++) {
				attributes.add((Attr) nodeMap.item(i));
			}

			return attributes;
		} catch (IOException e) {
			// Should never happen
			throw new RuntimeException(e);
		}
	}

	public static String write(Document document, String encoding, int lineWidth, int indentSize) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write(new OutputStreamWriter(baos), document, encoding, lineWidth, indentSize);

		try {
			return new String(baos.toByteArray(), encoding);
		} catch (UnsupportedEncodingException e) {
			// Should never happen
			throw new RuntimeException(e);
		}
	}

	public static void write(Writer writer, Document document, String encoding, int lineWidth, int indentSize) {
		OutputFormat format = new OutputFormat(document, encoding, true);
		format.setLineSeparator(XmlActionProcessor.LINE_SEPARATOR);
		format.setLineWidth(lineWidth);
		format.setIndent(indentSize);
		XMLSerializer serializer = new XMLSerializer(writer, format);
		try {
			serializer.serialize(document);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean representsNodeElement(String fragment) {
		return fragment.startsWith(NODE_START);
	}

	private static DocumentBuilder newDocumentBuilder(List<ParserFeature> features) throws ParserConfigurationException {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		
		for (ParserFeature feature : features) {
			domFactory.setFeature(feature.getName(), feature.getValue());
		}
		
		return domFactory.newDocumentBuilder();
	}
}
