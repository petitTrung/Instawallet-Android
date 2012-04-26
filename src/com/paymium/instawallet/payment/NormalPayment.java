package com.paymium.instawallet.payment;

import java.math.BigDecimal;

import com.google.gson.annotations.SerializedName;

public class NormalPayment 
{
	@SerializedName("address")
	private String address;
	
	@SerializedName("amount")
	private BigDecimal amount;

	public NormalPayment() 
	{
		super();
		// TODO Auto-generated constructor stub
	}

	public NormalPayment(String address, BigDecimal amount) 
	{
		super();
		this.address = address;
		this.amount = amount;
	}

	public String getAddress() 
	{
		return address;
	}

	public void setAddress(String address) 
	{
		this.address = address;
	}

	public BigDecimal getAmount() 
	{
		return amount;
	}

	public void setAmount(BigDecimal amount) 
	{
		this.amount = amount;
	}
	
	
	
	
}
