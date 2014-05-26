package edu.iastate.cs.boa.ui.syntaxcoloring;

import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultAntlrTokenToAttributeIdMapper;

public class BoaAntlrTokenToAttributeIdMapper extends DefaultAntlrTokenToAttributeIdMapper {
	override protected calculateId(String tokenName, int tokenType) {
		if ("RULE_INTEGER_LIT".equals(tokenName))
			return BoaHighlightingConfiguration.NUMBER_ID
		if ("RULE_FLOATING_LIT".equals(tokenName))
			return BoaHighlightingConfiguration.NUMBER_ID
		if ("RULE_STRING_LIT".equals(tokenName))
			return BoaHighlightingConfiguration.STRING_ID
		if ("RULE_REGEX_LIT".equals(tokenName))
			return BoaHighlightingConfiguration.REGEX_ID
		if ("RULE_CHARACTER_LIT".equals(tokenName))
			return BoaHighlightingConfiguration.STRING_ID
		if ("RULE_TIME_LIT".equals(tokenName))
			return BoaHighlightingConfiguration.TIME_ID

		super.calculateId(tokenName, tokenType)
	}
}
