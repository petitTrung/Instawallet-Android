package com.paymium.instawallet.wallet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.ClipboardManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.paymium.instawallet.R;
import com.paymium.instawallet.database.WalletsHandler;
import com.paymium.instawallet.dialog.AlertingDialogOneButton;
import com.paymium.instawallet.dialog.LoadingDialog;
import com.paymium.instawallet.exception.ConnectionNotInitializedException;
import com.paymium.instawallet.flip.AnimationFactory;
import com.paymium.instawallet.flip.AnimationFactory.FlipDirection;
import com.paymium.instawallet.json.NewWallet;
import com.paymium.instawallet.market.MarketPrices;
import com.paymium.instawallet.qrcode.QrCode;
import com.paymium.instawallet.send.SendActivity;


public class WalletsActivity extends SherlockFragmentActivity implements OnClickListener 
{
	private ListView walletsList;
	private WalletsAdapter walletsAdapter;
	
	private ViewAnimator viewAnimator;
	
	private Connection connection;
	
	private Fragment menuWalletsList;
	private Fragment menuSingleWallet;
	
	private static final int REQUEST_CODE = 1;
	private static final int REQUEST_SEND = 2;
	
	private WalletsHandler wallets_db;

	private static final int ID_DETAIL = 1;
	private static final int ID_DELETE = 2;
	private static final int ID_SAVE = 3;
	private static final int ID_SHARE = 4;
	private Wallet wl;
	
	private FragmentManager fragmentManager;
	
	private LinearLayout addLayout,exportLayout,shareLayout;
	private ImageButton add,export,share;
	
	private ImageView qr;
	private TextView btcAddress;
	private TextView balance;
	
	private LinkedList<String> walletsIdList;
	private ArrayList<String> addressBitcoin;
	
	private MarketPrices marketPrices;
	private TextView usd,eur,gbp;
	
