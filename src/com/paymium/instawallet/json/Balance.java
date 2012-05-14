package com.paymium.instawallet.json;

import java.math.BigDecimal;

import com.google.gson.annotations.SerializedName;

public class Balance 
{
	@SerializedName("successful")
	private boolean successful;
	
	@SerializedName("balance")
	private BigDecimal balance;
	
	@SerializedName("spendable_balance")
	private BigDecimal spendable_balance;

	public Balance(boolean successful, BigDecimal balance, BigDecimal spendable_balance) 
	{
		super();
		this.successful = successful;
		this.balance = balance;
		this.spendable_balance = spendable_balance;
	}

	public Balance() 
	{
		super();
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

	public BigDecimal getSpendable_balance() 
	{
		return spendable_balance;
	}

	public void setSpendable_balance(BigDecimal spendable_balance) 
	{
		this.spendable_balance = spendable_balance;
	}
}
