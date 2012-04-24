package com.paymium.instawallet;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.paymium.instawallet.dialog.LoadingDialog;

import android.os.Bundle;
import android.widget.Toast;


public class WalletsActivity extends SherlockFragmentActivity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        setTheme(R.style.Theme_Sherlock);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallets);

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
    
    
}