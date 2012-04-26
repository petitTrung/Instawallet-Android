package com.paymium.instawallet.json;

import com.google.gson.annotations.SerializedName;

public class Subscription 
{
	@SerializedName("successful")
	private boolean successful;
	
	@SerializedName("subscription_id")
	private String subscription_id;

	public Subscription() 
	{
		super();
		// TODO Auto-generated constructor stub
	}

	public Subscription(boolean successful, String subscription_id) 
	{
		super();
		this.successful = successful;
		this.subscription_id = subscription_id;
	}

	public boolean isSuccessful() 
	{
		return successful;
	}

	public void setSuccessful(boolean successful) 
	{
		this.successful = successful;
	}

	public String getSubscription_id() 
	{
		return subscription_id;
	}

	public void setSubscription_id(String subscription_id) 
	{
		this.subscription_id = subscription_id;
	}
	
	
	
}
