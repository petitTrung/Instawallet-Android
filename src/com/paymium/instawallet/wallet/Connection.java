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
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.paymium.instawallet.connection.MyHttpClient;
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
	
	private Context context;

	
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

	public Connection initialize(Context context)
	{
		GsonBuilder gsonBuilder = new GsonBuilder();

		// TODO : Handle timezones properly
		this.gson = gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
		
		this.context = context;
		
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
	        System.out.println("Android version <= 2.2");
	        
	        DefaultHttpClient http_client = new MyHttpClient(context);
        	
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
			if (this.isPayment) 
			{
				if (jsonData == null) 
				{
					throw new IllegalArgumentException("Cannot POST payment with empty body");
				}
				else
				{
					String jsonString = jsonData.toString();
		        	
		        	DefaultHttpClient http_client = new MyHttpClient(context);
		        	http_client.getParams().setParameter("http.protocol.version",HttpVersion.HTTP_1_0);
		        	
					HttpPost http_post = new HttpPost(url);
					http_post.setHeader("Accept", "application/json");
					http_post.setHeader("Content-Type", "application/json");
					
					/*List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("address","1CSNnXR5sKnYiVszLpTki22UHqA3j1XJWT"));
					nameValuePairs.add(new BasicNameValuePair("amount","0.00000005"));	
					http_post.setEntity(new UrlEncodedFormEntity(nameValuePairs));*/

					//http_post.setHeader("Content-Length", Integer.toString(jsonString.getBytes().length));
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
				DefaultHttpClient http_client = new MyHttpClient(context);
	        	
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
		boolean successful,id;
		
		String response = this.postMethod(Constant.newWalletUrl,null);
		
		//System.out.println(response);
		
		pattern = Pattern.compile("true");
		matcher = pattern.matcher(response);
		successful = matcher.find();
		
		pattern = Pattern.compile("wallet_id");
		matcher = pattern.matcher(response);
		id = matcher.find();
		
		if(successful && id)
		{
			System.out.println("A new wallet was created !!");
			
			NewWallet a = gson.fromJson(response, NewWallet.class);
			
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
		boolean successful,address;
		
		String response = this.getMethod(Constant.addressUrl(wallet_id));
		
		pattern = Pattern.compile("true");
		matcher = pattern.matcher(response);
		successful = matcher.find();
		
		pattern = Pattern.compile("address");
		matcher = pattern.matcher(response);
		address = matcher.find();
		
		if(successful && address)
		{
			System.out.println("Get an address !!");
			
			Address a = gson.fromJson(response, Address.class);
			
			return a;
		}
		else
		{
			System.out.println("No address was gotten !!");
			
			return null;
		}
	}
	
	
	
	public Balance getBalanceJson(String wallet_id) throws IOException, ConnectionNotInitializedException 
	{
		Pattern pattern;
		Matcher matcher;
		boolean successful,balance;
		
		String response = this.getMethod(Constant.balanceUrl(wallet_id));
		
		pattern = Pattern.compile("true");
		matcher = pattern.matcher(response);
		successful = matcher.find();
		
		pattern = Pattern.compile("balance");
		matcher = pattern.matcher(response);
		balance = matcher.find();
		
		if(successful && balance)
		{
			System.out.println("Get a balance !!");
			
			Balance a = gson.fromJson(response, Balance.class);
			
			return a;
		}
		else
		{
			System.out.println("No balance was gotten !!");
			
			return null;
		}
	}
	
	
	public Wallet getWallet(String wallet_id) throws IOException, ConnectionNotInitializedException 
	{
		Wallet wallet = new Wallet();
		
		wallet.setWallet_id(wallet_id);
		wallet.setWallet_address(this.getAddressJson(wallet_id).getAddress());
		wallet.setWallet_balance(this.getBalanceJson(wallet_id).getBalance().divide(new BigDecimal(Math.pow(10, 8))));
		
		return wallet;
	}
	
	
	private DecimalFormat decimalFormat;
	
	public Object postPayment(String wallet_id, String address, BigDecimal amount) throws IOException, ConnectionNotInitializedException
	{
		this.setPayment(true);		
		
		BigDecimal amountSatoshis = amount.multiply(new BigDecimal(Math.pow(10, 8)));
		
		
		JsonElement addressJson = gson.toJsonTree(address);
		JsonElement amountJson = gson.toJsonTree(decimalFormat.format(amountSatoshis));
		
		
		JsonObject jsonData = new JsonObject();
		
		jsonData.add("address", addressJson);
		jsonData.add("amount", amountJson);;
		
		System.out.println(jsonData.toString());
		
		Pattern pattern;
		Matcher matcher;
		boolean successful;
		
		String response = postMethod(Constant.paymentUrl(wallet_id), jsonData);
		
		System.out.println("Payment result : " + response);
		
		pattern = Pattern.compile("true");
		matcher = pattern.matcher(response);
		successful = matcher.find();

		
		if(successful)
		{
			return (this.gson.fromJson(response, Payment.class));
		}
		else
		{
			Payment a = this.gson.fromJson(response, Payment.class);
			
			System.out.println("Message code : " + a.getMessage_code());
			
			System.out.println("Message      : " + a.getMessage());
			
			return a.getMessage();

		}
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
