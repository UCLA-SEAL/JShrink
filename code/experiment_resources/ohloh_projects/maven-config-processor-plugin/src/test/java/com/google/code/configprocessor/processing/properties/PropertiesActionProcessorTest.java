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
package com.google.code.configprocessor.processing.properties;

import static com.google.code.configprocessor.processing.properties.PropertiesActionProcessor.*;
import static org.easymock.EasyMock.*;

import java.io.*;

import org.codehaus.plexus.component.configurator.expression.*;
import org.junit.*;

import com.google.code.configprocessor.io.*;
import com.google.code.configprocessor.maven.*;
import com.google.code.configprocessor.processing.*;
import com.google.code.configprocessor.processing.properties.model.*;

public class PropertiesActionProcessorTest {

	public static final String ENCODING = "ISO-8859-1";
	public static final String PROPERTIES_PATH = "/com/google/code/configprocessor/data/properties-target-config.properties";

	private PropertiesActionProcessingAdvisor advisor;
	
	@Test
	public void testProcess() throws Exception {
		advisor = createStrictMock(PropertiesActionProcessingAdvisor.class);
		
		expect(advisor.onStartProcessing()).andReturn(createDoNothingAdvice());
		expect(advisor.process(new PropertyMapping("property1.value", "value1"))).andReturn(createDoNothingAdvice());
		expect(advisor.process(new PropertyMapping("property2.value", null))).andReturn(createDoNothingAdvice());
		expect(advisor.process(new Comment("# Comment"))).andReturn(createDoNothingAdvice());
		expect(advisor.process(new PropertyMapping("	property3.value", "value3 \\" + LINE_SEPARATOR + "value 3 continuation"))).andReturn(createDoNothingAdvice());
		expect(advisor.process(new Comment("# property4.value=value4 \\" + LINE_SEPARATOR + "#value 4 continuation"))).andReturn(createDoNothingAdvice());
		expect(advisor.process(new Comment("#property5.value=value5"))).andReturn(createDoNothingAdvice());
		expect(advisor.process(new PropertyMapping("property6.value", "value6"))).andReturn(createDoNothingAdvice());
		expect(advisor.onEndProcessing()).andReturn(createDoNothingAdvice());
		
		replay(advisor);
		
		PropertiesActionProcessor processor = new TestPropertiesActionProcessor();
		InputStream input = getClass().getResourceAsStream(PROPERTIES_PATH);
		OutputStream output = new ByteArrayOutputStream();
		
		processor.process(new InputStreamReader(input), new OutputStreamWriter(output), null);
		
		verify(advisor);
	}
	
	protected PropertiesFileItemAdvice createDoNothingAdvice() {
		return new PropertiesFileItemAdvice(PropertiesFileItemAdviceType.DO_NOTHING, null);
	}
	
	class TestPropertiesActionProcessor extends PropertiesActionProcessor {
		
		public TestPropertiesActionProcessor() {
			super(ENCODING, new ClasspathFileResolver(), new MavenExpressionResolver(new DefaultExpressionEvaluator()));
		}
		
		@Override
		protected PropertiesActionProcessingAdvisor getAdvisorFor(Action action) {
			return advisor;
		}
	}
}
