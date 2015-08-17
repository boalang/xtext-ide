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
package edu.iastate.cs.boa.conversion;

import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;
import org.eclipse.xtext.conversion.impl.AbstractDeclarativeValueConverterService;
import org.eclipse.xtext.conversion.impl.AbstractIDValueConverter;
import org.eclipse.xtext.conversion.impl.STRINGValueConverter;

import com.google.inject.Inject;

/**
 * @author rdyer
 */
public class BoaValueConverterService extends AbstractDeclarativeValueConverterService {
	@Inject
	private AbstractIDValueConverter idValueConverter;

	@ValueConverter(rule = "ID")
	public IValueConverter<String> ID() {
		return idValueConverter;
	}

	@Inject
	private LONGValueConverter longValueConverter;

	@ValueConverter(rule = "INTEGER_LIT")
	public IValueConverter<Long> INTEGER_LIT() {
		return longValueConverter;
	}

	@Inject
	private STRINGValueConverter stringValueConverter;

	@ValueConverter(rule = "STRING_LIT")
	public IValueConverter<String> STRING_LIT() {
		return stringValueConverter;
	}

	@Inject
	private REGEXValueConverter regexValueConverter;

	@ValueConverter(rule = "REGEX_LIT")
	public IValueConverter<String> REGEX_LIT() {
		return regexValueConverter;
	}

	@Inject
	private TIMEValueConverter timeValueConverter;

	@ValueConverter(rule = "TIME_LIT")
	public IValueConverter<Long> TIME_LIT() {
		return timeValueConverter;
	}
}