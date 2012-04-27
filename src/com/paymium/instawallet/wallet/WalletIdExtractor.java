package com.paymium.instawallet.wallet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WalletIdExtractor 
{
	private Pattern pattern;
	private Matcher matcher;

	private static final String WALLET_ID_PATTERN = "([\\w&&[^_]]){20,}";
	
	private LinkedList<String> walletIdList;

	public WalletIdExtractor() 
	{
		this.pattern = Pattern.compile(WALLET_ID_PATTERN);	
		this.walletIdList = new LinkedList<String>();
	}
	
	public LinkedList<String> extract(String content) 
	{
	    this.matcher = pattern.matcher(content);
	    
	    while (matcher.find())
	    {
	    	this.walletIdList.add(matcher.group());	    	
	    }
	    
	    return this.walletIdList;
	}	
}
