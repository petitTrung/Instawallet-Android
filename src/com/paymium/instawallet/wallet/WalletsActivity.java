package com.paymium.instawallet.wallet;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.paymium.instawallet.R;
import com.paymium.instawallet.dialog.LoadingDialog;
import com.paymium.instawallet.flip.AnimationFactory;
import com.paymium.instawallet.flip.AnimationFactory.FlipDirection;


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
		AnimationFactory.flipTransition(viewAnimator, FlipDirection.LEFT_RIGHT);
		Toast.makeText(WalletsActivity.this, "Side B Touched", Toast.LENGTH_SHORT).show();
		
	}
    
    
}