package com.paymium.instawallet.wallet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.paymium.instawallet.constant.Constant;
import com.paymium.instawallet.exception.ConnectionNotInitializedException;
import com.paymium.instawallet.json.Address;
import com.paymium.instawallet.json.Balance;
import com.paymium.instawallet.json.NewWallet;
import com.paymium.instawallet.json.Payment;


public class Connection 
{
	private static Connection instance;
	
	private Gson gson;
	
	private boolean isInitialized = false;
	
	private boolean isPayment = false;

	
	/**
	 * Instantiates a new connection.
	 * Private constructor prevents instantiation from other classes
	 */
	private Connection() 
	{
		super();
	}

	
	/**
	 * Gets the single instance of Connection (Singleton)
	 *
	 * @return Singleton Connection
	 */
	public final static Connection getInstance() 
	{
		if (Connection.instance == null) 
		{
			synchronized (Connection.class) 
			{
				if (Connection.instance == null) 
				{
					Connection.instance = new Connection();
				}
			}
		}

		return Connection.instance;
	}

	
	public Connection initialize()
	{
		GsonBuilder gsonBuilder = new GsonBuilder();

		this.gson = gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
		
		this.decimalFormat = new DecimalFormat("#################");

		this.isInitialized = true;

		return (Connection.getInstance());
	}
	
	private String getMethod(String url) throws IOException, ConnectionNotInitializedException 
	{
		if (!this.isInitialized) 
		{
			throw new ConnectionNotInitializedException("Connection has not been initialized");
		}
		else
		{
			System.setProperty("http.keepAlive", "false");
	       
	        DefaultHttpClient http_client = new DefaultHttpClient();
        	
	        HttpGet http_get = new HttpGet(url);
	        http_get.setHeader("Accept", "application/json");

	        
	        HttpResponse response = http_client.execute(http_get);
			InputStream content = response.getEntity().getContent();
			
			BufferedReader responseReader = new BufferedReader(new InputStreamReader(content));
			
			StringBuilder responseBuilder = new StringBuilder();
			String line = null;
			
			while ((line = responseReader.readLine()) != null) 
			{
				responseBuilder.append(line);
			}
			//System.out.println("Return get ( <= 2.2 ) : " + response.getStatusLine().getStatusCode() + " " + responseBuilder.toString());
			
			return (responseBuilder.toString());
    
		}
		
	}
	
	
	
	private String postMethod(String url, JsonObject jsonData) throws IOException, ConnectionNotInitializedException 
	{
		if (!this.isInitialized) 
		{
			throw new ConnectionNotInitializedException("Connection has not been initialized");
		}
		else
		{
			if (this.isPayment == true) 
			{
				this.setPayment(false);
				
				if (jsonData == null) 
				{
					throw new IllegalArgumentException("Cannot POST payment with empty body");
				}
				else
				{
					String jsonString = jsonData.toString();
		        	
		        	DefaultHttpClient http_client = new DefaultHttpClient();
		        	
					HttpPost http_post = new HttpPost(url);
					http_post.setHeader("Accept", "application/json");
					http_post.setHeader("Content-Type", "application/json");
					
					StringEntity s = new StringEntity(jsonString);
					s.setContentType("application/json");
					http_post.setEntity(s);

			
					HttpResponse response = http_client.execute(http_post);
					InputStream content = response.getEntity().getContent();
					
					BufferedReader responseReader = new BufferedReader(new InputStreamReader(content));
					
					StringBuilder responseBuilder = new StringBuilder();
					String line = null;
					
					while ((line = responseReader.readLine()) != null) 
					{
						responseBuilder.append(line);
					}
					//System.out.println("Return post ( <= 2.2 ) : " + response.getStatusLine().getStatusCode() + " "  + responseBuilder.toString());
				
					return (responseBuilder.toString());
				}
			}
			
			else
			{
				
				DefaultHttpClient http_client = new DefaultHttpClient();;
	        	
				HttpPost http_post = new HttpPost(url);
				http_post.setHeader("Accept", "application/json");
		
				HttpResponse response = http_client.execute(http_post);
				InputStream content = response.getEntity().getContent();
				
				BufferedReader responseReader = new BufferedReader(new InputStreamReader(content));
				
				StringBuilder responseBuilder = new StringBuilder();
				String line = null;
				
				while ((line = responseReader.readLine()) != null) 
				{
					responseBuilder.append(line);
				}
				//System.out.println("Return post ( <= 2.2 ) : " + response.getStatusLine().getStatusCode() + " "  + responseBuilder.toString());
			
				return (responseBuilder.toString());
			}	
		}
	}

	
	

