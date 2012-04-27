package com.paymium.instawallet.wallet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WalletIdValidator 
{
	private Pattern pattern;
	private Matcher matcher;

	private static final String WALLET_ID_PATTERN = "([\\w&&[^_]]){20,}";

	public WalletIdValidator()
	{
		this.pattern = Pattern.compile(WALLET_ID_PATTERN);
	}

	public boolean validate(String s)
	{
		this.matcher = pattern.matcher(s);
		return this.matcher.find();
	}
}
