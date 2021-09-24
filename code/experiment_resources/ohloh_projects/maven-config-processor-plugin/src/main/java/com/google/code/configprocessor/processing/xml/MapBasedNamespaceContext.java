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

public class MapBasedNamespaceContext implements NamespaceContext {

	private Map<String, String> mappings;

	public MapBasedNamespaceContext(Map<String, String> mappings) {
		this.mappings = new HashMap<String, String>();
		if (mappings != null) {
			this.mappings.putAll(mappings);
		}
	}

	public String getNamespaceURI(String prefix) {
		return mappings.get(prefix);
	}

	/**
	 * Not used for XPath processing
	 */
	public String getPrefix(String namespaceURI) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not used for XPath processing
	 */
	@SuppressWarnings({ "rawtypes" })
	public Iterator getPrefixes(String namespaceURI) {
		throw new UnsupportedOperationException();
	}
}
