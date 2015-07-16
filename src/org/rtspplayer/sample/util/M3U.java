/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;


// Should be thread safe
public class M3U 
{
	public class M3UChannel
	{
		public String title;
		public String url;
		
		public M3UChannel()
		{
			title	= new String();
			url 	= new String();
		} 
	
		public M3UChannel(String t, String u)
		{
			title	= new String(t);
			url 	= new String(u);
		}
		
	}



    private static DefaultHttpClient client = null;
	List<M3UChannel> m3u_channels;

	public M3U()
	{
		m3u_channels = new ArrayList<M3UChannel>();
		Log.v(TAG,"M3UHTTPParser::M3UHTTPParser");
	}
	

    public synchronized static DefaultHttpClient getThreadSafeClient() 
    {
    	
		//final SharedSettings settings = SharedSettings.getInstance();
		int timeoutConnection 	= 30000;//settings.HttpServerConnectionTimeout;
		int timeoutSocket 		= 30000;//settings.HttpSocketTimeout;


		Log.v(TAG,"M3UHTTPParser::getThreadSafeClient client:" + client);
        if (client == null)
        {
			HttpParams httpParameters = new BasicHttpParams();
			
	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        //final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
	        //sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	        //registry.register(new Scheme("https", sslSocketFactory, 443));
	        
            client = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParameters, registry), httpParameters);
            //client = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParameters, mgr.getSchemeRegistry()), httpParameters);
        }
		
		HttpConnectionParams.setSoTimeout(client.getParams(), timeoutSocket);
		HttpConnectionParams.setConnectionTimeout(client.getParams(), timeoutConnection);
		HttpConnectionParams.setSocketBufferSize(client.getParams(), 512 * 1024);
        return client;
    }
	
	public String getDataAndParse(final String url) 
	{
		try 
		{
			//String response = executeAsyncTask(task, url).get();
			String response = new DownloadDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url).get();
			if (response == null || response.isEmpty())
				return "";
			
			return response;
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		} 
		catch (ExecutionException e) 
		{
			e.printStackTrace();
		}
		return "";
	}

	private int M3UParse(final BufferedReader br) 
	{
		String 	strLine;
		long 	totalread 		= 0;
		String 	ret 			= "";
		int		find_caption 	= 0;
		int		i				= 0;

		Log.v(TAG,"M3UHTTPParser: M3UParse");
	
		try {
			strLine = br.readLine();
		//Log.v(TAG,"M3UHTTPParser: readLine " + strLine + "   " + 
		//	strLine.trim().toUpperCase().contains("#EXTM3U") + "   " + 
		//	strLine.trim().toUpperCase().startsWith("#EXTM3U"));

			
		if (strLine.trim().toUpperCase().contains("#EXTM3U") == true)
			{
				find_caption = 1;
			}

		
		String title = "";
		while ((strLine = br.readLine()) != null) {

			//Log.v(TAG,"M3UHTTPParser: readLine " + strLine);

			if (find_caption == 1)
			{
				if (strLine.startsWith("#EXTINF") == true || strLine.startsWith("#EXT") == true)
				{
					String[] info = strLine.split(":|,");
					if (info.length > 2)
						title = info[2];

					strLine = br.readLine();if (strLine == null) break;	
					m3u_channels.add( new M3UChannel( title, strLine) );
				}						
				else if ( strLine.startsWith("# ") == true)
				{
					title = strLine.substring(2);
					strLine = br.readLine();if (strLine == null) break;	
					m3u_channels.add(new M3UChannel(title,strLine));
				}

			}
			else
				{
					title = "Channel" + i++;
					strLine = br.readLine();if (strLine == null) break;	
					m3u_channels.add(new M3UChannel(title,strLine));
				}
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}

	public List<M3UChannel> getChannelList() 
	{
		return m3u_channels;
	}

	public int getDataSynchroAndParse(final String url) 
	{
		  HttpClient httpClient = M3U.getThreadSafeClient();

  		  Log.v(TAG, "M3UHTTPParser::DataSynchroAndParse (" +url + ")");
		  try 
		  {
			  HttpGet request = new HttpGet(url);

			  //String base64EncodedCredentials = "cnRzcHBsYXllcjpWaWRlb19pbnRfNTEy";//dHZwbGF5ZXI6dDF2MnAzbDRhNXk2ZTc=";//cnRzcHBsYXllcjpWaWRlb19pbnRfNTEy";//Base64.encodeToString(id.getBytes("US-ASCII"), Base64.DEFAULT);
			  //request.addHeader("Authorization", "Basic " + base64EncodedCredentials.trim());
			  
			  HttpResponse response = httpClient.execute(request);
			  
			  HttpEntity entity = response.getEntity();
			  InputStream is = entity.getContent();
			  
			  long length = entity.getContentLength();
			  Log.v("M3UHTTPParser: DataSynchroAndParse", "length: " + length);
			  
	          DataInputStream in = new DataInputStream(is);
	          BufferedReader br = new BufferedReader( new InputStreamReader(in));
			  M3UParse(br);
			  
			  return 0;
		  } 
		  catch (Exception e) 
		  {
			  e.printStackTrace();
			  Log.v(TAG,"DataSynchroAndParse error: " + e.getMessage());
			  
		  }
		  return -1;
	}
	
	class DownloadDataTask extends AsyncTask<String, Integer, String> 
	{
		  protected void onPreExecute() 
		  {
		  }
		  
		  protected String doInBackground(String... urls) 
		  {
		    Log.v(TAG, "doing download of data");
		    downloadData(urls);
		    return "";
		  }

		  protected void onProgressUpdate(Integer... progress) {
		    Log.v(TAG, "Progress so far: " + progress[0]);
		  }

		  protected void onPostExecute(String result) 
		  {
		  }

		  private int downloadData(String... urls) 
		  {
		  
		  if (urls[0].startsWith("file://") == true || urls[0].startsWith("/storage") == true) 
		  {
			 BufferedReader br = null;
			 String response = null;
		      try {
        		  //StringBuffer output = new StringBuffer();
			      //String fpath = "/sdcard/"+fname+".txt";
			      Log.v(TAG, "URL: " + urls[0]);
		    	  br = new BufferedReader(new FileReader(new File(urls[0])));
				  M3UParse(br);
			      } catch (IOException e) {
				        e.printStackTrace();
			        	return -1;
      				}
		
		  	 /*
			 Log.v(TAG, "URL: " + urls[0]);
			 if (urls[0].startsWith("file://") == true)	
			 {
				File file = new File (urls[0]);
				InputStream in = null;
				try {
				  in = new BufferedInputStream(new FileInputStream(file));
				  BufferedReader br = new BufferedReader( new InputStreamReader(in));
				  M3UParse(br);
				 } catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 finally {
				  if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				  }
				}
				*/
				return 0;
			  }
		  	  else
		  	  {
				  HttpClient httpClient = M3U.getThreadSafeClient();
				  try 
				  {
					  String ret = "";
					  //publishProgress(0);
					  HttpGet request = new HttpGet(urls[0]);

					  //String base64EncodedCredentials = "cnRzcHBsYXllcjpWaWRlb19pbnRfNTEy";//dHZwbGF5ZXI6dDF2MnAzbDRhNXk2ZTc=";//cnRzcHBsYXllcjpWaWRlb19pbnRfNTEy";//Base64.encodeToString(id.getBytes("US-ASCII"), Base64.DEFAULT);
					  //request.addHeader("Authorization", "Basic " + base64EncodedCredentials.trim());
					  
					  HttpResponse response = httpClient.execute(request);
					  
					  HttpEntity entity = response.getEntity();
					  InputStream is = entity.getContent();
					  
					  long length = entity.getContentLength();
					  Log.v(TAG, "length: " + length);
					  
	                  DataInputStream in = new DataInputStream(is);
	                  BufferedReader br = new BufferedReader( new InputStreamReader(in));
					  
					  M3UParse(br);				  
					  return 0;
				  } 
				  catch (Exception e) 
				  {
					  e.printStackTrace();
				  }
				  return -1;
		  	  }  
	}
	}
	
    static public <T> AsyncTask<T, ?, ?> executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) 
    {
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
    	{
    		return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    	}
    	else 
    	{
    		return task.execute(params);
    	}
    }  
    
	private static final String TAG	= "M3UHTTPParser";
}
