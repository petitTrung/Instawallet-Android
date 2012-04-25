package com.paymium.instawallet.wallet;

import java.io.IOException;
import java.math.BigDecimal;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.paymium.instawallet.R;
import com.paymium.instawallet.dialog.AlertingDialog;
import com.paymium.instawallet.dialog.LoadingDialog;
import com.paymium.instawallet.exception.ConnectionNotInitializedException;
import com.paymium.instawallet.flip.AnimationFactory;
import com.paymium.instawallet.flip.AnimationFactory.FlipDirection;
import com.paymium.instawallet.json.NewWallet;


import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewAnimator;


public class WalletsActivity extends SherlockFragmentActivity implements OnClickListener 
{
	private ListView walletsList;
	private WalletsAdapter walletsAdapter;
	
	private ViewAnimator viewAnimator;
	
	private Connection connection;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        setTheme(R.style.Theme_Sherlock);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallets);
        
        ImageButton add = (ImageButton) findViewById(R.id.imageButton1);
        add.setOnClickListener(this);
        
        ImageButton export = (ImageButton) findViewById(R.id.imageButton2);
        export.setOnClickListener(this);
        
        ImageButton share = (ImageButton) findViewById(R.id.imageButton3);
        share.setOnClickListener(this);
        
        this.viewAnimator = (ViewAnimator) findViewById(R.id.viewFlipper);
        
        this.walletsList = (ListView) findViewById(R.id.listView1);
        this.walletsAdapter = new WalletsAdapter(this);
        this.walletsList.setAdapter(this.walletsAdapter);
        
        this.connection = Connection.getInstance().initialize();
   
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
	{
    	super.onCreateOptionsMenu(menu);
    	CreateMenu(menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	// TODO Auto-generated method stub
    	return MenuChoice(item);
    }
    
    private void CreateMenu(Menu menu)
    {
    	MenuItem refresh = menu.add(0,0,0,"Refresh");
    	{
    		refresh.setIcon(R.drawable.ic_refresh);
    		refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);		
    	}
    }
    
    private boolean MenuChoice(MenuItem item) 
    {
    	switch (item.getItemId()) 
    	{
		case 0:
			
			Toast.makeText(this, "Refresh ...", Toast.LENGTH_LONG).show();
			Refresh();
			break;
		}
    	return false;
    }
    
    public void Refresh()
    {
    	LoadingDialog dialog = LoadingDialog.newInstance("Waiting","Loading...");
    	dialog.show(getSupportFragmentManager(), "dialog");
    }

	public void onClick(View view) 
	{
		if (view.getId() == R.id.imageButton1)
		{
			new addWallet().execute();
		}
		else if (view.getId() == R.id.imageButton2)
		{
			
			if (this.currentView() == R.id.flip1)
			{
				Toast.makeText(WalletsActivity.this, "list", Toast.LENGTH_SHORT).show();
			}
			else if (this.currentView() == R.id.flip2)
			{
				Toast.makeText(WalletsActivity.this, "qr code", Toast.LENGTH_SHORT).show();
			}
			AnimationFactory.flipTransition(viewAnimator, FlipDirection.LEFT_RIGHT);
			
		}
		else if (view.getId() == R.id.imageButton3)
		{
			AnimationFactory.flipTransition(viewAnimator, FlipDirection.LEFT_RIGHT);
			Toast.makeText(WalletsActivity.this, "Side B Touched", Toast.LENGTH_SHORT).show();
		}
		
		
	}
	
	public int currentView()
	{
		return this.viewAnimator.getCurrentView().getId(); 
	}
	
	private AlertingDialog alertingDialog;
	private LoadingDialog loadingDialog;
	
	
	public class addWallet extends AsyncTask<String, Integer, Object>
	{
		@Override
		protected void onPreExecute() 
		{ 
			super.onPreExecute();
			
			loadingDialog = LoadingDialog.newInstance("Please wait", "Loading ...");			  									
			loadingDialog.show(getSupportFragmentManager(), "loading dialog");
	    } 

		@Override
		protected Object doInBackground(String... arg0) 
		{
			// TODO Auto-generated method stub
			NewWallet newWallet;
			Wallet wallet = null;
			
			try 
			{
				newWallet = connection.createNewWallet();
				
				String wallet_id = newWallet.getWallet_id();
				String wallet_address = connection.getAddressJson(wallet_id).getAddress();
				BigDecimal wallet_balance = connection.getBalanceJson(wallet_id).getBalance();
				
				if (notEmpty(wallet_id) && notEmpty(wallet_address) && notEmpty(wallet_balance.toString()))
				{
					wallet = new Wallet();
					wallet.setWallet_id(wallet_id);
					wallet.setWallet_address(wallet_address);
					wallet.setWallet_balance(wallet_balance);
					
					return wallet;
				}
	
			} 
			catch (IOException e) 
			{
				// If there is no connection
				e.printStackTrace();
				
				return "no connection";
			} 
			
			catch (ConnectionNotInitializedException e) 
			{
				// If the connection is too slow
				e.printStackTrace();
				
				return "slow connection";
			}	
			
			return "fail";
		}
		
		@Override
		protected void onPostExecute(Object result) 
		{
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			loadingDialog.dismiss();
			
			
			
			if (result.getClass().getSimpleName().equals("Wallet"))
			{
				Wallet a = (Wallet) result;
				walletsAdapter.addItem(a);
				
				alertingDialog = AlertingDialog.newInstance("Successful !!", "A wallet has been added", R.drawable.ok);
				alertingDialog.show(getSupportFragmentManager(), "ok alerting dialog");
			}
			else if (result.getClass().getSimpleName().equals("String"))
			{
				if (result.equals("no connection"))
				{
					alertingDialog = AlertingDialog.newInstance("Fail !!", "No connection, no wallet has been created", R.drawable.error);
					alertingDialog.show(getSupportFragmentManager(), "error 1 alerting dialog");
				}
				else if (result.equals("slow connection"))
				{
					alertingDialog = AlertingDialog.newInstance("Fail !!", "Slow connection, no wallet has been created", R.drawable.error);
					alertingDialog.show(getSupportFragmentManager(), "error 2 alerting dialog");
				}
				else if (result.equals("fail"))
				{
					alertingDialog = AlertingDialog.newInstance("Fail !!", "Error unknown", R.drawable.error);
					alertingDialog.show(getSupportFragmentManager(), "error 3 alerting dialog");
				}
			}
		}
		
	}
	
	public boolean notEmpty(String s) 
	{
		return (s != null && s.length() > 0);
	}
	
    
    
}