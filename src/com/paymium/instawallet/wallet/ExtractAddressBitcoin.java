package com.paymium.instawallet.wallet;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractAddressBitcoin 
{
	private Pattern pattern;
	private Matcher matcher;

	private static final String ADDRESS_BICOIN_PATTERN = "([\\w&&[^_0Il]]){30,35}";
	
	private ArrayList<String> addressBitcoin;

	public ExtractAddressBitcoin() 
	{
		this.pattern = Pattern.compile(ADDRESS_BICOIN_PATTERN);	
		this.addressBitcoin = new ArrayList<String>();
	}
	
	public ArrayList<String> extract(String content) 
	{
	    this.matcher = pattern.matcher(content);
	    
	    while (matcher.find())
	    {
	    	this.addressBitcoin.add(matcher.group());	    	
	    }
	    
	    return this.addressBitcoin;
	}	
}
