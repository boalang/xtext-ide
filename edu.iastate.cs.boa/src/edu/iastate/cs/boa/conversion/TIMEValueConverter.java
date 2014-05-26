package edu.iastate.cs.boa.conversion;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.util.Strings;

import com.google.inject.Inject;

public class TIMEValueConverter extends AbstractLexerBasedConverter<Long> {
	final DateFormat deafultdf = new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy");

	@Inject
	private LONGValueConverter longValueConverter;

	public TIMEValueConverter() {
		super();
	}
	
	@Override
	protected String toEscapedString(Long value) {
		return value.toString() + "T";
	}
	
	public Long toValue(String string, INode node) {
		if (Strings.isEmpty(string))
			throw new ValueConverterException("Couldn't convert empty string to a time value.", node, null);

		if (!string.startsWith("T")) {
			final String s = string.substring(0, string.length() - 1);
			try {
				final Long val = longValueConverter.toValue(s, node);
				if (val < 0)
					throw new ValueConverterException("Couldn't convert negative number '" + s + "' to a time value.", node, null);
				return val;
			} catch (NumberFormatException e) {
				throw new ValueConverterException("Couldn't convert '" + s + "' to a time value.", node, e);
			}
		}

		final String s = string.substring(2, string.length() - 1);

		// first try a standard format
		try {
			return deafultdf.parse(s).getTime() * 1000L;
		} catch (Exception e) { }

		// then try every possible combination of built in formats
		final int [] formats = new int[] {DateFormat.DEFAULT, DateFormat.FULL, DateFormat.SHORT, DateFormat.LONG, DateFormat.MEDIUM};
		for (final int f : formats)
			for (final int f2 : formats)
				try {
					return DateFormat.getDateTimeInstance(f, f2).parse(s).getTime() * 1000L;
				} catch (Exception e) { }

		throw new ValueConverterException("Couldn't convert '" + s + "' to a time value.", node, null);
	}
}
