package com.paymium.instawallet.market;

import com.google.gson.annotations.SerializedName;

public class MarketPrices 
{
	@SerializedName("USD")
	private Unit usd;
	
	@SerializedName("EUR")
	private Unit eur;
	
	@SerializedName("GBP")
	private Unit gbp;

	
	
	public MarketPrices() 
	{
		super();
		// TODO Auto-generated constructor stub
	}

	public MarketPrices(Unit usd, Unit eur, Unit gbp) 
	{
		super();
		this.usd = usd;
		this.eur = eur;
		this.gbp = gbp;
	}

	public Unit getUsd() 
	{
		return usd;
	}

	public void setUsd(Unit usd) 
	{
		this.usd = usd;
	}

	public Unit getEur() 
	{
		return eur;
	}

	public void setEur(Unit eur) 
	{
		this.eur = eur;
	}

	public Unit getGbp() 
	{
		return gbp;
	}

	public void setGbp(Unit gbp) 
	{
		this.gbp = gbp;
	}
	
	
	public String toString()
	{
		StringBuilder asString = new StringBuilder();

	    asString.append("Market Prices\n=============================\n");
	    asString.append("USD                    : ");
	    asString.append(this.usd);
	    asString.append("\n");
	
	    asString.append("EUR                    : ");
	    asString.append(this.eur);
	    asString.append("\n");
	    
	    asString.append("GBP                    : ");
	    asString.append(this.gbp);
	    asString.append("\n\n");
	    
	    return (asString.toString());
	    
	}
	
}
