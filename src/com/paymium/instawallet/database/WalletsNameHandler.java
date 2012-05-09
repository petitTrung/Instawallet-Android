package com.paymium.instawallet.database;

import java.util.LinkedList;

import com.paymium.instawallet.wallet.Wallet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class WalletsNameHandler 
{
	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 2;

	// Database Name
	private static final String DATABASE_NAME = "WalletsNameManager";

	// Table name
	private static final String TABLE_WALLETS_NAME = "WalletsName";
	
	// Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "name";
	
	private static final String TAG = "DBAdapter";
	
	private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_WALLETS_NAME + "(" + KEY_ID + " TEXT PRIMARY KEY,"								 
																							 + KEY_NAME + " TEXT"
																					   + ")";	
	private DatabaseHelper DBHelper;
    private SQLiteDatabase db;
    
    public WalletsNameHandler(Context context) 
	{
		this.DBHelper = new DatabaseHelper(context);
	}
    
    private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
		
		// Creating Tables
		@Override
        public void onCreate(SQLiteDatabase db)
        {
            try 
            {
                db.execSQL(DATABASE_CREATE);
            } 
            catch (SQLException e) 
            {
                e.printStackTrace();
            }
        }
		
		// Upgrading database
		@Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
            			+ newVersion + ", which will destroy all old data");
            
        	truncate(db);
        }
		
		// Truncate table's content
		public void truncate(SQLiteDatabase db)
		{ 
			// Drop older table if existed
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_WALLETS_NAME);

			// Create tables again
			onCreate(db);

		}
	}
    
	//---opens the database---
    public WalletsNameHandler open() throws SQLException 
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---
    public void close() 
    {
        DBHelper.close();
    }
   
    
    //---insert a wallet name into the database---
    public void addWalletName(Wallet wallet) 
    {
    	if (!this.verifyBeforeAdding(wallet))
    	{
    		//System.out.println("PREPARING FOR ADDING A WALLET!!");
    		this.open();
    		
    		ContentValues value = new ContentValues();

    		value.put(KEY_ID, wallet.getWallet_id());
    		
    		value.put(KEY_NAME, "");
    		
    		db.insert(TABLE_WALLETS_NAME, null, value);
    		
    		this.close();
    		
    		//System.out.println("ADDING A WALLET IS DONE !!");
    	}
		
		
    }
    
    //---insert a wallet name into the database---
    public void addWalletName(Wallet wallet, String wallet_name) 
    {
    	if (!this.verifyBeforeAdding(wallet))
    	{
    		//System.out.println("PREPARING FOR ADDING A WALLET!!");
    		this.open();
    		
    		ContentValues value = new ContentValues();

    		value.put(KEY_ID, wallet.getWallet_id());
    		value.put(KEY_NAME, wallet_name);
    		
    		db.insert(TABLE_WALLETS_NAME, null, value);
    		
    		this.close();
    		
    		//System.out.println("ADDING A WALLET IS DONE !!");
    	}
		
		
    }
    
	// Getting a single wallet name
	public String getWalletName(String id) 
	{
		this.open();
	 
	    Cursor cursor = db.query(TABLE_WALLETS_NAME, new String[] { KEY_ID, KEY_NAME}, KEY_ID + "=?",
	            								new String[] { id }, null, null, null, null);
	    if (cursor != null)
	        cursor.moveToFirst();
	    
	    String wallet_name = cursor.getString(1);
	    
	    System.out.println("donnee : " + wallet_name);
	    
	    cursor.close();
	    
	    
	    this.close();
	    
	    // return wallet name
	    return wallet_name;
	}
	
	// Getting all wallets names
	public LinkedList<String> getAllWalletsNames() 
	{
		LinkedList<String> walletsNamesList = new LinkedList<String>();
		
		// Select All Query
		String selectQuery = "SELECT * FROM " + TABLE_WALLETS_NAME;

		this.open();
		
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) 
		{
			do 
			{
				String wallet_name = cursor.getString(1);
				
				walletsNamesList.add(wallet_name);		
			} 
			while (cursor.moveToNext());
		}
		
		cursor.close();
		
		this.close();
		
		// return wallets name list
		
		return walletsNamesList;
	}
	
	public LinkedList<String> getAllWalletsIDs()
	{
		LinkedList<String> walletsIDsList = new LinkedList<String>();
		
		// Select All Query
		String selectQuery = "SELECT * FROM " + TABLE_WALLETS_NAME;

		this.open();
		
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) 
		{
			do 
			{
				String wallet_id = cursor.getString(0);
				
				walletsIDsList.add(wallet_id);		
			} 
			while (cursor.moveToNext());
		}
		
		cursor.close();
		
		this.close();
		
		// return wallets name list
		
		return walletsIDsList;
	}
	
    // Updating a wallet name
	public void updateWallet(Wallet wallet, String wallet_name) 
	{ 
		this.open();
			
	    ContentValues value = new ContentValues();
	    
	    value.put(KEY_ID, wallet.getWallet_id());
		value.put(KEY_NAME, wallet_name);
	 
	    // updating row
	    db.update(TABLE_WALLETS_NAME, value, KEY_ID + " = ?", new String[] { wallet.getWallet_id() });
	    
	    this.close();
	}
	
    // Deleting a wallet name
	public void deleteWallet(Wallet wallet) 
	{
		this.open();
		
	    db.delete(TABLE_WALLETS_NAME, KEY_ID + " = ?", new String[] { wallet.getWallet_id() });
	    
	    this.close();
	}
    
	public boolean verifyBeforeAdding(Wallet wallet)
	{
		LinkedList<String> walletsIDList = this.getAllWalletsIDs();
		
		for (int i = 0 ; i < walletsIDList.size() ; i++)
		{
			if (walletsIDList.get(i).equals(wallet.getWallet_id()))
			{
				return true;
			}
		}
		return false;
	}
	
	
}