	public NewWallet createNewWallet() throws IOException, ConnectionNotInitializedException 
	{
		Pattern pattern;
		Matcher matcher;
		boolean successful;
		
		String response = this.postMethod(Constant.newWalletUrl,null);
		
		//System.out.println(response);
		
		pattern = Pattern.compile("true");
		matcher = pattern.matcher(response);
		successful = matcher.find();
		
		if(successful)
		{
			System.out.println("A new wallet was created !!");
			
			NewWallet a = this.gson.fromJson(response, NewWallet.class);
			
			return a;
		}
		else
		{
			System.out.println("No wallet was created !!");
			
			return null;
		}
	}
	
	public Address getAddressJson(String wallet_id) throws IOException, ConnectionNotInitializedException 
	{
		Pattern pattern;
		Matcher matcher;
		boolean successful;
		
		String response = this.getMethod(Constant.addressUrl(wallet_id));
		
		pattern = Pattern.compile("true");
		matcher = pattern.matcher(response);
		successful = matcher.find();
		
		if(successful)
		{
			//System.out.println("Get an address !!");
			
			Address a = gson.fromJson(response, Address.class);
			
			return a;
		}
		else
		{
			//System.out.println("No address was gotten !!");
			
			return null;
		}
	}
	
	
	
	public Balance getBalanceJson(String wallet_id) throws IOException, ConnectionNotInitializedException 
	{
		Pattern pattern;
		Matcher matcher;
		boolean successful;
		
		String response = this.getMethod(Constant.balanceUrl(wallet_id));
		
		pattern = Pattern.compile("true");
		matcher = pattern.matcher(response);
		successful = matcher.find();
		
		if(successful)
		{	
			Balance a = gson.fromJson(response, Balance.class);
			
			return a;
		}
		else
		{
			//System.out.println("No balance was gotten !!");
			
			return null;
		}
	}
	
	
	public Wallet getWallet(String wallet_id) throws IOException, ConnectionNotInitializedException 
	{
		Wallet wallet = new Wallet();
		
		wallet.setWallet_id(wallet_id);
		wallet.setWallet_address(this.getAddressJson(wallet_id).getAddress());
		
		Balance b = this.getBalanceJson(wallet_id);
			
		wallet.setWallet_balance(b.getBalance().divide(new BigDecimal(Math.pow(10, 8))));
		wallet.setWallet_spendable_balance(b.getSpendable_balance().divide(new BigDecimal(Math.pow(10, 8))));
		
		return wallet;
	}
	
	
	private DecimalFormat decimalFormat;
	
	public Payment postPayment(String wallet_id, String address, BigDecimal amount) throws IOException, ConnectionNotInitializedException
	{
		this.setPayment(true);		
		
		BigDecimal amountSatoshis = amount.multiply(new BigDecimal(Math.pow(10, 8)));
		
		
		JsonElement addressJson = this.gson.toJsonTree(address);
		JsonElement amountJson = this.gson.toJsonTree(decimalFormat.format(amountSatoshis));
		
		
		JsonObject jsonData = new JsonObject();
		
		jsonData.add("address", addressJson);
		jsonData.add("amount", amountJson);;
		
		String response = postMethod(Constant.paymentUrl(wallet_id), jsonData);
	
		return this.gson.fromJson(response, Payment.class);
		
	}
	

	public boolean isPayment() 
	{
		return this.isPayment;
	}

	public void setPayment(boolean isPayment) 
	{
		this.isPayment = isPayment;
	}

}
