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

import java.util.regex.*;

import com.google.code.configprocessor.expression.*;
import com.google.code.configprocessor.processing.*;
import com.google.code.configprocessor.processing.properties.model.*;

public class PropertiesModifyActionProcessingAdvisor extends AbstractPropertiesActionProcessingAdvisor {

	private ModifyAction action;
	private Pattern pattern;
	private String replace;

	public PropertiesModifyActionProcessingAdvisor(ModifyAction action, ExpressionResolver expressionResolver) {
		super(expressionResolver);
		this.action = action;
		if (action.getFind() != null) {
			pattern = action.getPattern();
			replace = resolve(action.getReplace());
		}
	}

	@Override
	public PropertiesFileItemAdvice process(PropertiesFileItem item) {
		if (item instanceof PropertyMapping) {
			PropertyMapping mapping = (PropertyMapping) item;

			if (mapping.getPropertyName().trim().equals(action.getName())) {
				PropertyMapping aux = createPropertyMapping(mapping.getPropertyName(), action.getValue());
				return new PropertiesFileItemAdvice(PropertiesFileItemAdviceType.MODIFY, aux);
			}

			if (pattern != null && mapping.getPropertyValue() != null) {
				Matcher matcher = pattern.matcher(mapping.getPropertyValue());
				String newValue = matcher.replaceAll(replace);
				if (!newValue.equals(mapping.getPropertyValue())) {
					PropertyMapping aux = createPropertyMapping(mapping.getPropertyName(), newValue);
					return new PropertiesFileItemAdvice(PropertiesFileItemAdviceType.MODIFY, aux);
				}
			}
		}
		
		if (pattern != null && item instanceof Comment) {
			Comment comment = (Comment) item;
			if (comment.getAsText() != null) {
				Matcher matcher = pattern.matcher(comment.getAsText());
				String newValue = matcher.replaceAll(replace);
				if (!newValue.equals(comment.getAsText())) {
					Comment aux = new Comment(newValue);
					return new PropertiesFileItemAdvice(PropertiesFileItemAdviceType.MODIFY, aux);
				}
			}
		}

		return super.process(item);
	}

}
