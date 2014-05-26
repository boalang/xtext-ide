package edu.iastate.cs.boa.conversion;

import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;
import org.eclipse.xtext.conversion.impl.AbstractDeclarativeValueConverterService;
import org.eclipse.xtext.conversion.impl.AbstractIDValueConverter;
import org.eclipse.xtext.conversion.impl.STRINGValueConverter;

import com.google.inject.Inject;

public class BoaValueConverterService extends AbstractDeclarativeValueConverterService {
	@Inject
	private AbstractIDValueConverter idValueConverter;

	@ValueConverter(rule = "ID")
	public IValueConverter<String> ID() {
		return idValueConverter;
	}

	@Inject
	private LONGValueConverter longValueConverter;

	@ValueConverter(rule = "INT")
	public IValueConverter<Long> INT() {
		return longValueConverter;
	}

	@Inject
	private STRINGValueConverter stringValueConverter;

	@ValueConverter(rule = "STRING_LIT")
	public IValueConverter<String> STRING_LIT() {
		return stringValueConverter;
	}

	@Inject
	private REGEXValueConverter regexValueConverter;

	@ValueConverter(rule = "REGEX_LIT")
	public IValueConverter<String> REGEX_LIT() {
		return regexValueConverter;
	}

	@Inject
	private TIMEValueConverter timeValueConverter;

	@ValueConverter(rule = "TIME_LIT")
	public IValueConverter<Long> TIME_LIT() {
		return timeValueConverter;
	}
}
