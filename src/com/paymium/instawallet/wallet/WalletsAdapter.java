package com.paymium.instawallet.wallet;

import java.text.DecimalFormat;
import java.util.LinkedList;

import com.paymium.instawallet.R;
import com.paymium.instawallet.database.WalletsHandler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class WalletsAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;
	private WalletsList walletsList;
	
	private Context context;
	private WalletsHandler db;
	private DecimalFormat decimalFormat;
	
	public WalletsAdapter(Context context)
	{
		this.context = context;
		this.walletsList = new WalletsList();
		this.layoutInflater = LayoutInflater.from(this.context);
		this.decimalFormat = new DecimalFormat("'+' ###0.00;'−' ###0.00");
		
		//---Initialize database
		this.db = new WalletsHandler(this.context);
	}
	
	public void addItem(Wallet wallet)
	{
		this.walletsList.add(wallet);
		this.notifyDataSetChanged();
		this.db.addWallet(wallet);
	}
	
	public void addItems(LinkedList<Wallet> walletsList)
	{
		this.walletsList.addAll(walletsList);
		this.notifyDataSetChanged();
	}
	
	public void updateItem(Wallet wallet)
	{
		int index = -1;
		
		for (int i = 0 ; i < this.walletsList.size() ; i++ )
		{
			if (this.walletsList.get(i).equals(wallet))
			{
				index = i;
				break;
			}
				
		}
		if (index >= 0)
		{
			this.walletsList.set(index, wallet);
			this.notifyDataSetChanged();
			this.db.updateWallet(wallet);
		}
		
		
	}
	
	public void removeItem(Wallet wallet)
	{
		int index = -1;
		
		for (int i = 0 ; i < this.walletsList.size() ; i++ )
		{
			if (this.walletsList.get(i).equals(wallet))
			{
				index = i;
				break;
			}
				
		}
		if (index >= 0)
		{
			this.walletsList.remove(index);
			this.notifyDataSetChanged();
			this.db.deleteWallet(wallet);
		}
	}

	public int getCount() 
	{
		// TODO Auto-generated method stub
		return this.walletsList.size();
	}

	public Object getItem(int position) 
	{
		// TODO Auto-generated method stub
		return this.walletsList.get(position);
	}

	public long getItemId(int position) 
	{
		// TODO Auto-generated method stub
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) 
	{
		// TODO Auto-generated method stub
		
		
		if (convertView == null) 
		{
			convertView = this.layoutInflater.inflate(R.layout.wallet_item, null);
		}
		
		TextView wallet_id = (TextView) convertView.findViewById(R.id.textView1);
		TextView wallet_address = (TextView) convertView.findViewById(R.id.textView2);
		TextView wallet_balance = (TextView) convertView.findViewById(R.id.textView3);
		
		Wallet wallet = this.walletsList.get(position);
		
		wallet_id.setText(wallet.getWallet_id());
		wallet_address.setText(wallet.getWallet_address());
		wallet_balance.setText(this.decimalFormat.format(wallet.getWallet_balance()));

		return convertView;
	}

}
