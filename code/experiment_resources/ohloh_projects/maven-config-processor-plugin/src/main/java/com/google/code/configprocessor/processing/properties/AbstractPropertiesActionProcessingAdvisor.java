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
import com.google.code.configprocessor.processing.properties.model.*;

public abstract class AbstractPropertiesActionProcessingAdvisor implements PropertiesActionProcessingAdvisor {

	private ExpressionResolver expressionResolver;

	public AbstractPropertiesActionProcessingAdvisor(ExpressionResolver expressionResolver) {
		this.expressionResolver = expressionResolver;
	}

	/**
	 * Default implementation to indicate do nothing.
	 */
	public PropertiesFileItemAdvice onStartProcessing() {
		return new PropertiesFileItemAdvice(PropertiesFileItemAdviceType.DO_NOTHING, null);
	}

	/**
	 * Default implementation to indicate do nothing.
	 */
	public PropertiesFileItemAdvice process(PropertiesFileItem item) {
		return new PropertiesFileItemAdvice(PropertiesFileItemAdviceType.DO_NOTHING, null);
	}

	/**
	 * Default implementation to indicate do nothing.
	 */
	public PropertiesFileItemAdvice onEndProcessing() {
		return new PropertiesFileItemAdvice(PropertiesFileItemAdviceType.DO_NOTHING, null);
	}

	protected PropertyMapping createPropertyMapping(String name, String value) {
		return new PropertyMapping(name, resolve(value));
	}

	protected String resolve(String value) {
		return expressionResolver.resolve(value, true);
	}
}