	private SharedPreferences prefs;
	private Editor editor;
	private String USD,EUR,GBP;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        setTheme(R.style.Theme_Sherlock_ForceOverflow);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallets);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        this.addLayout = (LinearLayout) findViewById(R.id.add);
        this.addLayout.setOnClickListener(this);
        
        this.exportLayout = (LinearLayout) findViewById(R.id.export);
        this.exportLayout.setOnClickListener(this);
        
        this.shareLayout = (LinearLayout) findViewById(R.id.share);
        this.shareLayout.setOnClickListener(this);
        
        this.add = (ImageButton) findViewById(R.id.imageButton1);
        this.add.setOnClickListener(this);
        
        this.export = (ImageButton) findViewById(R.id.imageButton2);
        this.export.setOnClickListener(this);
        
        this.share = (ImageButton) findViewById(R.id.imageButton3);
        this.share.setOnClickListener(this);
            
        this.qr = (ImageView) findViewById(R.id.imageView1);
        this.btcAddress = (TextView) findViewById(R.id.textView7);
        this.balance = (TextView) findViewById(R.id.textView6);
        
        this.viewAnimator = (ViewAnimator) findViewById(R.id.viewFlipper);
        
        this.usd = (TextView) findViewById(R.id.textView3);
        this.eur = (TextView) findViewById(R.id.textView4);
        this.gbp = (TextView) findViewById(R.id.textView5);
        
		this.prefs = getSharedPreferences("XXX", MODE_PRIVATE);
		this.editor = prefs.edit();
        
        this.walletsList = (ListView) findViewById(R.id.listView1);
        this.walletsAdapter = new WalletsAdapter(this);
        this.walletsList.setAdapter(this.walletsAdapter);
        
        this.connection = Connection.getInstance().initialize(this);
        
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        
        this.menuWalletsList = fragmentManager.findFragmentByTag("f1");
        if (this.menuWalletsList == null)
        {
        	this.menuWalletsList= new MenuWalletsList();
        	ft.add(this.menuWalletsList, "f1");
        }
        
        this.menuSingleWallet = fragmentManager.findFragmentByTag("f2");
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
        mQuickAction.addActionItem(save);
        mQuickAction.addActionItem(send);
        mQuickAction.addActionItem(delete);
        

         
        //setup the action item click listener
        mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
        	
        	@Override
			public void onItemClick(QuickAction source, int pos, int actionId) 
			{			
				// TODO Auto-generated method stub
				if (actionId == ID_DETAIL)
				{
					flipToQrCode(wl);
				}
				else if (actionId == ID_SAVE)
				{
					exportItem();
				}
				else if (actionId == ID_SHARE)
				{
					shareItem();
				}
				else if (actionId == ID_DELETE)
				{
					deleteItem();
				}
				
			}
        });

		
		this.walletsList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				wl = (Wallet) walletsAdapter.getItem(position);
				
				flipToQrCode(wl);
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

		this.walletIdExtractor = new WalletIdExtractor();
		
		

	}
    

    
    public void flipToQrCode(Wallet wallet)
    {
    	AnimationFactory.flipTransition(viewAnimator, FlipDirection.LEFT_RIGHT);
    	
    	this.qr.setImageBitmap(QrCode.generateQrCode(wallet.getWallet_address(), 450, 450));
    	this.btcAddress.setText(wallet.getWallet_address());
    	this.balance.setText("Balance : " + wallet.getWallet_balance().toString() + " BTC");
    	
    	changeMenu();
    }
    
    
    public void load()
    {
    	if (this.wallets_db.getAllWallets().size() > 0)
    	{
    		this.walletsAdapter.addItems(this.wallets_db.getAllWallets());
    	}
    	
    	//---Check connectivity state
    	ConnectivityManager connectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean isConnected = (connectionManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || 
								connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI  ).getState() == NetworkInfo.State.CONNECTED );	
		
		USD = this.prefs.getString("usd", "");
		EUR = this.prefs.getString("eur", "");
		GBP = this.prefs.getString("gbp", "");
		
		if (!USD.equals("") && !EUR.equals("") && !GBP.equals(""))
		{
			this.usd.setText("1 BTC = " + USD + " USD");
			this.eur.setText("1 BTC = " + EUR + " EUR");
			this.gbp.setText("1 BTC = " + GBP + " GBP");
		}
		else
		{
			if (isConnected)
			{
				try 
				{
					this.marketPrices = connection.getMarketPrices();
					
					USD = this.marketPrices.getUsd().getDay();
					EUR = this.marketPrices.getEur().getDay();
					GBP = this.marketPrices.getGbp().getDay();
					
					this.editor.putString("usd", USD);
					this.editor.putString("eur", EUR);
					this.editor.putString("gbp", GBP);
					this.editor.commit();
					
					this.usd.setText("1 BTC = " + USD + " USD");
					this.eur.setText("1 BTC = " + EUR + " EUR");
					this.gbp.setText("1 BTC = " + GBP + " GBP");
					
				} 
				catch (IOException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				catch (ConnectionNotInitializedException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if(!isConnected)
			{
				this.usd.setText("Unavailable"); 
				this.eur.setText("Unavailable"); 
				this.gbp.setText("Unavailable"); 
			}
		}
		
    }
    
    public void refresh()
    {
    	if (this.currentView() == R.id.flip1)
    	{
    		String[] data = new String[this.wallets_db.getAllWalletsID().size()];
    		this.wallets_db.getAllWalletsID().toArray(data);
    		
    		new refreshAllWallets().execute(data);
    	}
    	else if (this.currentView() == R.id.flip2)
    	{
    		String[] data = new String[]{wl.getWallet_id()};
    		
    		new refreshWallet().execute(data);
    	}
    
    }
    
    public class refreshAllWallets extends AsyncTask<String, Integer, String>
    {
    	private LinkedList<Wallet> update;
    	
    	public refreshAllWallets() 
    	{
			// TODO Auto-generated constructor stub
    		this.update = new LinkedList<Wallet>();
		}
    	
    	@Override
    	protected void onPreExecute() 
    	{
    		// TODO Auto-generated method stub
    		super.onPreExecute();
    		loadingDialog = LoadingDialog.newInstance("Please wait", "Loading ...");			  									
			loadingDialog.show(getSupportFragmentManager(), "loading dialog all refresh");
    	}

		@Override
		protected String doInBackground(String... params) 
		{
			// TODO Auto-generated method stub
			for (int i = 0 ; i < params.length ; i++)
			{
				System.out.println(params[i]);
			}
			
			
			String[] walletsIDList = params;
			
			for(int i = 0 ; i < walletsIDList.length ; i++ )
			{
				try 
				{
					this.update.add(connection.getWallet(walletsIDList[i]));
					
					if (i == 0)
					{
						marketPrices = connection.getMarketPrices();
						
						USD = marketPrices.getUsd().getDay();
						EUR = marketPrices.getEur().getDay();
						GBP = marketPrices.getGbp().getDay();
						
						editor.putString("usd", USD);
						editor.putString("eur", EUR);
						editor.putString("gbp", GBP);
						editor.commit();
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
			}
			
			return "OK";
		}
		
		@Override
		protected void onPostExecute(String result) 
		{
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			loadingDialog.dismiss();
			
			if (result.equals("no connection"))
			{
				alertingDialogOneButton = AlertingDialogOneButton.newInstance("Fail !!", "No connection, no wallet has been created", R.drawable.error);
				alertingDialogOneButton.show(getSupportFragmentManager(), "error 1 alerting dialog");
			}
			else if(result.equals("slow connection"))
			{
				alertingDialogOneButton = AlertingDialogOneButton.newInstance("Fail !!", "Slow connection, no wallet has been created", R.drawable.error);
				alertingDialogOneButton.show(getSupportFragmentManager(), "error 2 alerting dialog");
			}
			else if (result.equals("OK"))
			{
				usd.setText("1 BTC = " + USD + " USD");
				eur.setText("1 BTC = " + EUR + " EUR");
				gbp.setText("1 BTC = " + GBP + " GBP");			
				
				for (int i = 0 ; i < this.update.size() ; i++ )
				{
					walletsAdapter.updateItem(this.update.get(i));
				}
				
				
				alertingDialogOneButton = AlertingDialogOneButton.newInstance("Successful !!", "All of wallets have been updated !", R.drawable.ok);
				alertingDialogOneButton.show(getSupportFragmentManager(), "ok alerting dialog");
			}
		}

    }
    
    public class refreshWallet extends AsyncTask<String, Integer, String>
    {
    	private Wallet wallet;
    	
    	public refreshWallet() 
    	{
			// TODO Auto-generated constructor stub
    		this.wallet = new Wallet();
		}

    	@Override
    	protected void onPreExecute() 
    	{
    		// TODO Auto-generated method stub
    		super.onPreExecute();
    		loadingDialog = LoadingDialog.newInstance("Please wait", "Loading ...");			  									
			loadingDialog.show(getSupportFragmentManager(), "loading dialog refresh");
    	}
    	
		@Override
		protected String doInBackground(String... params) 
		{
			// TODO Auto-generated method stub
			String wallet_id = params[0];
			
			try 
			{
				wallet = connection.getWallet(wallet_id);
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
	
			return "OK";
		}
		
		@Override
		protected void onPostExecute(String result) 
		{
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			loadingDialog.dismiss();
			
			if (result.equals("no connection"))
			{
				alertingDialogOneButton = AlertingDialogOneButton.newInstance("Fail !!", "No connection, no wallet has been created", R.drawable.error);
				alertingDialogOneButton.show(getSupportFragmentManager(), "error 1 alerting dialog");
			}
			else if(result.equals("slow connection"))
			{
				alertingDialogOneButton = AlertingDialogOneButton.newInstance("Fail !!", "Slow connection, no wallet has been created", R.drawable.error);
				alertingDialogOneButton.show(getSupportFragmentManager(), "error 2 alerting dialog");
			}
			else if (result.equals("OK"))
			{
				walletsAdapter.updateItem(wallet);
				
				alertingDialogOneButton = AlertingDialogOneButton.newInstance("Successful !!", "This wallet has been updated !", R.drawable.ok);
				alertingDialogOneButton.show(getSupportFragmentManager(), "ok alerting dialog");
			}
		}
    	
    }
    
  
    public void onClick(View view) 
	{
		if (view.getId() == R.id.imageButton1 || view.getId() == R.id.add)
		{		
			add();
		}
		else if (view.getId() == R.id.imageButton2 || view.getId() == R.id.export)
		{		
			export();		
		}
		else if (view.getId() == R.id.imageButton3 || view.getId() == R.id.share)
		{
			share();
		}
		
		changeMenu();	
	}
    
    public void add()
    {
    	final CharSequence[] items = { "Create a new wallet", "Scan an existing wallet ID" };
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Add a wallet");
		builder.setIcon(R.drawable.wallet);
		builder.setItems(items, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int item) 
			{
				if (items[item].equals("Create a new wallet"))
				{
					new createWallet().execute();
					
					if(currentView() == R.id.flip2)
					{
						AnimationFactory.flipTransition(viewAnimator, FlipDirection.RIGHT_LEFT);
						changeMenu();
					}
				}
				else if (items[item].equals("Scan an existing wallet ID"))
				{
					Intent intent = new Intent("com.google.zxing.client.android.SCAN");
					intent.putExtra("SCAN_MODE", "QR_CODE_MODE");

					startActivityForResult(intent, REQUEST_CODE);
				}
			
			}
		});
		
		builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				//Toast.makeText(getActivity(), "click on Cancel", Toast.LENGTH_LONG);
			}
		});	
		
		AlertDialog alert = builder.create();
		alert.show();
    }
    
    public void export()
    {
    	if (this.currentView() == R.id.flip1)
    	{
    		StringBuilder wallets = new StringBuilder();
	
    		if (wallets_db.getAllWallets().size() > 0)
    		{
    			
    			wallets.append("Your Instawallets IDs : ");
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
    		}
    	}
    	
    	else if (this.currentView() == R.id.flip2)
    	{
    		exportItem();
    	}
    	
    }
    public void exportItem()
    {
    	StringBuilder wallets = new StringBuilder();
		
		wallets.append("Your Instawallet ID : ");
		wallets.append("\n"); 		
		wallets.append(wl.getWallet_id());
		wallets.append("\n");
		
		Intent email = new Intent(Intent.ACTION_SEND);
		email.putExtra(Intent.EXTRA_SUBJECT, "Your Instawallet");
		email.putExtra(Intent.EXTRA_TEXT, wallets.toString());
		email.setType("text/plain");
		startActivity(Intent.createChooser(email, "Save Instawallets IDs"));
    }
    
    
    public void share()
    {
    	if (this.currentView() == R.id.flip1)
		{
    		alertingDialogOneButton = AlertingDialogOneButton.newInstance("Warning", "Please select a wallet to share" ,R.drawable.warning);			  									
    		alertingDialogOneButton.show(getSupportFragmentManager(), "no selecting wallet");
		}
		else if (this.currentView() == R.id.flip2)
		{
			shareItem();
		}
    }
	public void shareItem()
	{
		final CharSequence[] items = { "Send via Email", "Send via SMS", "Copy to clipboard" };
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Send bitcoin address");
		builder.setIcon(R.drawable.share_bitcoin_address);
		builder.setItems(items, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int item) 
			{
				if (items[item].equals("Send via Email"))
				{
					StringBuilder wallets = new StringBuilder();
		    		
		    		wallets.append("Please send your bitcoins to this address : ");
		    		wallets.append("\n"); 		
		    		wallets.append(wl.getWallet_address());
		    		wallets.append("\n");
		    		
		    		Intent email = new Intent(Intent.ACTION_SEND);
					email.putExtra(Intent.EXTRA_SUBJECT, "Request bitcoins");
					email.putExtra(Intent.EXTRA_TEXT, wallets.toString());
					email.setType("text/plain");
					startActivity(Intent.createChooser(email, "Request bitcoins"));
				}
				else if (items[item].equals("Send via SMS"))
				{
					StringBuilder wallets = new StringBuilder();
		    		
		    		wallets.append("Please send your bitcoins to this address : ");
		    		wallets.append("\n"); 		
		    		wallets.append(wl.getWallet_address());
		    		wallets.append("\n");
		    		
					Intent sms = new Intent(android.content.Intent.ACTION_VIEW);
					sms.putExtra("address","");
					sms.putExtra("sms_body", wallets.toString());
					sms.setType("vnd.android-dir/mms-sms");
					startActivity(sms);
				}
				else if (items[item].equals("Copy to clipboard"))
				{					
					ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
					clipboard.setText(wl.getWallet_address());
					
					Toast.makeText(WalletsActivity.this, "The bitcoin address of this wallet is copied to clipboard", Toast.LENGTH_LONG).show();
					
				}
			
			}
		});
		
		builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				//Toast.makeText(getActivity(), "click on Cancel", Toast.LENGTH_LONG);
			}
		});	
		
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public void deleteItem()
	{
		walletsAdapter.removeItem(wl);
	}
	
	
	private WalletIdExtractor walletIdExtractor;
	
	
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
				
				//System.out.println("String result : " + link);
				
				walletsIdList = new LinkedList<String>();
				
 	           	this.walletIdExtractor = new WalletIdExtractor();
 	           	walletsIdList = this.walletIdExtractor.extract(link);				
			}
		}
	}
	
	@Override
	protected void onStart() 
	{
		// TODO Auto-generated method stub
		super.onStart();
		
		if (walletsIdList != null)
		{
			/*for (int i = 0 ; i < walletsIdList.size();i++)
			{
	    		System.out.println( walletsIdList.get(i).toString());
	    	}	*/
	        	
			
			if (walletsIdList.size() == 0)
			{
				alertingDialogOneButton = AlertingDialogOneButton.newInstance("Warning", "No wallet id found", R.drawable.warning);
				alertingDialogOneButton.show(getSupportFragmentManager(), "No id found");
			}
			else if (walletsIdList.size() > 1)
			{
				alertingDialogOneButton = AlertingDialogOneButton.newInstance("Warning", "There are more than one wallet id found", R.drawable.warning);
				alertingDialogOneButton.show(getSupportFragmentManager(), "More than one ids found");
			}
			else
			{
				if (this.walletsAdapter.isIncluded(walletsIdList.get(0)))
				{
					alertingDialogOneButton = AlertingDialogOneButton.newInstance("Warning", "This wallet was already existed in your list !!", R.drawable.warning);
					alertingDialogOneButton.show(getSupportFragmentManager(), "This wallet is existed in DB");
				}
				else
				{
					String[] data = new String[]{walletsIdList.get(0)}; 
					walletsIdList.clear();
					new addWallet().execute(data);
				}
				
			}
		}
		
		if (addressBitcoin != null)
		{
			if (addressBitcoin.size() == 0)
			{
				alertingDialogOneButton = AlertingDialogOneButton.newInstance("Warning", "No bitcoin address found", R.drawable.warning);
				alertingDialogOneButton.show(getSupportFragmentManager(), "No bitcoin address found");
			}
			else if (addressBitcoin.size() > 1)
			{
				alertingDialogOneButton = AlertingDialogOneButton.newInstance("Warning", "There are more than one bitcoin address found", R.drawable.warning);
				alertingDialogOneButton.show(getSupportFragmentManager(), "More than one btc address found");
			}
			else
			{
				AddressBitcoinValidator addressBitcoinValidator = new AddressBitcoinValidator();
				if (addressBitcoinValidator.validate(addressBitcoin.get(0)))
				{
					Intent send  = new Intent(this, SendActivity.class);
					send.putExtra("address", addressBitcoin.get(0));
					send.putExtra("wallet_id", wl.getWallet_id());
					
					addressBitcoin = null;
					
					startActivity(send);

				}
				else
				{
					alertingDialogOneButton = AlertingDialogOneButton.newInstance("Warning", "This address is invalid !!", R.drawable.warning);
					alertingDialogOneButton.show(getSupportFragmentManager(), "Invalid btc address");
				}
			}
		}
				
	}
	
	
	public void changeName()
	{
		
	}
	
	
	public int currentView()
	{
		return this.viewAnimator.getCurrentView().getId(); 
	}

	private AlertingDialogOneButton alertingDialogOneButton;
	private LoadingDialog loadingDialog;
	
	public class addWallet extends AsyncTask<String, Integer, Object>
	{
		
		
		@Override
		protected void onPreExecute() 
		{ 
			super.onPreExecute();
			
			loadingDialog = LoadingDialog.newInstance("Please wait", "Loading ...");			  									
			loadingDialog.show(getSupportFragmentManager(), "loading dialog add");
	    } 

		@Override
		protected Object doInBackground(String... data) 
		{
			// TODO Auto-generated method stub
			Wallet wallet = null;
			System.out.println("data : " + data[0]);
			try 
			{
				String wallet_id = data[0];
				String wallet_address = connection.getAddressJson(wallet_id).getAddress();
				BigDecimal wallet_balance = connection.getBalanceJson(wallet_id).getBalance().divide(new BigDecimal(Math.pow(10, 8)));
				
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
				
				alertingDialogOneButton = AlertingDialogOneButton.newInstance("Successful !!", "A wallet has been added", R.drawable.ok);
				alertingDialogOneButton.show(getSupportFragmentManager(), "ok alerting dialog");
			}
			else if (result.getClass().getSimpleName().equals("String"))
			{
				if (result.equals("no connection"))
				{
					alertingDialogOneButton = AlertingDialogOneButton.newInstance("Fail !!", "No connection, no wallet has been created", R.drawable.error);
					alertingDialogOneButton.show(getSupportFragmentManager(), "error 1 alerting dialog");
				}
				else if (result.equals("slow connection"))
				{
					alertingDialogOneButton = AlertingDialogOneButton.newInstance("Fail !!", "Slow connection, no wallet has been created", R.drawable.error);
					alertingDialogOneButton.show(getSupportFragmentManager(), "error 2 alerting dialog");
				}
				else if (result.equals("fail"))
				{
					alertingDialogOneButton = AlertingDialogOneButton.newInstance("Fail !!", "Error unknown", R.drawable.error);
					alertingDialogOneButton.show(getSupportFragmentManager(), "error 3 alerting dialog");
				}
			}
		}
	}
	
	
	public class createWallet extends AsyncTask<String, Integer, Object>
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
				
				alertingDialogOneButton = AlertingDialogOneButton.newInstance("Successful !!", "A wallet has been added", R.drawable.ok);
				alertingDialogOneButton.show(getSupportFragmentManager(), "ok alerting dialog");
			}
			else if (result.getClass().getSimpleName().equals("String"))
			{
				if (result.equals("no connection"))
				{
					alertingDialogOneButton = AlertingDialogOneButton.newInstance("Fail !!", "No connection, no wallet has been created", R.drawable.error);
					alertingDialogOneButton.show(getSupportFragmentManager(), "error 1 alerting dialog");
				}
				else if (result.equals("slow connection"))
				{
					alertingDialogOneButton = AlertingDialogOneButton.newInstance("Fail !!", "Slow connection, no wallet has been created", R.drawable.error);
					alertingDialogOneButton.show(getSupportFragmentManager(), "error 2 alerting dialog");
				}
				else if (result.equals("fail"))
				{
					alertingDialogOneButton = AlertingDialogOneButton.newInstance("Fail !!", "Error unknown", R.drawable.error);
					alertingDialogOneButton.show(getSupportFragmentManager(), "error 3 alerting dialog");
				}
			}
		}
		
	}
	
	public class MenuWalletsList extends SherlockFragment
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
	    	
	    	MenuItem about = menu.add(0,1,1,"About");
	    	{
	    		about.setIcon(R.drawable.about);
	    		about.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);		
	    	}
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) 
		{
			return MenuChoice(item);
		}
		
		public boolean MenuChoice(MenuItem item)
		{
			switch (item.getItemId()) 
			{
				case 0:
					
					refresh(); 
					
					return true;
					
				case 1:
					
					
					
					return true;
					
			};
			
			return false;
		}
	}
	
	public class MenuSingleWallet extends SherlockFragment
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
	    	
	    	MenuItem send = menu.add(0,1,1,"Send coins");
	    	{
	    		send.setIcon(R.drawable.send);
	    		send.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);		
	    	}
	    	
	    	MenuItem delete = menu.add(0,3,3,"Release this Wallet");
	    	{
	    		delete.setIcon(R.drawable.delete);
	    		delete.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);		
	    	}

		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) 
		{
			return MenuChoice(item);
		}
		
		public boolean MenuChoice(MenuItem item)
		{
			switch (item.getItemId()) 
			{
				case 0:
					
					refresh();
					
					return true;
					
				case 1:
					
					Intent intent = new Intent("com.google.zxing.client.android.SCAN");
					intent.putExtra("SCAN_MODE", "QR_CODE_MODE");

					startActivityForResult(intent, REQUEST_SEND);
					
					return true;
					
				case 2:
					
					return true;
					
				case 3:
					
					AnimationFactory.flipTransition(viewAnimator, FlipDirection.LEFT_RIGHT);
					
					changeMenu();
					
					deleteItem();
					
					return true;
					
				case android.R.id.home:
					
					AnimationFactory.flipTransition(viewAnimator, FlipDirection.LEFT_RIGHT);
					
					changeMenu();
					
					return true;
					
			};
			
			return false;
		}
		
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent intent) 
		{
			// TODO Auto-generated method stub
			super.onActivityResult(requestCode, resultCode, intent);
			
			ExtractAddressBitcoin extractAddressBitcoin = new ExtractAddressBitcoin();
			
			if (requestCode == REQUEST_SEND) 
			{
				if (resultCode == RESULT_OK) 
				{
					String address = intent.getStringExtra("SCAN_RESULT");
					//String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

					System.out.println("Bitcoin Address : " + address);
					
					addressBitcoin = new ArrayList<String>();

					addressBitcoin = extractAddressBitcoin.extract(address);
				}
			}
		}
	}
	
	
	
	
	
	public void changeMenu()
	{
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		
				
		if (this.currentView() == R.id.flip1)
		{
			ft.show(this.menuWalletsList);
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);	
		}
		else
		{
			ft.hide(this.menuWalletsList);
		}
		
		if (this.currentView() == R.id.flip2)
		{
			ft.show(this.menuSingleWallet);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);	
		}
		else
		{
			ft.hide(this.menuSingleWallet);
		}
		ft.commit();
	}
	
	
	public boolean notEmpty(String s) 
	{
		return (s != null && s.length() > 0);
	}
	
    
    
}