package com.paymium.instawallet.send;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.paymium.instawallet.R;

public class SendActivity extends SherlockActivity 
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		setTheme(R.style.Theme_Sherlock_ForceOverflow);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        
	}
}
