package com.paymium.instawallet.payment;

import java.math.BigDecimal;

import com.google.gson.annotations.SerializedName;

public class GreenAddressPayment 
{
	@SerializedName("address")
	private String address;
	
	@SerializedName("amount")
	private BigDecimal amount;
	
	@SerializedName("use_green_address")
	private boolean use_green_address;

	
	
	public GreenAddressPayment() 
	{
		super();
		// TODO Auto-generated constructor stub
	}



	public GreenAddressPayment(String address, BigDecimal amount, boolean use_green_address) 
	{
		super();
		this.address = address;
		this.amount = amount;
		this.use_green_address = use_green_address;
	}



	public String getAddress() 
	{
		return this.address;
	}



	public void setAddress(String address) 
	{
		this.address = address;
	}



	public BigDecimal getAmount() 
	{
		return this.amount;
	}



	public void setAmount(BigDecimal amount) 
	{
		this.amount = amount;
	}



	public boolean isUse_green_address() 
	{
		return this.use_green_address;
	}



	public void setUse_green_address(boolean use_green_address) 
	{
		this.use_green_address = use_green_address;
	}
	
	
	
	
}
