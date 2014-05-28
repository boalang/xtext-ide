/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
 *                 and Iowa State University of Science and Technology
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

import org.eclipse.xtext.conversion.impl.STRINGValueConverter;
import org.eclipse.xtext.nodemodel.INode;

/**
 * @author rdyer
 */
public class REGEXValueConverter extends STRINGValueConverter {
	@Override
	protected String toEscapedString(String value) {
		// TODO is this right?
		return '`' + value + '`';
	}
	
	public String toValue(String string, INode node) {
		if (string == null)
			return null;
		return super.toValue("\"" + string.substring(1, string.length() - 1).replace("\\", "\\\\") + "\"", node);
	}
}
