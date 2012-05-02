package com.paymium.instawallet.send;

import java.io.IOException;
import java.math.BigDecimal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.paymium.instawallet.R;
import com.paymium.instawallet.dialog.AlertingDialogOneButton;
import com.paymium.instawallet.dialog.LoadingDialog;
import com.paymium.instawallet.exception.ConnectionNotInitializedException;
import com.paymium.instawallet.json.Payment;
import com.paymium.instawallet.wallet.Connection;

public class SendActivity extends SherlockFragmentActivity implements OnClickListener 
{
	
	private EditText address;
	private EditText amount;
	
	private Button validate;
	private Button discard;
	
	private Bundle extras;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		
		setTheme(R.style.Theme_Sherlock);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        this.extras = getIntent().getExtras();
        
        this.address = (EditText) findViewById(R.id.editText1);
        this.address.setText(this.extras.getString("address"));
        this.address.setEnabled(false);
		this.address.setClickable(false);
		this.address.setFocusable(false);
        
        this.amount = (EditText) findViewById(R.id.editText2);
        this.amount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        
        this.validate = (Button) findViewById(R.id.button1);
        this.validate.setOnClickListener(this);
        this.discard = (Button) findViewById(R.id.button2);
        this.discard.setOnClickListener(this);
        
	}

	@Override
	public void onClick(View view) 
	{
		if (view.getId() == R.id.button1)
		{
			if (isNumeric(this.amount.getText().toString()))
			{
				new Send().execute();
			}
			else
			{
				alertingDialogOneButton = AlertingDialogOneButton.newInstance("Fail !!", "Check your amount", R.drawable.error);
				alertingDialogOneButton.show(getSupportFragmentManager(), "error amount");
			}
			
		}
		else if (view.getId() == R.id.button2)
		{
			this.finish();
		}
		
	}
	
	
	private AlertingDialogOneButton alertingDialogOneButton;
	private AlertingDialogFinish alertingDialogFinish;
	private LoadingDialog loadingDialog;
	
	
	public class Send extends AsyncTask<String, Integer, String>
	{
		private Payment payment;
		
		public Send() 
		{
			this.payment = new Payment();
		}
		@Override
		protected void onPreExecute() 
		{
			super.onPreExecute();
			
			loadingDialog = LoadingDialog.newInstance("Please wait", "Loading ...");			  									
			loadingDialog.show(getSupportFragmentManager(), "loading dialog send");
		}

		@Override
		protected String doInBackground(String... params) 
		{
			try 
			{
				payment = Connection.getInstance().postPayment(extras.getString("wallet_id"), extras.getString("address"), new BigDecimal(amount.getText().toString()));
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
				switch (Integer.valueOf(payment.getMessage_code())) 
				{
					case 0:
						
						Intent broadcastIntent = new Intent();
			            broadcastIntent.setAction("SENT");
			            sendBroadcast(broadcastIntent);
			            
			            
						alertingDialogFinish = AlertingDialogFinish.newInstance("Send !!", payment.getMessage(), R.drawable.ok);
						alertingDialogFinish.show(getSupportFragmentManager(), "Ok dialog");
						
						break;		
	
					default:
						
						alertingDialogOneButton = AlertingDialogOneButton.newInstance("Error !!", payment.getMessage(), R.drawable.error);
						alertingDialogOneButton.show(getSupportFragmentManager(), "Error dialog");
						
						break;
				}
				
			}
		}
		
	}
	
	public static boolean isNumeric(String str)  
	{ 
		
		try  
		{  
			Double.parseDouble(str);  
		}  
		catch(NumberFormatException nfe)  
		{  
			return false;  
		}  
		return true;  
	}
	
	public static class AlertingDialogFinish extends SherlockDialogFragment
	{
		public static AlertingDialogFinish newInstance(String title, String message, int icon)
		{
			AlertingDialogFinish frag = new AlertingDialogFinish();
			Bundle args = new Bundle();
			args.putString("title", title);
			args.putString("message", message);
			args.putInt("icon", icon);
			frag.setArguments(args);
			
			return frag;
		}
		
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			String title = getArguments().getString("title");
			String message = getArguments().getString("message");
			int icon = getArguments().getInt("icon");
			
			AlertDialog.Builder aldg = new AlertDialog.Builder(getActivity());
			
			aldg.setIcon(getActivity().getResources().getDrawable(icon));
			aldg.setTitle(title);
			aldg.setMessage(message);
			
			aldg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					getActivity().finish();
				}
			});
			
			
			return aldg.create();
		}
	}
}
