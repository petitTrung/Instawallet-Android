package com.paymium.instawallet.json;

import java.math.BigDecimal;

import com.google.gson.annotations.SerializedName;

public class Balance 
{
	@SerializedName("successful")
	private boolean successful;
	
	
	@SerializedName("balance")
	private BigDecimal balance;


	public Balance() 
	{
		super();
		// TODO Auto-generated constructor stub
	}


	public boolean isSuccessful() 
	{
		return successful;
	}


	public void setSuccessful(boolean successful) 
	{
		this.successful = successful;
	}


	public BigDecimal getBalance() 
	{
		return balance;
	}


	public void setBalance(BigDecimal balance) 
	{
		this.balance = balance;
	}


	
}
