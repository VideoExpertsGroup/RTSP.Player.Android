/*
 *
 * Copyright (c) 2010-2014 EVE GROUP PTE. LTD.
 *
 */


package org.rtspplayer.sample.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class StorageDrives {

	final public static String TAG = "StorageDrives";
	
	Context context=null;
	
	public StorageDrives(final Context c){
		context = c;
	}
	

	//=>storage info
	public static class StorageInfo {
        public final String path;
        public final boolean readonly;
        public final boolean removable;     
        public final int number;
        
        public long lSize=0; //total in MB
        public long lBusy=0; //busy  in MB
        public long lFree=0; //free  in MB
        //public int  lClips=0; //clips on this sdcard

        StorageInfo(String path, boolean readonly, boolean removable, int number) {
            this.path = path;
            this.readonly = readonly;
            this.removable = removable;         
            this.number = number;
            Log.d(TAG, "StorageInfo path="+path+" number="+number+" "+(readonly?"ro":"rw")+(removable?"removable":""));
        }

        public String getDisplayName() {
            StringBuilder res = new StringBuilder();
            if (!removable) {
                res.append("Internal SD card");
            } else if (number > 1) {
                res.append("SD card " + number);
            } else {
                res.append("SD card");
            }
            if (readonly) {
                res.append(" (Read only)");
            }
            return res.toString();
        }
    }
	
	List<StorageInfo> list_storInfo = new ArrayList<StorageInfo>();
	boolean is_stor_needrescan = false;
	//<=
	
	//=>utilites for getting local storage size
	public long get_storTotalMemory(String path){
		StatFs statFs = null;
		try{
			statFs = new StatFs(path);
		}catch(IllegalArgumentException e){
			e.printStackTrace();
			is_stor_needrescan = true;
			return 0;
		}
		
        long Total = ((long)statFs.getBlockCount() * (long)statFs.getBlockSize()) / 1048576;
        return Total;
	}
	
	public long get_storFreeMemory(String path){
		StatFs statFs = null;
		try{
			statFs = new StatFs(path);
		}catch(IllegalArgumentException e){
			e.printStackTrace();
			is_stor_needrescan = true;
			return 0;
		}
		
        long Total = ((long)statFs.getAvailableBlocks() * (long)statFs.getBlockSize()) / 1048576;
        return Total;
	}
	
	public long get_storBusyMemory(String path){
		return get_storTotalMemory(path)-get_storFreeMemory(path);
	}
	
	List<StorageInfo> _get_storList() {

        List<StorageInfo> list = new ArrayList<StorageInfo>();
        String def_path = Environment.getExternalStorageDirectory().getPath();
        boolean def_path_removable = Environment.isExternalStorageRemovable();
        String def_path_state = Environment.getExternalStorageState();
        boolean def_path_available = def_path_state.equals(Environment.MEDIA_MOUNTED)
                                    || def_path_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        boolean def_path_readonly = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);

        HashSet<String> paths = new HashSet<String>();
        int cur_removable_number = 1;

        if (def_path_available) {
            paths.add(def_path);
            list.add(0, new StorageInfo(def_path, def_path_readonly, def_path_removable, def_path_removable ? cur_removable_number++ : -1));
        }

        BufferedReader buf_reader = null;
        try {
            buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
            Log.d(TAG, "/proc/mounts");
            while ((line = buf_reader.readLine()) != null) {
                Log.d(TAG, line);
                if (line.contains("vfat") || line.contains("/mnt") || line.contains("/sdcard")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String unused = tokens.nextToken(); //device
                    String mount_point = tokens.nextToken(); //mount point
                    if (paths.contains(mount_point)) {
                        continue;
                    }
                    unused = tokens.nextToken(); //file system
                    List<String> flags = Arrays.asList(tokens.nextToken().split(",")); //flags
                    boolean readonly = flags.contains("ro");

                    if (line.contains("/dev/block/vold")) {
                        if (!line.contains("/mnt/secure")
                            && !line.contains("/mnt/asec")
                            && !line.contains("/mnt/obb")
                            && !line.contains("/dev/mapper")
                            && !line.contains("tmpfs")) {
                            paths.add(mount_point);
                            list.add(new StorageInfo(mount_point, readonly, true, cur_removable_number++));
                        }
                    }
                }
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (buf_reader != null) {
                try {
                    buf_reader.close();
                } catch (IOException ex) {}
            }
        }
        
        File[] sdCardsKitKat = null;
		if(Build.VERSION.SDK_INT >= 19){
			sdCardsKitKat = get_kitkat_sdcards();
			if(sdCardsKitKat != null && sdCardsKitKat.length > 0 && sdCardsKitKat.length == list.size()){
				int i = 0;
				List<StorageInfo> list2 = new ArrayList<StorageInfo>();
				for(StorageInfo si : list){
					if (sdCardsKitKat[i] == null)
					{
						i++;
						continue;
					}
					
					String path2 = sdCardsKitKat[i].getAbsolutePath();
					String path = path2.replace("/Android/data/" + context.getPackageName()+ "/cache", "");
					if(path == null)
						path = path2;
					list2.add(new StorageInfo(path, si.readonly, si.removable, si.number));
					i++;
				}
				list = list2;
			}
		}

        return list;
    }
	
	@SuppressLint("NewApi")
	private File[] get_kitkat_sdcards(){
		return context.getApplicationContext().getExternalCacheDirs();
	}

	
	public  List<StorageInfo> get_storInfo(){
		return list_storInfo; 
	}

	
	//usage: 
	//File file = new File(Environment.getExternalStorageDirectory().getPath()+"/folder");
	//long folder_size=getFolderSize(file);
	public static long get_storFolderSize(File dir){
		long size = 0;
		if(dir == null)
			return 0;
		File[] ff = dir.listFiles();
		if(ff == null)
			return 0;
		
		for (File file : ff) {
			if(file == null)
				continue;
			
		    if (file.isFile()) {
		        // System.out.println(file.getName() + " " + file.length());
		        size += file.length();
		    } else
		        size += get_storFolderSize(file);
		}
		return size;
	}
	
	public boolean update_stor_space(){
		
		boolean is_changed = false;
		List<StorageInfo> list = _get_storList(); 
		
		if(list_storInfo == null || list_storInfo.size() != list.size() || is_stor_needrescan){
			list_storInfo = list;
			is_changed = true;
			is_stor_needrescan = false;
		}

		Log.i(TAG, "=>update_stor_space cnt="+list.size());
		int i=0;
		for(StorageInfo si : list){
			si.lSize = get_storTotalMemory(si.path);
			si.lBusy = get_storBusyMemory(si.path);
			si.lFree = get_storFreeMemory(si.path);
			
			/*if(i==0){
				si.lClips = list_docs.size();
			}
			Log.i(TAG, "=check_stor_space clips="+si.lClips+"  "+si.path+" "+si.getDisplayName()+" MB T["+si.lSize+"]; B["+si.lBusy+"]; F["+si.lFree+"]");
			*/
			if(!is_changed){
				StorageInfo si_prev = list_storInfo.get(i);
				//Log.i(TAG, "=check_stor_space prev clips="+si_prev.lClips);
				if(si_prev != null){
					if(si_prev.lSize != si.lSize ||
						si_prev.lBusy != si.lBusy ||
						si_prev.lFree != si.lFree 
						//si_prev.lClips != si.lClips 
							){
						is_changed = true;
						list_storInfo = list;
					}
				}
			}
			i++;
		}
		//Log.i(TAG, "=update_stor_space is_changed="+is_changed+" lClips="+list_docs.size());

		/*
		//walk down through docs
		long sizeLRV = 0;
		long sizeHRV = 0;
		for( HTTPDoc hd : list_docs ){
			File fd = new File(hd.getPath());
			sizeLRV += get_storFolderSize(fd);
			
			File fdHRV = new File(hd.getHRVFileName());
			if(fdHRV.exists())
				sizeHRV += fdHRV.length();
		}
		//sizeLRV /= 1048576;
		//sizeHRV /= 1048576;
		Log.i("=check_stor_space LRV["+sizeLRV+"] HRV["+sizeHRV+"] T["+(sizeLRV+sizeHRV)+"]");
		*/
		//notify
		/*if(is_changed && mListeners != null && mListeners.size()>0){
			
			time_check_storsize = SystemClock.elapsedRealtime();

			synchronized (mListeners) {
				for( CallbackListener cb: mListeners){
					cb.onMessage(this, MainActivity.MSG_APP_SERVICE_STORAGE, "");
				}
			}
		}*/
		Log.i(TAG, "<=update_stor_space is_changed="+is_changed);

		return is_changed;
	}
	//<=

}
