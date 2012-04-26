package com.paymium.instawallet.wallet;

import java.util.LinkedList;

public class WalletsList extends LinkedList<Wallet> 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String toString()
	{
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("******* Wallets List ********");
		stringBuilder.append("\n\n");
		
		for (int i = 0 ; i < this.size() ; i++)
		{
			stringBuilder.append("\n");
			stringBuilder.append((i+1) +"/. "+ this.get(i).toString());
		}
		
		return stringBuilder.toString();
	}

}
