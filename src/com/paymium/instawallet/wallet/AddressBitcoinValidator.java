package com.paymium.instawallet.wallet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AddressBitcoinValidator 
{
	private Pattern pattern;
	private Matcher matcher;

	private static final String ADDRESS_BICOIN_PATTERN = "([\\w&&[^_0Il]]){32,35}";

	public AddressBitcoinValidator()
	{
		this.pattern = Pattern.compile(ADDRESS_BICOIN_PATTERN);
	}

	public boolean validate(String s)
	{
		this.matcher = pattern.matcher(s);
		return this.matcher.find();
	}
}
