package com.paymium.instawallet.wallet;

import java.io.IOException;
import java.math.BigDecimal;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.paymium.instawallet.R;
import com.paymium.instawallet.database.WalletsHandler;
import com.paymium.instawallet.dialog.AlertingDialog;
import com.paymium.instawallet.dialog.LoadingDialog;
import com.paymium.instawallet.exception.ConnectionNotInitializedException;
import com.paymium.instawallet.flip.AnimationFactory;
import com.paymium.instawallet.flip.AnimationFactory.FlipDirection;
import com.paymium.instawallet.json.NewWallet;
import com.paymium.instawallet.qrcode.QrCode;


public class WalletsActivity extends SherlockFragmentActivity implements OnClickListener 
{
	private ListView walletsList;
	private WalletsAdapter walletsAdapter;
	
	private ViewAnimator viewAnimator;
	
	private Connection connection;
	
	private Fragment menuWalletsList;
	private Fragment menuSingleWallet;
	
	private static final int REQUEST_CODE = 1;
	
	private WalletsHandler wallets_db;

	private static final int ID_DETAIL = 1;
	private static final int ID_DELETE = 2;
	private static final int ID_SAVE = 3;
	private static final int ID_SHARE = 4;
	private Wallet wl;
	
	private ImageView qr;
	private TextView btcAddress;
	private TextView clickCopy;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        setTheme(R.style.Theme_Sherlock_ForceOverflow);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallets);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        ImageButton add = (ImageButton) findViewById(R.id.imageButton1);
        add.setOnClickListener(this);
        
        ImageButton export = (ImageButton) findViewById(R.id.imageButton2);
        export.setOnClickListener(this);
        
        ImageButton share = (ImageButton) findViewById(R.id.imageButton3);
        share.setOnClickListener(this);
        
        this.qr = (ImageView) findViewById(R.id.imageView1);
        this.btcAddress = (TextView) findViewById(R.id.textView7);
        this.clickCopy = (TextView) findViewById(R.id.textView6);
        this.clickCopy.setText("Click on image to copy your BTC address");
        
        this.viewAnimator = (ViewAnimator) findViewById(R.id.viewFlipper);
        
        this.walletsList = (ListView) findViewById(R.id.listView1);
        this.walletsAdapter = new WalletsAdapter(this);
        this.walletsList.setAdapter(this.walletsAdapter);
        
        this.connection = Connection.getInstance().initialize(this);
        
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        
        this.menuWalletsList = fm.findFragmentByTag("f1");
        if (this.menuWalletsList == null)
        {
        	this.menuWalletsList= new MenuWalletsList();
        	ft.add(this.menuWalletsList, "f1");
        }
        
        this.menuSingleWallet = fm.findFragmentByTag("f2");
        if (this.menuSingleWallet == null)
        {
        	this.menuSingleWallet = new MenuSingleWallet();
        	ft.add(this.menuSingleWallet, "f2");
        }
        ft.commit();
        
        this.wallets_db = new WalletsHandler(this);
        this.load();
   
        changeMenu();
        
        ActionItem detail = new ActionItem();
        detail.setActionId(ID_DETAIL);
        detail.setIcon(getResources().getDrawable(R.drawable.detail_dark));
        detail.setTitle("Detail");
        
        ActionItem delete = new ActionItem();
        delete.setActionId(ID_DELETE);
        delete.setIcon(getResources().getDrawable(R.drawable.delete_dark));
        delete.setTitle("Delete");
        
        ActionItem save = new ActionItem();
        save.setActionId(ID_SAVE);
        save.setIcon(getResources().getDrawable(R.drawable.email));
        save.setTitle("Save");
        
        ActionItem send = new ActionItem();
        send.setActionId(ID_SHARE);
        send.setIcon(getResources().getDrawable(R.drawable.share));
        send.setTitle("Share");
        
        final QuickAction mQuickAction  = new QuickAction(this);
        
        mQuickAction.addActionItem(detail);
        mQuickAction.addActionItem(delete);
        mQuickAction.addActionItem(save);
        mQuickAction.addActionItem(send);

         
        //setup the action item click listener
        mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
        	
        	@Override
			public void onItemClick(QuickAction source, int pos, int actionId) 
			{			
				// TODO Auto-generated method stub
				if (actionId == 1)
				{
					Toast.makeText(WalletsActivity.this, "Detail item selected", Toast.LENGTH_SHORT).show();
					flipToQrCode(wl);
				}
				else if (actionId == 2)
				{
					Toast.makeText(WalletsActivity.this, "Delete item selected", Toast.LENGTH_SHORT).show();
					walletsAdapter.removeItem(wl);
				}
				else if (actionId == 3)
				{
					
				}
				else if (actionId == 4)
				{
					
				}
					
			}
        });

		
		this.walletsList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				flipToQrCode((Wallet)walletsAdapter.getItem(position));
			}
		});
		
		
		
		this.walletsList.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) 
			{
				wl = (Wallet) walletsAdapter.getItem(position);
				
				mQuickAction.show(view);
				return false;
			}
		});
    }

    public void flipToQrCode(Wallet wallet)
    {
    	AnimationFactory.flipTransition(viewAnimator, FlipDirection.LEFT_RIGHT);
    	this.qr.setImageBitmap(QrCode.generateQrCode(wallet.getWallet_address(), 450, 450));
    	this.btcAddress.setText(wallet.getWallet_address());
    	changeMenu();
    }
    
    
    public void load()
    {
    	if (this.wallets_db.getAllWallets().size() > 0)
    	{
    		this.walletsAdapter.addItems(this.wallets_db.getAllWallets());
    	}
    	
    }
    
    public void Refresh()
    {
    	LoadingDialog dialog = LoadingDialog.newInstance("Waiting","Loading...");
    	dialog.show(getSupportFragmentManager(), "dialog");
    }

    public void export()
    {
    	/*StringBuilder wallets = new StringBuilder();
		
		
		if (wallets_db.getAllWallets().size() > 0)
		{
			
			wallets.append("Your Instawallets : ");
			wallets.append("\n");
			
			
			for (int i = 0 ; i < wallets_db.getAllWallets().size() ; i++)
			{
				wallets.append("Wallet ID " + (i+1) +" : " + wallets_db.getAllWallets().get(i).getWallet_id());
				wallets.append("\n");
			}
			
			Intent email = new Intent(Intent.ACTION_SEND);
			email.putExtra(Intent.EXTRA_SUBJECT, "Your Instawallets");
			email.putExtra(Intent.EXTRA_TEXT, wallets.toString());
			email.setType("text/plain");
			startActivity(Intent.createChooser(email, "Save Instawallets IDs"));
		}*/
    }
    
	public void onClick(View view) 
	{
		if (view.getId() == R.id.imageButton1)
		{
			final CharSequence[] items = { "Create a new wallet", "Scan existing wallet ID" , "Cancel" };
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Add a wallet");
			builder.setIcon(R.drawable.add);
			builder.setItems(items, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int item) 
				{
					if (items[item].equals("Create a new wallet"))
					{
						new addWallet().execute();
					}
					else if (items[item].equals("Scan existing wallet ID"))
					{
						Intent intent = new Intent("com.google.zxing.client.android.SCAN");
						intent.putExtra("SCAN_MODE", "QR_CODE_MODE");

						startActivityForResult(intent, REQUEST_CODE);
					}
				
				}
			});
			AlertDialog alert = builder.create();
			alert.show();		
			
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
			if (this.currentView() == R.id.flip1)
			{
				alertingDialog = AlertingDialog.newInstance("Warning", "Please select a wallet to share" ,R.drawable.warning);			  									
				alertingDialog.show(getSupportFragmentManager(), "no selecting wallet");
			}
			else if (this.currentView() == R.id.flip2)
			{
				Toast.makeText(WalletsActivity.this, "copy qr code to clip board", Toast.LENGTH_SHORT).show();
			}
		}
		changeMenu();
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) 
	{
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == REQUEST_CODE) 
		{
			if (resultCode == RESULT_OK) 
			{
				String link = intent.getStringExtra("SCAN_RESULT");
				//String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				
				System.out.println("String result : " + link);
			}
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
	
	public static class MenuWalletsList extends SherlockFragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState) 
		{
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);
		}
		
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
		{
			// TODO Auto-generated method stub
			super.onCreateOptionsMenu(menu, inflater);
			MenuItem refresh = menu.add(0,0,0,"Refresh");
	    	{
	    		refresh.setIcon(R.drawable.ic_refresh);
	    		refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);		
	    	}
	    	
	    	MenuItem about = menu.add(0,0,1,"About");
	    	{
	    		about.setIcon(R.drawable.about);
	    		about.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);		
	    	}
		}
	}
	
	public static class MenuSingleWallet extends SherlockFragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState) 
		{
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);
		}
		
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
		{
			// TODO Auto-generated method stub
			super.onCreateOptionsMenu(menu, inflater);
			
			MenuItem refresh = menu.add(0,0,0,"Refresh");
	    	{
	    		refresh.setIcon(R.drawable.ic_refresh);
	    		refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);		
	    	}
	    	
	    	MenuItem send = menu.add(0,0,1,"Send coins");
	    	{
	    		send.setIcon(R.drawable.send);
	    		send.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);		
	    	}
	    	
	    	MenuItem rename = menu.add(0,0,2,"Change Wallet Name");
	    	{
	    		rename.setIcon(R.drawable.remane);
	    		rename.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);		
	    	}
	    	
	    	MenuItem delete = menu.add(0,0,3,"Release this Wallet");
	    	{
	    		delete.setIcon(R.drawable.delete);
	    		delete.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);		
	    	}
		}
	}
	
	
	@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) 
	{
        super.onRestoreInstanceState(savedInstanceState);
        // Make sure fragments are updated after check box view state is restored.
        changeMenu();
    }
	
	
	public void changeMenu()
	{
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		
		if (this.currentView() == R.id.flip1)
		{
			ft.show(this.menuWalletsList);
		}
		else
		{
			ft.hide(menuWalletsList);
		}
		
		if (this.currentView() == R.id.flip2)
		{
			ft.show(menuSingleWallet);
		}
		else
		{
			ft.hide(menuSingleWallet);
		}
		ft.commit();
	}
	
	
	public boolean notEmpty(String s) 
	{
		return (s != null && s.length() > 0);
	}
	
    @Override
    protected void onPause() 
    {
    	// TODO Auto-generated method stub
    	super.onPause();
    	
    }
    
}