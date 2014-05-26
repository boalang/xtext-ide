package edu.iastate.cs.boa.conversion;

import org.eclipse.xtext.conversion.impl.STRINGValueConverter;
import org.eclipse.xtext.nodemodel.INode;

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
