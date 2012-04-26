package com.paymium.instawallet.json;

import com.google.gson.annotations.SerializedName;

public class Payment 
{
	@SerializedName("successful")
	private boolean successful;
	
	@SerializedName("message")
	private String message;
	
	@SerializedName("message_code")
	private String message_code;

	
	public Payment() 
	{
		super();
		// TODO Auto-generated constructor stub
	}


	public Payment(boolean successful, String message, String message_code) 
	{
		super();
		this.successful = successful;
		this.message = message;
		this.message_code = message_code;
	}


	public boolean isSuccessful() 
	{
		return successful;
	}


	public void setSuccessful(boolean successful) 
	{
		this.successful = successful;
	}


	public String getMessage() 
	{
		return message;
	}


	public void setMessage(String message) 
	{
		this.message = message;
	}


	public String getMessage_code() 
	{
		return message_code;
	}


	public void setMessage_code(String message_code) 
	{
		this.message_code = message_code;
	}
	
	
	
}
