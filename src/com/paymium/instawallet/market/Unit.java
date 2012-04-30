package com.paymium.instawallet.market;

import com.google.gson.annotations.SerializedName;

public class Unit 
{
	@SerializedName("24h")
	private String day;

	public String getDay() 
	{
		return day;
	}

	public void setDay(String day) 
	{
		this.day = day;
	}
	
	public String toString()
	{
		StringBuilder asString = new StringBuilder();

	    asString.append("24h                   : ");
	    asString.append(this.day);
	    asString.append("\n\n");
	    
	    return (asString.toString());
	    
	}
}
