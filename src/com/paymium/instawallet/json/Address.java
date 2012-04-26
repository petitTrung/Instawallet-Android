package com.paymium.instawallet.json;

import com.google.gson.annotations.SerializedName;

public class Address 
{
	@SerializedName("successful")
	private boolean successful;
	
	@SerializedName("address")
	private String address;

	
	
	public Address() 
	{
		super();
		// TODO Auto-generated constructor stub
	}



	public Address(boolean successful, String address) 
	{
		super();
		this.successful = successful;
		this.address = address;
	}



	public boolean isSuccessful() 
	{
		return successful;
	}



	public void setSuccessful(boolean successful) 
	{
		this.successful = successful;
	}



	public String getAddress() 
	{
		return address;
	}



	public void setAddress(String address) 
	{
		this.address = address;
	}
	
	
	
	
	
	
}
