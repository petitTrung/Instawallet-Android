package com.paymium.instawallet.wallet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
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
	
	private IntentFilter intentFilter;	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        setTheme(R.style.Theme_Sherlock_ForceOverflow);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallets);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        if (android.os.Build.VERSION.SDK_INT >= 11)
        {
        	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        	StrictMode.setThreadPolicy(policy);
        }

        this.intentFilter = new IntentFilter();
        this.intentFilter.addAction("SENT");
        
        this.registerReceiver(intentReceiver, intentFilter);
        
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
        
        this.fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = this.fragmentManager.beginTransaction();
        
        this.menuWalletsList = this.fragmentManager.findFragmentByTag("f1");
        if (this.menuWalletsList == null)
        {
        	this.menuWalletsList= new MenuWalletsList();
        	ft.add(this.menuWalletsList, "f1");
        }
        
        this.menuSingleWallet = this.fragmentManager.findFragmentByTag("f2");
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
        detail.setTitle(getResources().getString(R.string.detail));
        
        ActionItem delete = new ActionItem();
        delete.setActionId(ID_DELETE);
        delete.setIcon(getResources().getDrawable(R.drawable.delete_dark));
        delete.setTitle(getResources().getString(R.string.remove));
        
        ActionItem save = new ActionItem();
        save.setActionId(ID_SAVE);
        save.setIcon(getResources().getDrawable(R.drawable.email));
        save.setTitle(getResources().getString(R.string.save));
        
        ActionItem send = new ActionItem();
        send.setActionId(ID_SHARE);
        send.setIcon(getResources().getDrawable(R.drawable.share));
        send.setTitle(getResources().getString(R.string.share_item));
        
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
    	AnimationFactory.flipTransition(this.viewAnimator, FlipDirection.LEFT_RIGHT);
    	
    	this.qr.setImageBitmap(QrCode.generateQrCode(wallet.getWallet_address(), 450, 450));
    	this.btcAddress.setText(wallet.getWallet_address());
    	this.balance.setText(getResources().getString(R.string.balance) + " : " + wallet.getWallet_balance().toString() + " BTC");
    	
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
					e.printStackTrace();
				} 
				catch (ConnectionNotInitializedException e) 
				{
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
    		this.update = new LinkedList<Wallet>();
		}
    	
    	@Override
    	protected void onPreExecute() 
    	{
    		super.onPreExecute();
    		
    		loadingDialog = LoadingDialog.newInstance(getResources().getString(R.string.please_wait), 
    												getResources().getString(R.string.refresh_all));			  									
			loadingDialog.show(getSupportFragmentManager(), "loading dialog all refresh");
    	}

		@Override
		protected String doInBackground(String... params) 
		{
			/*for (int i = 0 ; i < params.length ; i++)
			{
				System.out.println(params[i]);
			}*/
			
			
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
			super.onPostExecute(result);
			
			loadingDialog.dismiss();
			
			if (result.equals("no connection"))
			{
				alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.fail), 
																			getResources().getString(R.string.no_connection_no_update_all_wallets), 
																			R.drawable.error);
				alertingDialogOneButton.show(getSupportFragmentManager(), "error 1 alerting dialog");
			}
			else if(result.equals("slow connection"))
			{
				alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.fail), 
																			getResources().getString(R.string.slow_connection_no_update_all_wallets), 
																			R.drawable.error);
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
				
				
				alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.successful), 
																			getResources().getString(R.string.all_wallets_updated), 
																			R.drawable.ok);
				alertingDialogOneButton.show(getSupportFragmentManager(), "ok alerting dialog");
			}
		}

    }
    
    public class refreshWallet extends AsyncTask<String, Integer, String>
    {
    	private Wallet wallet;
    	
    	public refreshWallet() 
    	{
    		this.wallet = new Wallet();
		}

    	@Override
    	protected void onPreExecute() 
    	{
    		super.onPreExecute();
    		loadingDialog = LoadingDialog.newInstance(getResources().getString(R.string.please_wait),
    												getResources().getString(R.string.refresh));
    		
			loadingDialog.show(getSupportFragmentManager(), "loading dialog refresh");
    	}
    	
		@Override
		protected String doInBackground(String... params) 
		{
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
			super.onPostExecute(result);
			
			loadingDialog.dismiss();
			
			if (result.equals("no connection"))
			{
				alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.fail), 
																			getResources().getString(R.string.no_connection_no_update_wallet), 
																			R.drawable.error);
				alertingDialogOneButton.show(getSupportFragmentManager(), "error 1 alerting dialog");
			}
			else if(result.equals("slow connection"))
			{
				alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.fail), 
																			getResources().getString(R.string.slow_connection_no_update_wallet), 
																			R.drawable.error);
				alertingDialogOneButton.show(getSupportFragmentManager(), "error 2 alerting dialog");
			}
			else if (result.equals("OK"))
			{
				walletsAdapter.updateItem(wallet);
				
				alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.successful), 
																			getResources().getString(R.string.wallet_updated), 
																			R.drawable.ok);
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
    	final CharSequence[] items = { getResources().getString(R.string.create_wallet), 
    									getResources().getString(R.string.scan_id)};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.add_wallet));
		builder.setIcon(R.drawable.wallet);
		builder.setItems(items, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int item) 
			{
				if (item == 0)
				{
					new createWallet().execute();
					
					if(currentView() == R.id.flip2)
					{
						AnimationFactory.flipTransition(viewAnimator, FlipDirection.RIGHT_LEFT);
						changeMenu();
					}
				}
				else if (item == 1)
				{
					Intent intent = new Intent("com.google.zxing.client.android.SCAN");
					intent.putExtra("SCAN_MODE", "QR_CODE_MODE");

					startActivityForResult(intent, REQUEST_CODE);
				}
			
			}
		});
		
		builder.setNeutralButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				
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
	
    		if (this.wallets_db.getAllWallets().size() > 0)
    		{
    			
    			wallets.append(getResources().getString(R.string.your_wallets_ids));
    			wallets.append("\n");
    			
    			
    			for (int i = 0 ; i < this.wallets_db.getAllWallets().size() ; i++)
    			{
    				wallets.append("Wallet ID " + (i+1) +" : " + this.wallets_db.getAllWallets().get(i).getWallet_id());
    				wallets.append("\n");
    			}
    			
    			Intent email = new Intent(Intent.ACTION_SEND);
    			email.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.your_wallets_ids));
    			email.putExtra(Intent.EXTRA_TEXT, wallets.toString());
    			email.setType("text/plain");
    			startActivity(Intent.createChooser(email, getResources().getString(R.string.save_wallets_ids)));
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
		
		wallets.append(getResources().getString(R.string.your_wallet_id));
		wallets.append("\n"); 		
		wallets.append(wl.getWallet_id());
		wallets.append("\n");
		
		Intent email = new Intent(Intent.ACTION_SEND);
		email.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.your_wallet_id));
		email.putExtra(Intent.EXTRA_TEXT, wallets.toString());
		email.setType("text/plain");
		startActivity(Intent.createChooser(email, getResources().getString(R.string.save_wallet_id)));
    }
    
    
    public void share()
    {
    	if (this.currentView() == R.id.flip1)
		{
    		this.alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.warning), 
    																	getResources().getString(R.string.please_select_wallet), 
    																	R.drawable.warning);			  									
    		this.alertingDialogOneButton.show(getSupportFragmentManager(), "no selecting wallet");
		}
		else if (this.currentView() == R.id.flip2)
		{
			shareItem();
		}
    }
	public void shareItem()
	{
		final CharSequence[] items = { getResources().getString(R.string.send_via_email), 
									getResources().getString(R.string.send_via_sms), 
									getResources().getString(R.string.copy_to_clipboard) };
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.send_btc_address));
		builder.setIcon(R.drawable.share_bitcoin_address);
		builder.setItems(items, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int item) 
			{
				if (item == 0)
				{
					StringBuilder wallets = new StringBuilder();
		    		
		    		wallets.append(getResources().getString(R.string.please_send_to_address));
		    		wallets.append("\n"); 		
		    		wallets.append(wl.getWallet_address());
		    		wallets.append("\n");
		    		
		    		Intent email = new Intent(Intent.ACTION_SEND);
					email.putExtra(Intent.EXTRA_SUBJECT, "Request bitcoins");
					email.putExtra(Intent.EXTRA_TEXT, wallets.toString());
					email.setType("text/plain");
					startActivity(Intent.createChooser(email, "Request bitcoins"));
				}
				else if (item == 1)
				{
					StringBuilder wallets = new StringBuilder();
		    		
		    		wallets.append(getResources().getString(R.string.please_send_to_address));
		    		wallets.append("\n"); 		
		    		wallets.append(wl.getWallet_address());
		    		wallets.append("\n");
		    		
					Intent sms = new Intent(android.content.Intent.ACTION_VIEW);
					sms.putExtra("address","");
					sms.putExtra("sms_body", wallets.toString());
					sms.setType("vnd.android-dir/mms-sms");
					startActivity(sms);
				}
				else if (item == 2)
				{					
					ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
					clipboard.setText(wl.getWallet_address());
					
					Toast.makeText(WalletsActivity.this, getResources().getString(R.string.is_copied_to_clipboard), Toast.LENGTH_LONG).show();
					
				}
			
			}
		});
		
		builder.setNeutralButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
		this.walletsAdapter.removeItem(wl);
	}
	
	
	private WalletIdExtractor walletIdExtractor;
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) 
	{
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
		super.onStart();
		
		if (this.walletsIdList != null)
		{
			/*for (int i = 0 ; i < walletsIdList.size();i++)
			{
	    		System.out.println( walletsIdList.get(i).toString());
	    	}	*/
	        	
			
			if (this.walletsIdList.size() == 0)
			{
				this.alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.warning), 
																				getResources().getString(R.string.no_id_found), 
																				R.drawable.warning);
				this.alertingDialogOneButton.show(getSupportFragmentManager(), "No id found");
			}
			else if (walletsIdList.size() > 1)
			{
				this.alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.warning), 
																				getResources().getString(R.string.more_than_one_id), 
																				R.drawable.warning);
				this.alertingDialogOneButton.show(getSupportFragmentManager(), "More than one ids found");
			}
			else
			{
				if (this.walletsAdapter.isIncluded(walletsIdList.get(0)))
				{
					this.alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.warning), 
																					getResources().getString(R.string.wallet_existed), 
																					R.drawable.warning);
					this.alertingDialogOneButton.show(getSupportFragmentManager(), "This wallet is existed in DB");
				}
				else
				{
					String[] data = new String[]{walletsIdList.get(0)}; 
					this.walletsIdList.clear();
					new addWallet().execute(data);
				}
				
			}
		}
		
		if (this.addressBitcoin != null)
		{
			if (this.addressBitcoin.size() == 0)
			{
				this.alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.warning), 
																				"No bitcoin address found", 
																				R.drawable.warning);
				this.alertingDialogOneButton.show(getSupportFragmentManager(), "No bitcoin address found");
			}
			else if (this.addressBitcoin.size() > 1)
			{
				this.alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.warning), 
																					getResources().getString(R.string.more_than_one_address), 
																					R.drawable.warning);
				this.alertingDialogOneButton.show(getSupportFragmentManager(), "More than one btc address found");
			}
			else
			{
				AddressBitcoinValidator addressBitcoinValidator = new AddressBitcoinValidator();
				
				if (addressBitcoinValidator.validate(addressBitcoin.get(0)))
				{
					Intent send  = new Intent(this, SendActivity.class);
					send.putExtra("address", addressBitcoin.get(0));
					send.putExtra("wallet_id", wl.getWallet_id());
					
					this.addressBitcoin = null;
					
					startActivity(send);

				}
				else
				{
					this.alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.warning), 
																						getResources().getString(R.string.invalid_address), 
																						R.drawable.warning);
					this.alertingDialogOneButton.show(getSupportFragmentManager(), "Invalid btc address");
				}
			}
		}
				
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
			
			loadingDialog = LoadingDialog.newInstance(getResources().getString(R.string.please_wait), 
														getResources().getString(R.string.add_wallet));			  									
			loadingDialog.show(getSupportFragmentManager(), "loading dialog add");
	    } 

		@Override
		protected Object doInBackground(String... data) 
		{
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
			super.onPostExecute(result);
			
			loadingDialog.dismiss();

			
			if (result.getClass().getSimpleName().equals("Wallet"))
			{
				Wallet a = (Wallet) result;
				walletsAdapter.addItem(a);
				
				alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.successful), 
																				getResources().getString(R.string.wallet_added), 
																				R.drawable.ok);
				alertingDialogOneButton.show(getSupportFragmentManager(), "ok alerting dialog");
			}
			else if (result.getClass().getSimpleName().equals("String"))
			{
				if (result.equals("no connection"))
				{
					alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.fail), 
																					getResources().getString(R.string.no_connection_no_wallet), 
																					R.drawable.error);
					alertingDialogOneButton.show(getSupportFragmentManager(), "error 1 alerting dialog");
				}
				else if (result.equals("slow connection"))
				{
					alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.fail), 
																					getResources().getString(R.string.slow_connection_no_wallet), 
																					R.drawable.error);
					alertingDialogOneButton.show(getSupportFragmentManager(), "error 2 alerting dialog");
				}
				else if (result.equals("fail"))
				{
					alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.fail), 
																					getResources().getString(R.string.unknown_problem), 
																					R.drawable.error);
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
			super.onPostExecute(result);
			
			loadingDialog.dismiss();
					
			if (result.getClass().getSimpleName().equals("Wallet"))
			{
				Wallet a = (Wallet) result;
				walletsAdapter.addItem(a);
				
				alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.successful), 
																				getResources().getString(R.string.wallet_added), 
																				R.drawable.ok);
				alertingDialogOneButton.show(getSupportFragmentManager(), "ok alerting dialog");
			}
			else if (result.getClass().getSimpleName().equals("String"))
			{
				if (result.equals("no connection"))
				{
					alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.fail), 
																					getResources().getString(R.string.no_connection_no_wallet), 
																					R.drawable.error);
					alertingDialogOneButton.show(getSupportFragmentManager(), "error 1 alerting dialog");
				}
				else if (result.equals("slow connection"))
				{
					alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.fail), 
																					getResources().getString(R.string.slow_connection_no_wallet), 
																					R.drawable.error);
					alertingDialogOneButton.show(getSupportFragmentManager(), "error 2 alerting dialog");
				}
				else if (result.equals("fail"))
				{
					alertingDialogOneButton = AlertingDialogOneButton.newInstance(getResources().getString(R.string.fail), 
																					getResources().getString(R.string.unknown_problem), 
																					R.drawable.error);
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
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);
		}
		
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
		{
			super.onCreateOptionsMenu(menu, inflater);
			MenuItem refresh = menu.add(0,0,0,getActivity().getResources().getString(R.string.refresh));
	    	{
	    		refresh.setIcon(R.drawable.ic_refresh);
	    		refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);		
	    	}
	    	
	    	MenuItem about = menu.add(0,1,1,getActivity().getResources().getString(R.string.about));
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
			super.onCreate(savedInstanceState);
			setHasOptionsMenu(true);	
		}
		
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
		{
			super.onCreateOptionsMenu(menu, inflater);
			
			MenuItem refresh = menu.add(0,0,0,getActivity().getResources().getString(R.string.refresh));
	    	{
	    		refresh.setIcon(R.drawable.ic_refresh);
	    		refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);		
	    	}
	    	
	    	MenuItem send = menu.add(0,1,1,getActivity().getResources().getString(R.string.send_coins));
	    	{
	    		send.setIcon(R.drawable.send);
	    		send.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);		
	    	}
	    	
	    	MenuItem delete = menu.add(0,2,2,getActivity().getResources().getString(R.string.remove_wallet));
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
			super.onActivityResult(requestCode, resultCode, intent);
			
			ExtractAddressBitcoin extractAddressBitcoin = new ExtractAddressBitcoin();
			
			if (requestCode == REQUEST_SEND) 
			{
				if (resultCode == RESULT_OK) 
				{
					String address = intent.getStringExtra("SCAN_RESULT");
					//String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

					//System.out.println("Bitcoin Address : " + address);
					
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
	
	
	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) 
	    {
	    	String action = intent.getAction();

	    	System.out.println("ACTION : " + action);

	    	if (action.equals("SENT"))
	    	{  		
	    		//System.out.println("Begin to refresh the wallet !!!!");
	    		
	    		try 
	    		{
					walletsAdapter.updateItem(connection.getWallet(wl.getWallet_id()));
				} 
	    		catch (IOException e) 
	    		{
					e.printStackTrace();
					
					return;
				} 
	    		catch (ConnectionNotInitializedException e) 
				{
					e.printStackTrace();
					
					return;
				}
	    	}
	    	
	    }
	};
	
	@Override
	protected void onDestroy() 
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(intentReceiver);
	}
	
	public boolean notEmpty(String s) 
	{
		return (s != null && s.length() > 0);
	}
	
    
    
}