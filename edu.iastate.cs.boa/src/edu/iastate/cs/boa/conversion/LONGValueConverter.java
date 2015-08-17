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

import java.math.BigInteger;

import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.util.Strings;

/**
 * @author rdyer
 */
public class LONGValueConverter extends AbstractLexerBasedConverter<Long> {
	@Override
	protected String toEscapedString(Long value) {
		return value.toString();
	}

	public Long toValue(String string, INode node) {
		if (Strings.isEmpty(string))
			throw new ValueConverterException("Couldn't convert empty string to a long value.", node, null);

		try {
			if (string.length() > 2 && string.charAt(0) == '0' && (string.charAt(1) == 'b' || string.charAt(1) == 'B'))
				return Long.valueOf(new BigInteger(string.substring(2), 2).longValue());

			return Long.valueOf(Long.decode(string));
		} catch (NumberFormatException e) {
			throw new ValueConverterException("Couldn't convert '" + string + "' to a long value.", node, e);
		}
	}
}