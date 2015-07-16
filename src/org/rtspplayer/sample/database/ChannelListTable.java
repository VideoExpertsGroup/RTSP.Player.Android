/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.database;

import android.provider.BaseColumns;

public abstract class ChannelListTable implements BaseColumns {
    public static final String TABLE_NAME = "table_name";
    public static final String _ID = "id";


    public static final String CHANNEL_NAME = "name";
    public static final String CHANNEL_URL = "url";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String CHANNEL_CONNECTION_TYPE = "connection_type";
    public static final String CHANNEL_QUALITY = "quality";
    public static final String CHANNEL_TAG = "tag";
    public static final String CHANNEL_ID = "channel_id"; // int
    public static final String CHANNEL_NAME_NATIVE = "name_native";
    public static final String CHANNEL_FLAG_FAVOURITE = "flag_fovourite"; //int
    public static final String CHANNEL_LANG = "lang";
    public static final String CHANNEL_COUNTRY = "country";
    public static final String CHANNEL_WORKING = "working"; //int
    public static final String CHANNEL_URL_ALTERNATIVES = "url_alternatives";
    public static final String CHANNEL_DATARECEIVE_TIMEOUT = "datareceive_timeout"; //int
    public static final String CHANNEL_COUNTRY_CODE = "country_code";
    public static final String CHANNEL_LANGUAGE_CODE = "language_code";
    public static final String CHANNEL_IMAGE_URL = "image_url";
    public static final String CHANNEL_IMAGE_URL_STR = "image_url_str";


    public static final String COLUMN_NAME_NULLABLE = null;
    public static final String COLUMN_NAME_UPDATED = null;

    private static final String TEXT_TYPE = " TEXT";
    private static final String TEXT_NOT_NULL_TYPE = " TEXT NOT NULL";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";


    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ChannelListTable.TABLE_NAME + " (" +
                    ChannelListTable._ID + " INTEGER PRIMARY KEY," +
                    ChannelListTable.CHANNEL_NAME + TEXT_NOT_NULL_TYPE + COMMA_SEP +
                    ChannelListTable.CHANNEL_URL + TEXT_TYPE + COMMA_SEP +
                    ChannelListTable.USER + TEXT_TYPE + COMMA_SEP +
                    ChannelListTable.PASSWORD + TEXT_TYPE + COMMA_SEP +
                    ChannelListTable.CHANNEL_CONNECTION_TYPE + TEXT_TYPE + COMMA_SEP +
                    ChannelListTable.CHANNEL_QUALITY + TEXT_TYPE + COMMA_SEP +
                    ChannelListTable.CHANNEL_TAG + TEXT_TYPE + COMMA_SEP +
                    ChannelListTable.CHANNEL_ID + INTEGER_TYPE + COMMA_SEP +
                    ChannelListTable.CHANNEL_NAME_NATIVE + TEXT_TYPE + COMMA_SEP +
                    ChannelListTable.CHANNEL_FLAG_FAVOURITE + INTEGER_TYPE + COMMA_SEP +
                    ChannelListTable.CHANNEL_LANG + TEXT_TYPE + COMMA_SEP +
                    ChannelListTable.CHANNEL_COUNTRY + TEXT_TYPE + COMMA_SEP +
                    ChannelListTable.CHANNEL_WORKING + INTEGER_TYPE + COMMA_SEP +
                    ChannelListTable.CHANNEL_URL_ALTERNATIVES + TEXT_TYPE + COMMA_SEP +
                    ChannelListTable.CHANNEL_DATARECEIVE_TIMEOUT + INTEGER_TYPE + COMMA_SEP +
                    ChannelListTable.CHANNEL_COUNTRY_CODE + TEXT_TYPE + COMMA_SEP +
                    ChannelListTable.CHANNEL_LANGUAGE_CODE + TEXT_TYPE + COMMA_SEP +
                    ChannelListTable.CHANNEL_IMAGE_URL + INTEGER_TYPE + COMMA_SEP +
                    ChannelListTable.CHANNEL_IMAGE_URL_STR + TEXT_TYPE + 
                    ")";
                    //" UNIQUE(" + CHANNEL_ID + ") ON CONFLICT REPLACE)";
                    //" )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ChannelListTable.TABLE_NAME;
}