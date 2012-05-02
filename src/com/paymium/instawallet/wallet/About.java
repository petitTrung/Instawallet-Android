package com.paymium.instawallet.wallet;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.paymium.instawallet.R;

public class About extends SherlockActivity implements OnClickListener
{
	private TextView tv;
	private Button btn;
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        
        this.tv = (TextView) findViewById(R.id.text);
        this.tv.setText(R.string.about);
        
        this.btn = (Button) findViewById(R.id.button1);
        this.btn.setOnClickListener(this);
        
    }
	@Override
	public void onClick(View v) 
	{
		if (v.getId() == R.id.button1)
		{
			this.finish();
		}
		
	}
}
