/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
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

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

// Should be thread safe
public class HttpClientFactory 
{
    private static DefaultHttpClient client = null;

    public synchronized static DefaultHttpClient getThreadSafeClient() 
    {
		final SharedSettings settings = SharedSettings.getInstance();
		
		int timeoutConnection = 30000;//settings.HttpServerConnectionTimeout;
		int timeoutSocket = 30000;//settings.HttpSocketTimeout;
        if (client == null)
        {
			HttpParams httpParameters = new BasicHttpParams();
			
	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
	        sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	        registry.register(new Scheme("https", sslSocketFactory, 443));
	        
            client = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParameters, registry), httpParameters);
            //client = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParameters, mgr.getSchemeRegistry()), httpParameters);
        }
		
		HttpConnectionParams.setSoTimeout(client.getParams(), timeoutSocket);
		HttpConnectionParams.setConnectionTimeout(client.getParams(), timeoutConnection);
		HttpConnectionParams.setSocketBufferSize(client.getParams(), 512 * 1024);
        return client;
    }
	
	public String getDataAsStringFromServer(final String url) 
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

	public String getDataAsStringFromServerSynchro(final String url) 
	{
		  Log.v(TAG, "getDataAsStringFromServerSynchro");
		  HttpClient httpClient = HttpClientFactory.getThreadSafeClient();
		    
		  try 
		  {
			  String ret = "";
			  HttpGet request = new HttpGet(url);

			  String base64EncodedCredentials = "cnRzcHBsYXllcjpWaWRlb19pbnRfNTEy";//dHZwbGF5ZXI6dDF2MnAzbDRhNXk2ZTc=";//cnRzcHBsYXllcjpWaWRlb19pbnRfNTEy";//Base64.encodeToString(id.getBytes("US-ASCII"), Base64.DEFAULT);
			  request.addHeader("Authorization", "Basic " + base64EncodedCredentials.trim());
			  
			  HttpResponse response = httpClient.execute(request);
			  
			  HttpEntity entity = response.getEntity();
			  InputStream is = entity.getContent();
			  
			  long length = entity.getContentLength();
			  Log.v("downloadData", "length: " + length);
			  
	          DataInputStream in = new DataInputStream(is);
	          BufferedReader br = new BufferedReader( new InputStreamReader(in));
	          String strLine;
	          long totalread = 0;
	          while ((strLine = br.readLine()) != null) {
	              ret += strLine;
				  totalread += strLine.length();
				  Log.v("downloadData", "Progress so far: " + totalread);
	          }
			  
			  return (String)ret;
		  } 
		  catch (Exception e) 
		  {
			  e.printStackTrace();
		  }
		  return "";
	}
	
	class DownloadDataTask extends AsyncTask<String, Integer, String> 
	{
		  protected void onPreExecute() 
		  {
		  }
		  
		  protected String doInBackground(String... urls) 
		  {
		    Log.v(TAG, "doing download of data");
		    return downloadData(urls);
		  }

		  protected void onProgressUpdate(Integer... progress) {
		    Log.v(TAG, "Progress so far: " + progress[0]);
		  }

		  protected void onPostExecute(String result) 
		  {
		  }

		  private String downloadData(String... urls) 
		  {
			  HttpClient httpClient = HttpClientFactory.getThreadSafeClient();
		    
			  try 
			  {
				  String ret = "";
				  //publishProgress(0);
				  HttpGet request = new HttpGet(urls[0]);

				  String base64EncodedCredentials = "cnRzcHBsYXllcjpWaWRlb19pbnRfNTEy";//dHZwbGF5ZXI6dDF2MnAzbDRhNXk2ZTc=";//cnRzcHBsYXllcjpWaWRlb19pbnRfNTEy";//Base64.encodeToString(id.getBytes("US-ASCII"), Base64.DEFAULT);
				  request.addHeader("Authorization", "Basic " + base64EncodedCredentials.trim());
				  
				  HttpResponse response = httpClient.execute(request);
//				  return EntityUtils.toString(response.getEntity());
				  
				  HttpEntity entity = response.getEntity();
				  InputStream is = entity.getContent();
				  
				  long length = entity.getContentLength();
				  Log.v("downloadData", "length: " + length);
				  
                  DataInputStream in = new DataInputStream(is);
                  BufferedReader br = new BufferedReader( new InputStreamReader(in));
                  String strLine;
                  long totalread = 0;
                  while ((strLine = br.readLine()) != null) {
                      ret += strLine;
				      totalread += strLine.length();
					  Log.v("downloadData", "Progress so far: " + totalread);
                  }
				  
				  return (String)ret;
			  } 
			  catch (Exception e) 
			  {
				  e.printStackTrace();
			  }
			  return "";
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
    
	private static final String TAG	= "HttpClientFactory";
}
