package com.beetle.component.search.imp;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import com.beetle.framework.AppProperties;
import com.beetle.framework.log.AppLogger;

public class StoreManager {
	private final static String datFileName = AppProperties.getAppHome()
			+ "StoreManager.dat";
	private static final AppLogger logger = AppLogger
			.getInstance(StoreManager.class);
	private static final Object lock = new Object();
	private Map<String, StoreInfo> infoCache;// = new HashMap<String,
												// StoreInfo>();
	private static final StoreManager instance = new StoreManager();

	public static StoreManager getInstance() {
		return instance;
	}

	private StoreManager() {
		super();
		try {
			this.infoCache = readFromDisk();
			logger.info("read info cache from disk,OK");
		} catch (Throwable e) {
			// e.printStackTrace();
			this.infoCache = new HashMap<String, StoreInfo>();
			logger.info("read info cache from disk,raise err,may be no data,new a cache in momory!");
		}
	}

	private static Map<String, StoreInfo> readFromDisk() throws Throwable {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(datFileName);
			ois = new ObjectInputStream(fis);
			@SuppressWarnings("unchecked")
			Map<String, StoreInfo> infoCache = (Map<String, StoreInfo>) ois
					.readObject();
			return infoCache;
		} finally {
			if (ois != null) {
				ois.close();
			}
			if (fis != null) {
				fis.close();
			}
		}
	}

	private static void writeToDisk(Map<String, StoreInfo> cache)
			throws Throwable {
		java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(
				new java.io.FileOutputStream(datFileName));
		try {
			out.writeObject(cache);
		} finally {
			out.close();
		}
	}

	public void saveCacheToDisk() {
		synchronized (lock) {
			try {
				writeToDisk(infoCache);
				logger.info("writeToDisk OK,{}", infoCache);
			} catch (Throwable e) {
				logger.error("writeToDisk err", e);
			}
		}
	}

	public void putIntoInfoCache(String uid, StoreInfo info) {
		synchronized (lock) {
			infoCache.put(uid, info);
			try {
				writeToDisk(infoCache);
				logger.info("writeToDisk OK,{}", infoCache);
			} catch (Throwable e) {
				logger.error("writeToDisk err", e);
			}
		}
	}

	public void removeFromCache(String uid) {
		synchronized (lock) {
			infoCache.remove(uid);
			try {
				writeToDisk(infoCache);
				logger.info("writeToDisk OK,{}", infoCache);
			} catch (Throwable e) {
				logger.error("writeToDisk err", e);
			}
		}
	}

	public StoreInfo getFromInfoCache(String uid) {
		return infoCache.get(uid);
	}
}
