/*
 * Copyright 2015, Hridesh Rajan, Robert Dyer, 
 *                 Iowa State University of Science and Technology,
 *                 and Bowling Green State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.iastate.cs.boa;

import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.scoping.IScopeProvider;

import edu.iastate.cs.boa.conversion.BoaValueConverterService;
import edu.iastate.cs.boa.scoping.BoaScopeProvider;

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 *
 * @author rdyer
 */
public class BoaRuntimeModule extends edu.iastate.cs.boa.AbstractBoaRuntimeModule {
	@Override
	public Class<? extends IValueConverterService> bindIValueConverterService() {
		return BoaValueConverterService.class;
	}

	@Override
	public Class<? extends IScopeProvider> bindIScopeProvider() {
		return BoaScopeProvider.class;
	}
}