package edu.iastate.cs.boa.conversion;

import java.math.BigInteger;

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
			if (string.length() > 2 && string.charAt(0) == '0' && (string.charAt(1) == 'b' || string.charAt(1) == 'B'))
				return Long.valueOf(new BigInteger(string.substring(2), 2).longValue());

			return Long.valueOf(Long.decode(string));
		} catch (NumberFormatException e) {
			throw new ValueConverterException("Couldn't convert '" + string + "' to a long value.", node, e);
		}
	}
}
