package edu.iastate.cs.boa.ui.syntaxcoloring;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor;
import org.eclipse.xtext.ui.editor.utils.TextStyle;

public class BoaHighlightingConfiguration extends DefaultHighlightingConfiguration {
	public static final String REGEX_ID = "regex";

	override configure(IHighlightingConfigurationAcceptor acceptor) {
		acceptor.acceptDefaultHighlighting(REGEX_ID, "Regular Expressions", regexStyle())

		super.configure(acceptor)
	}

	def TextStyle regexStyle() {
		var regexStyle = stringTextStyle().copy();
		regexStyle.setColor(new RGB(30, 144, 255));
		return regexStyle;
	}
}
