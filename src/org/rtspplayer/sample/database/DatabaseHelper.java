/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.database;

import org.rtspplayer.sample.data.GridData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "DataBase.db";
    public static final String TAG = "DatabaseHelper";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ChannelListTable.SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(ChannelListTable.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void addChannel(String channelName, String channelURL, String user, String password,
                           String channelConnectionType, String channelQuality, String channelTag,
                           int channelId, String channelNameNative, int channelFlagFavourite,
                           String channelLang, String channelCountry, int channelWorking,
                           String channelURLAlternatives, int channelDatareceiveTimeoutput,
                           String channelCountryCode, String channelLanguageCode, int channelImageURL, String channelImageURL_S) {

        SQLiteDatabase database = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ChannelListTable.CHANNEL_NAME, channelName);
        values.put(ChannelListTable.CHANNEL_URL, channelURL);
        values.put(ChannelListTable.USER, user);
        values.put(ChannelListTable.PASSWORD, password);
        values.put(ChannelListTable.CHANNEL_CONNECTION_TYPE, channelConnectionType);
        values.put(ChannelListTable.CHANNEL_QUALITY, channelQuality);
        values.put(ChannelListTable.CHANNEL_TAG, channelTag);
        values.put(ChannelListTable.CHANNEL_ID, channelId);
        values.put(ChannelListTable.CHANNEL_NAME_NATIVE, channelNameNative);
        values.put(ChannelListTable.CHANNEL_FLAG_FAVOURITE, channelFlagFavourite);
        values.put(ChannelListTable.CHANNEL_LANG, channelLang);
        values.put(ChannelListTable.CHANNEL_COUNTRY, channelCountry);
        values.put(ChannelListTable.CHANNEL_WORKING, channelWorking);
        values.put(ChannelListTable.CHANNEL_URL_ALTERNATIVES, channelURLAlternatives);
        values.put(ChannelListTable.CHANNEL_DATARECEIVE_TIMEOUT, channelDatareceiveTimeoutput);
        values.put(ChannelListTable.CHANNEL_COUNTRY_CODE, channelCountryCode);
        values.put(ChannelListTable.CHANNEL_LANGUAGE_CODE, channelLanguageCode);
        values.put(ChannelListTable.CHANNEL_IMAGE_URL, channelImageURL);
        values.put(ChannelListTable.CHANNEL_IMAGE_URL_STR, channelImageURL_S);

        long newRowId = database.insert(
                ChannelListTable.TABLE_NAME,
                ChannelListTable.COLUMN_NAME_NULLABLE,
                values);
    }

    public long addChannel(String channelName, String channelURL, String channelTag, String user, String password,
                           int channelId, int channelImageURL, String channelImageURL_S) {

        SQLiteDatabase database = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ChannelListTable.CHANNEL_NAME, channelName);
        values.put(ChannelListTable.CHANNEL_URL, channelURL);
        values.put(ChannelListTable.USER, user);
        values.put(ChannelListTable.PASSWORD, password);
        values.put(ChannelListTable.CHANNEL_TAG, channelTag);
        values.put(ChannelListTable.CHANNEL_ID, channelId);
        values.put(ChannelListTable.CHANNEL_IMAGE_URL, channelImageURL_S);
        
        if (!channelImageURL_S.isEmpty())
        {
        	values.put(ChannelListTable.CHANNEL_IMAGE_URL, channelImageURL);
	        values.put(ChannelListTable.CHANNEL_IMAGE_URL_STR, channelImageURL_S);
        }

        long newRowId = 0;
        String whereClause = ChannelListTable.CHANNEL_NAME + " = ? AND " + 
        						ChannelListTable.CHANNEL_URL + " = ? ";
        String[] whereArgs = new String[] { channelName, channelURL };
        if (database.update(ChannelListTable.TABLE_NAME, values, whereClause, whereArgs) == 0)
        {
            newRowId = database.insert(
                    ChannelListTable.TABLE_NAME,
                    ChannelListTable.COLUMN_NAME_NULLABLE,
                    values);
            return newRowId;
        }
        
        return -1;
        
    }

    public Cursor readChannelForPresentation(final String tag) {
        SQLiteDatabase database = getReadableDatabase();
        final String[] projection = {
                ChannelListTable._ID,
                ChannelListTable.CHANNEL_NAME,
                ChannelListTable.CHANNEL_URL,
                ChannelListTable.USER,
                ChannelListTable.PASSWORD,
                ChannelListTable.CHANNEL_ID,
                ChannelListTable.CHANNEL_IMAGE_URL,
                ChannelListTable.COLUMN_NAME_UPDATED,
                ChannelListTable.CHANNEL_IMAGE_URL_STR
        };
        final String sortOrder =
                ChannelListTable.COLUMN_NAME_UPDATED + " DESC";

        String whereClause = ChannelListTable.CHANNEL_TAG + " = ? ";
        String[] whereArgs = new String[] { tag };
        
        Cursor mCursorRead = database.query(
                ChannelListTable.TABLE_NAME,
                projection,
                (!tag.isEmpty() ? whereClause : null),
                (!tag.isEmpty() ? whereArgs : null),
                null,
                null,
                sortOrder
        );
        return mCursorRead;
    }

    public long getCount() 
    {
		try 
		{
	        SQLiteDatabase db = getReadableDatabase();
			long count = DatabaseUtils.queryNumEntries(db, ChannelListTable.TABLE_NAME);
	        return count;
		}
		catch (SQLiteCantOpenDatabaseException e)
		{
		}
		catch (SQLiteException e)
		{
		}
        return 0;
    }
    
    public void deleteChannel(long rowId) {
        SQLiteDatabase database = getReadableDatabase();
        String selection = ChannelListTable._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(rowId)};
        database.delete(ChannelListTable.TABLE_NAME, selection, selectionArgs);
    }
    
    public void updateChannel(GridData gd)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        
        ContentValues values = new ContentValues();
        values.put(ChannelListTable.CHANNEL_NAME, gd.name);
        values.put(ChannelListTable.CHANNEL_URL, gd.url);
        values.put(ChannelListTable.USER, gd.user);
        values.put(ChannelListTable.PASSWORD, gd.password);
        values.put(ChannelListTable.CHANNEL_ID, 1);
        values.put(ChannelListTable.CHANNEL_IMAGE_URL, gd.image);
        
        if (gd.image_file != null && !gd.image_file.isEmpty())
        	values.put(ChannelListTable.CHANNEL_IMAGE_URL_STR, gd.image_file);

        // Inserting Row
        if (db.update(ChannelListTable.TABLE_NAME, values, 
        					ChannelListTable._ID + " = " + gd.id, null) == 0)
        {
	    	//Log.i(TAG, "=update==0, goto insert");
	    	db.insert(ChannelListTable.TABLE_NAME, null, values);
    	}
           
        db.setTransactionSuccessful();
        db.endTransaction();
        //db.close(); 
    	
    }
    
    public void updateChannel2(GridData gd)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        
        ContentValues values = new ContentValues();
        values.put(ChannelListTable.CHANNEL_NAME, gd.name);
        values.put(ChannelListTable.CHANNEL_URL, gd.url);
        values.put(ChannelListTable.USER, gd.user);
        values.put(ChannelListTable.PASSWORD, gd.password);
        values.put(ChannelListTable.CHANNEL_ID, 1);
        values.put(ChannelListTable.CHANNEL_IMAGE_URL, gd.image);
        
        if (gd.image_file != null && !gd.image_file.isEmpty())
        {
        	values.put(ChannelListTable.CHANNEL_IMAGE_URL_STR, gd.image_file);
        }
        else
        {
        	values.put(ChannelListTable.CHANNEL_IMAGE_URL_STR, "");
        }

        // Inserting Row
        if (db.update(ChannelListTable.TABLE_NAME, values, 
        					ChannelListTable._ID + " = " + gd.id, null) == 0)
        {
	    	//Log.i(TAG, "=update==0, goto insert");
	    	db.insert(ChannelListTable.TABLE_NAME, null, values);
    	}
           
        db.setTransactionSuccessful();
        db.endTransaction();
        //db.close(); 
    	
    }
    
}