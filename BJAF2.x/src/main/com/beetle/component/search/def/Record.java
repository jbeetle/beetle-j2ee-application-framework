package com.beetle.component.search.def;

import java.util.HashMap;
import java.util.Map;

public class Record implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;
	private final Map<String, String> toIndexAttributes;
	public static final String RECORD_KEY = "RECORD_KEY";
	private long docID;

	public long getDocID() {
		return docID;
	}

	public void setDocID(long docID) {
		this.docID = docID;
	}

	public Record(String id) {
		super();
		this.id = id;
		this.toIndexAttributes = new HashMap<String, String>();
	}

	public String getId() {
		return id;
	}

	public Map<String, String> getToIndexAttributes() {
		return toIndexAttributes;
	}

	public void addForIndex(String name, String info) {
		toIndexAttributes.put(name, info);
	}

	public void clear() {
		toIndexAttributes.clear();
	}

	@Override
	public String toString() {
		return "Record [id=" + id + ", toIndexAttributes=" + toIndexAttributes
				+ ", docID=" + docID + "]";
	}

}
