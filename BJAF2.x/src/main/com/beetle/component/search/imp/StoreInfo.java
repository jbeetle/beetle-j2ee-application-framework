package com.beetle.component.search.imp;

import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;

import com.beetle.component.search.def.StoreType;

public class StoreInfo implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String uid;
	private transient Analyzer analyzer;
	private transient Directory dir;
	private String path;
	private StoreType storeType;
	private String indexKeys[];

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public StoreType getStoreType() {
		return storeType;
	}

	public void setStoreType(StoreType storeType) {
		this.storeType = storeType;
	}

	public String[] getIndexKeys() {
		return indexKeys;
	}

	public void setIndexKeys(String[] indexKeys) {
		this.indexKeys = indexKeys;
		StoreManager.getInstance().saveCacheToDisk();
	}

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	public Directory getDir() {
		return dir;
	}

	public void setDir(Directory dir) {
		this.dir = dir;
	}

	@Override
	public String toString() {
		return "StoreInfo [uid=" + uid + ", path=" + path + ", storeType="
				+ storeType + ", indexKeys=" + Arrays.toString(indexKeys) + "]";
	}

}
