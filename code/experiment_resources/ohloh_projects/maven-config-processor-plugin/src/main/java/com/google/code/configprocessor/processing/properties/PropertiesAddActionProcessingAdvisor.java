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

import com.google.code.configprocessor.expression.*;
import com.google.code.configprocessor.processing.*;
import com.google.code.configprocessor.processing.properties.model.*;

public class PropertiesAddActionProcessingAdvisor extends AbstractPropertiesActionProcessingAdvisor {

	private AddAction action;

	public PropertiesAddActionProcessingAdvisor(AddAction action, ExpressionResolver expressionResolver) {
		super(expressionResolver);
		this.action = action;
	}

	@Override
	public PropertiesFileItemAdvice onStartProcessing() {
		if (action.isFirst()) {
			if (action.getFile() == null) {
				return new PropertiesFileItemAdvice(PropertiesFileItemAdviceType.ADD_BEFORE, createPropertyMapping(action.getName(), action.getValue()));
			} else {
				return new PropertiesFileItemAdvice(PropertiesFileItemAdviceType.APPEND_FILE_BEFORE, new FilePropertiesFileItem(resolve(action.getFile())));
			}
		}
		return super.onStartProcessing();
	}

	@Override
	public PropertiesFileItemAdvice process(PropertiesFileItem item) {
		if (item instanceof PropertyMapping) {
			PropertyMapping mapping = (PropertyMapping) item;

			if (mapping.getPropertyName().trim().equals(action.getBefore()) || mapping.getPropertyName().trim().equals(action.getAfter())) {

				PropertiesFileItemAdvice advice;
				if (action.getFile() == null) {
					PropertyMapping aux = createPropertyMapping(action.getName(), action.getValue());
					if (mapping.getPropertyName().trim().equals(action.getBefore())) {
						advice = new PropertiesFileItemAdvice(PropertiesFileItemAdviceType.ADD_BEFORE, aux);
					} else {
						advice = new PropertiesFileItemAdvice(PropertiesFileItemAdviceType.ADD_AFTER, aux);
					}
				} else {
					FilePropertiesFileItem aux = new FilePropertiesFileItem(resolve(action.getFile()));
					if (mapping.getPropertyName().trim().equals(action.getBefore())) {
						advice = new PropertiesFileItemAdvice(PropertiesFileItemAdviceType.APPEND_FILE_BEFORE, aux);
					} else {
						advice = new PropertiesFileItemAdvice(PropertiesFileItemAdviceType.APPEND_FILE_AFTER, aux);
					}
				}

				return advice;
			}
		}

		return super.process(item);
	}

	@Override
	public PropertiesFileItemAdvice onEndProcessing() {
		if (action.isLast()) {
			if (action.getFile() == null) {
				return new PropertiesFileItemAdvice(PropertiesFileItemAdviceType.ADD_AFTER, createPropertyMapping(action.getName(), action.getValue()));
			} else {
				return new PropertiesFileItemAdvice(PropertiesFileItemAdviceType.APPEND_FILE_AFTER, new FilePropertiesFileItem(resolve(action.getFile())));
			}
		}
		return super.onEndProcessing();
	}
}
