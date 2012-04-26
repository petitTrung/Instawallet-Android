package com.paymium.instawallet.wallet;

import java.math.BigDecimal;

public class Wallet 
{		
	private String wallet_id;
	
	private String wallet_address;
	
	private BigDecimal wallet_balance;

	
	public Wallet() 
	{
		super();
		// TODO Auto-generated constructor stub
	}

	public Wallet(String wallet_id) 
	{
		super();
		this.wallet_id = wallet_id;
	}

	public String getWallet_id() 
	{
		return wallet_id;
	}

	public void setWallet_id(String wallet_id) 
	{
		this.wallet_id = wallet_id;
	}

	public String getWallet_address() 
	{
		return wallet_address;
	}

	public void setWallet_address(String wallet_address) 
	{
		this.wallet_address = wallet_address;
	}

	public BigDecimal getWallet_balance() 
	{
		return wallet_balance;
	}

	public void setWallet_balance(BigDecimal wallet_balance)
	{
		this.wallet_balance = wallet_balance;
	}
	
	public String toString()
	{
		StringBuilder asString = new StringBuilder();

	    asString.append("Wallet\n=============================\n");
	    asString.append("Id                    : ");
	    asString.append(this.wallet_id);
	    asString.append("\n");
	
	    asString.append("Address               : ");
	    asString.append(this.wallet_address);
	    asString.append("\n");
	    
	    asString.append("Balance               : ");
	    asString.append(this.wallet_balance.toString());
	    asString.append("\n\n");
	    
	    return (asString.toString());
	    
	}
	
}
