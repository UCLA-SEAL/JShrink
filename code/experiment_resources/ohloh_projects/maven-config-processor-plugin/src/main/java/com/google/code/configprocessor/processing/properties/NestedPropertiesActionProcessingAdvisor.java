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

import java.util.*;

import com.google.code.configprocessor.processing.properties.model.*;

public class NestedPropertiesActionProcessingAdvisor extends AbstractPropertiesActionProcessingAdvisor {

	private List<PropertiesActionProcessingAdvisor> advisors;

	public NestedPropertiesActionProcessingAdvisor(List<PropertiesActionProcessingAdvisor> advisors) {
		super(null);
		this.advisors = advisors;
	}

	@Override
	public PropertiesFileItemAdvice onStartProcessing() {
		NestedPropertiesFileItemAdvice advice = new NestedPropertiesFileItemAdvice(null);

		for (PropertiesActionProcessingAdvisor advisor : advisors) {
			PropertiesFileItemAdvice aux = advisor.onStartProcessing();
			advice.addAdvice(aux);
		}

		return advice;
	}

	@Override
	public PropertiesFileItemAdvice process(PropertiesFileItem item) {
		NestedPropertiesFileItemAdvice advice = new NestedPropertiesFileItemAdvice(item);

		PropertiesFileItem currentItem = item;
		for (PropertiesActionProcessingAdvisor advisor : advisors) {
			PropertiesFileItemAdvice aux = advisor.process(currentItem);
			advice.addAdvice(aux);
			if (aux.getType() == PropertiesFileItemAdviceType.MODIFY) {
				currentItem = aux.getItem();
			}
		}

		return advice;
	}

	@Override
	public PropertiesFileItemAdvice onEndProcessing() {
		NestedPropertiesFileItemAdvice advice = new NestedPropertiesFileItemAdvice(null);

		for (PropertiesActionProcessingAdvisor advisor : advisors) {
			PropertiesFileItemAdvice aux = advisor.onEndProcessing();
			advice.addAdvice(aux);
		}

		return advice;
	}

}
