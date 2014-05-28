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
package edu.iastate.cs.boa.ui.syntaxcoloring;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor;
import org.eclipse.xtext.ui.editor.utils.TextStyle;

/**
 * @author rdyer
 */
public class BoaHighlightingConfiguration extends DefaultHighlightingConfiguration {
	public static final String REGEX_ID = "regex";
	public static final String TIME_ID = "time";

	override configure(IHighlightingConfigurationAcceptor acceptor) {
		acceptor.acceptDefaultHighlighting(REGEX_ID, "Regular Expression", regexStyle())
		acceptor.acceptDefaultHighlighting(TIME_ID, "Time", timeStyle())

		super.configure(acceptor)
	}

	def TextStyle regexStyle() {
		var regexStyle = stringTextStyle().copy();
		regexStyle.setColor(new RGB(30, 144, 255));
		return regexStyle;
	}

	def TextStyle timeStyle() {
		var timeStyle = numberTextStyle().copy();
		timeStyle.setColor(new RGB(255, 165, 0));
		return timeStyle;
	}
}
