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

import static org.junit.Assert.*;

import java.io.*;
import java.nio.charset.*;

import org.junit.*;

import com.google.code.configprocessor.*;
import com.google.code.configprocessor.processing.*;
import com.thoughtworks.xstream.*;

public class ProcessingConfigurationParserTest {

	@Test
	public void parsingOk() throws Exception {
		InputStream is = getClass().getResourceAsStream("/com/google/code/configprocessor/data/xml-processing-configuration.xml");
		
		ProcessingConfigurationParser parser = new ProcessingConfigurationParser();
		NestedAction action = null;
		try {
			action = parser.parse(is, Charset.forName("UTF-8"));
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		assertEquals(10, action.getActions().size());
		assertEquals(new AddAction(null, "<test-property>test-value</test-property>", "/root/property3", null), action.getActions().get(0));
		assertEquals(new ModifyAction("/root/property1", "<modified-property1>modified-value</modified-property1>"), action.getActions().get(1));
		assertEquals(new RemoveAction("/root/property2"), action.getActions().get(2));
		assertEquals(new CommentAction("property-to-comment"), action.getActions().get(3));
		assertEquals(new UncommentAction("property-to-uncomment"), action.getActions().get(4));
		
		ModifyAction modifyFindReplace = new ModifyAction();
		modifyFindReplace.setFind("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]+");
		modifyFindReplace.setReplace("my-email@server.com");
		modifyFindReplace.setFlags("CASE_INSENSITIVE,COMMENTS");
		assertEquals(modifyFindReplace, action.getActions().get(5));
		
		assertEquals(new AddAction("${filename-to-resolve}", "last-property", null), action.getActions().get(6));
		
		AddAction addInsideAction = new AddAction();
		addInsideAction.setInside("/root/property4");
		addInsideAction.setFile("src/etc/my-file.xml");
		addInsideAction.setIgnoreRoot(false);
		assertEquals(addInsideAction, action.getActions().get(7));

		AddAction actionWithNestedActions = new AddAction();
		actionWithNestedActions.setInside("/root");
		actionWithNestedActions.setFile("src/assembly/file.xml");
		NestedAction nestedAction = new NestedAction();
		// Added a subNestedAction facet to the tests, while trying to pin down a bug with recursively nested actions.
		AddAction subNestedAction = buildSubNestedAction();
		nestedAction.addAction(subNestedAction);
		nestedAction.addAction(new ModifyAction("/tag/@att", "new-value"));
		nestedAction.addAction(new RemoveAction("/tag/nothing"));
		actionWithNestedActions.setNestedAction(nestedAction);
		assertEquals(actionWithNestedActions, action.getActions().get(8));

		AddAction firstAction = new AddAction("first-property", "value-first-property");
		firstAction.setFirst(true);
		assertEquals(firstAction, action.getActions().get(9));
	}

	private AddAction buildSubNestedAction() {
		AddAction nestedActionWithNestedActions = new AddAction();
		nestedActionWithNestedActions.setInside( "/sub-root" );
		nestedActionWithNestedActions.setFile( "src/assemble/sub-file.xml" );
		NestedAction nestedNestedAction = new NestedAction();
		nestedNestedAction.addAction(new ModifyAction("/sub-tag/@att", "new-value"));
		nestedNestedAction.addAction(new RemoveAction("/sub-tag/nothing"));
		nestedActionWithNestedActions.setNestedAction( nestedNestedAction );
		return nestedActionWithNestedActions;
	}

	@Test(expected = NullPointerException.class)
	public void parsingInexistentInput() throws Exception {
		ProcessingConfigurationParser parser = new ProcessingConfigurationParser();
		parser.parse(getClass().getResourceAsStream("inexistent"), Charset.forName("UTF-8"));
	}

	@Test(expected = ParsingException.class)
	public void parsingInvalidInput() throws Exception {
		ProcessingConfigurationParser parser = new ProcessingConfigurationParser();
		parser.parse(getClass().getResourceAsStream("/com/google/code/configprocessor/data/xml-target-config.xml"), Charset.forName("UTF-8"));
	}
	
	@Test
	@Ignore
	public void generationExample() {
		XStream xstream = new ProcessingConfigurationParser().getXStream();
		
		NestedAction config = new NestedAction();
		config.addAction(new AddAction("/root", "<property5 attribute=\"value5\"></property3>", "/root/property2", null));
		config.addAction(new ModifyAction("/root/property1", "new-value1"));
		config.addAction(new ModifyAction("/root/property4", ""));
		config.addAction(new RemoveAction("/root/property3"));
		config.addAction(new RemoveAction("/root/property4[@attribute]"));
		config.addAction(new CommentAction("property-to-comment"));
		config.addAction(new UncommentAction("property-to-uncomment"));

		ModifyAction modifyFindReplace = new ModifyAction();
		modifyFindReplace.setFind("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]+");
		modifyFindReplace.setReplace("my-email@server.com");
		modifyFindReplace.setFlags("CASE_INSENSITIVE,COMMENTS");
		config.addAction(modifyFindReplace);
		
		config.addAction(new AddAction("${filename-to-resolve}", "last-property", null));

		AddAction addInsideAction = new AddAction();
		addInsideAction.setInside("/root/property4");
		addInsideAction.setFile("src/etc/my-file.xml");
		config.addAction(addInsideAction);

		System.out.println(xstream.toXML(config));
	}
}
