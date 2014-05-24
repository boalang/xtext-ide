package edu.iastate.cs.boa.ui.syntaxcoloring;

import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultAntlrTokenToAttributeIdMapper;

public class BoaAntlrTokenToAttributeIdMapper extends DefaultAntlrTokenToAttributeIdMapper {
	override protected calculateId(String tokenName, int tokenType) {
		if ("RULE_REGEX".equals(tokenName))
			return BoaHighlightingConfiguration.REGEX_ID

		super.calculateId(tokenName, tokenType)
	}
}
