package com.paymium.instawallet.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;


public class AlertingDialog extends SherlockDialogFragment
{
	public static AlertingDialog newInstance(String title, String message, int icon)
	{
		AlertingDialog frag = new AlertingDialog();
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
				Toast.makeText(getActivity(), "click on OK", Toast.LENGTH_LONG);
			}
		});
		
		aldg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				Toast.makeText(getActivity(), "click on Cancel", Toast.LENGTH_LONG);
			}
		});	
		
		return aldg.create();
	}
}
