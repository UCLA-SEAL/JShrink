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

import java.io.*;
import java.nio.charset.*;

import com.google.code.configprocessor.*;
import com.google.code.configprocessor.processing.*;
import com.thoughtworks.xstream.*;

public class ProcessingConfigurationParser {

	public NestedAction parse(InputStream is, Charset charset) throws ParsingException {
		if (is == null) {
			throw new NullPointerException("InputStream is null");
		}
		return parse(new InputStreamReader(is, charset));
	}

	public NestedAction parse(Reader is) throws ParsingException {
		XStream xstream = getXStream();
		try {
			return (NestedAction) xstream.fromXML(is);
		} catch (Exception e) {
			throw new ParsingException(e);
		}
	}

	protected XStream getXStream() {
		XStream xstream = new XStream();

		xstream.alias("processor", NestedAction.class);
		xstream.alias("add", AddAction.class);
		xstream.alias("modify", ModifyAction.class);
		xstream.alias("remove", RemoveAction.class);
		xstream.alias("comment", CommentAction.class);
		xstream.alias("uncomment", UncommentAction.class);
		xstream.addImplicitCollection(NestedAction.class, "actions");
		xstream.registerConverter(new AddActionConverter());

		return xstream;
	}
}
