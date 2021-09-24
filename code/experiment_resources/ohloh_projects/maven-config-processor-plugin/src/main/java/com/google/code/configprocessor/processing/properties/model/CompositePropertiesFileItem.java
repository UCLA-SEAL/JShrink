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
package com.google.code.configprocessor.processing.properties.model;

import java.util.*;

import com.google.code.configprocessor.processing.properties.*;

public class CompositePropertiesFileItem implements PropertiesFileItem {

	private List<PropertiesFileItem> nestedItems;

	public CompositePropertiesFileItem() {
		nestedItems = new ArrayList<PropertiesFileItem>();
	}

	public String getAsText() {
		StringBuilder sb = new StringBuilder();

		Iterator<PropertiesFileItem> it = nestedItems.iterator();
		while (it.hasNext()) {
			PropertiesFileItem item = it.next();
			sb.append(item.getAsText());

			if (it.hasNext()) {
				sb.append(PropertiesActionProcessor.LINE_SEPARATOR);
			}
		}

		return sb.toString();
	}

	public void addPropertiesFileItem(PropertiesFileItem item) {
		nestedItems.add(item);
	}

	public void addAllPropertiesFileItems(List<PropertiesFileItem> items) {
		nestedItems.addAll(items);
	}

	public void removePropertiesFileItem(PropertiesFileItem item) {
		nestedItems.remove(item);
	}

	public List<PropertiesFileItem> getNestedItems() {
		return nestedItems;
	}

	@Override
	public String toString() {
		return "Composite:\n" + nestedItems;
	}
}
