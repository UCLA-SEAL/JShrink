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

public class PropertiesRemoveActionProcessingAdvisor extends AbstractPropertiesActionProcessingAdvisor {

	private RemoveAction action;

	public PropertiesRemoveActionProcessingAdvisor(RemoveAction action, ExpressionResolver expressionResolver) {
		super(expressionResolver);
		this.action = action;
	}

	@Override
	public PropertiesFileItemAdvice process(PropertiesFileItem item) {
		if (item instanceof PropertyMapping) {
			PropertyMapping mapping = (PropertyMapping) item;

			if (mapping.getPropertyName().trim().equals(action.getName())) {
				return new PropertiesFileItemAdvice(PropertiesFileItemAdviceType.REMOVE, null);
			}
		}

		return super.process(item);
	}

}
