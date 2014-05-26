package edu.iastate.cs.boa.conversion;

import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.util.Strings;

public class LONGValueConverter extends AbstractLexerBasedConverter<Long> {
	@Override
	protected String toEscapedString(Long value) {
		return value.toString();
	}
	
	public Long toValue(String string, INode node) {
		if (Strings.isEmpty(string))
			throw new ValueConverterException("Couldn't convert empty string to a long value.", node, null);

		try {
			// FIXME doesnt handle binary
			long val = Long.decode(string);
			return Long.valueOf(val);
		} catch (NumberFormatException e) {
			throw new ValueConverterException("Couldn't convert '" + string + "' to an long value.", node, e);
		}
	}
}
