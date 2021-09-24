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
package com.google.code.configprocessor.parsing;

import com.google.code.configprocessor.processing.*;
import com.thoughtworks.xstream.converters.*;
import com.thoughtworks.xstream.io.*;

public class AddActionConverter implements Converter {

	public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
		return AddAction.class.isAssignableFrom(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		throw new UnsupportedOperationException();
	}
	
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		AddAction action = new AddAction();

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			setValue(reader, context, action);
			reader.moveUp();
		}
		
		return action;
	}
	
	protected void setValue(HierarchicalStreamReader reader, UnmarshallingContext context, AddAction action) {
		String name = reader.getNodeName();
		
		if ("name".equals(name)) {
			action.setName(reader.getValue());
		} else if ("value".equals(name)) {
			action.setValue(reader.getValue());
		} else if ("strict".equals(name)) {
			action.setStrict(Boolean.valueOf(reader.getValue()));
		} else if ("first".equals(name)) {
			action.setFirst(true);
		} else if ("last".equals(name)) {
			action.setLast(true);
		} else if ("after".equals(name)) {
			action.setAfter(reader.getValue());
		} else if ("before".equals(name)) {
			action.setBefore(reader.getValue());
		} else if ("inside".equals(name)) {
			action.setInside(reader.getValue());
		} else if ("file".equals(name)) {
			if (reader.getAttributeCount() > 0) {
				action.setIgnoreRoot(Boolean.valueOf(reader.getAttribute("ignore-root")));
			}
			action.setFile(reader.getValue());
		} else if ("actions".equals(name)) {
			NestedAction nestedAction = (NestedAction)context.convertAnother(action, NestedAction.class);
			action.setNestedAction(nestedAction);
		}
	}

}
