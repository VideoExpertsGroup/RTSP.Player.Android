/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/*
 * balm. Thread class for handy thread usage
 */
public abstract class ThreadHelper implements Runnable{
	
	boolean 		isstarted = false; //start/stop status
	boolean 		isrunning = false; //true when run() is running
	Thread 			thread=null;
	
	Object 			objWait = new Object();
	CountDownLatch  objLatch = null; 
	
	protected ThreadHelper(){
	}
	
	public void sleep(long millis){
		synchronized (objWait){
			try {
				objWait.wait(millis);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean is_started(){
		return (thread != null && isstarted); 
	}
	public boolean is_running(){
		return (thread != null && isrunning);
	}
	
	@Override
	public void run(){
		isrunning = true;
		synchronized (objWait){
			objLatch = new CountDownLatch(1);
		}
		
		runt();
		
		if(objLatch != null)
			objLatch.countDown();
		
		isrunning = false;
	}
	//=> abstract members
	public abstract void runt();
	//<= abstract members
	
	public void wakeup(){
		synchronized (objWait){
			objWait.notifyAll();
		}
	}
	synchronized public void start(){
		if(is_started()){
			return;
		}
		isstarted=true;
		objLatch = null;
		thread = new Thread(this);
		thread.start();
	}
	
	synchronized public void stop(long wait_millis){
		if(!is_started()){
			return;
		}
		
		isstarted = false;
		synchronized (objWait){
			objWait.notifyAll();
		}
		
		if(wait_millis != 0 && objLatch != null){
			try {
				if(wait_millis == -1){
					//infinite
					objLatch.await();
				}else{
					objLatch.await(wait_millis, TimeUnit.MILLISECONDS);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(thread != null) thread.interrupt();
		thread = null;
		objLatch = null;
		isrunning = false;
	}

}
