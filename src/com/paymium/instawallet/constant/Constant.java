/*
 * 
 */
package com.paymium.instawallet.constant;

// TODO: Auto-generated Javadoc
/**
 * The Class HttpVerb. 
 * This class contain the name of methods
 */
public final class Constant 
{
	
	/** The Constant GET. */
	public final static String GET = "GET";
	
	/** The Constant POST. */
	public final static String POST = "POST";
	
	/** The Constant DELETE. */
	public final static String DELETE = "DELETE";
	
	public final static String backendUrl = "https://instawallet.org/api/v1";
	
	public final static String newWalletUrl = backendUrl + "/new_wallet";
	
	public static String addressUrl (String wallet_id)
	{
		return backendUrl + "/w/" + wallet_id + "/address"; 
	}
	
	public static String balanceUrl (String wallet_id)
	{
		return backendUrl + "/w/" + wallet_id + "/balance"; 
	}
	
	public static String paymentUrl (String wallet_id)
	{
		return backendUrl + "/w/" + wallet_id + "/payment"; 
	}
	
	public static String subscriptionUrl (String wallet_id)
	{
		return backendUrl + "/w/" + wallet_id + "/subscription"; 
	}
	
}
