package com.paymium.instawallet.json;

import com.google.gson.annotations.SerializedName;

public class NewWallet 
{
	@SerializedName("successful")
	private boolean successful;
	
	@SerializedName("wallet_id")
	private String wallet_id;

	
	
	public NewWallet() 
	{
		super();
		// TODO Auto-generated constructor stub
	}



	public NewWallet(boolean successful, String wallet_id) 
	{
		super();
		this.successful = successful;
		this.wallet_id = wallet_id;
	}



	public boolean isSuccessful() 
	{
		return successful;
	}



	public void setSuccessful(boolean successful) 
	{
		this.successful = successful;
	}



	public String getWallet_id() 
	{
		return wallet_id;
	}



	public void setWallet_id(String wallet_id) 
	{
		this.wallet_id = wallet_id;
	}
	
	
	
	
}
