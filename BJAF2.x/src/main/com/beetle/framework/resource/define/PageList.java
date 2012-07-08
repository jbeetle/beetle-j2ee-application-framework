package com.beetle.framework.resource.define;

import java.util.ArrayList;

public class PageList<T> extends ArrayList<T> {

	public PageList() {
		super();
	}

	public PageList(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8215605110760184261L;

	private int recordAmount;

	private int curPageSize;

	private int curPageNumber;

	private int nextPageNumber;

	private int prePageNumber;

	private int pageAmount;

	private int curPos;

	public int getCurPageNumber() {
		return curPageNumber;
	}

	public void setCurPageNumber(int curPageNumber) {
		this.curPageNumber = curPageNumber;
	}

	public int getCurPos() {
		return curPos;
	}

	public void setCurPos(int curPos) {
		this.curPos = curPos;
	}

	public int getNextPageNumber() {
		return nextPageNumber;
	}

	public void setNextPageNumber(int nextPageNumber) {
		this.nextPageNumber = nextPageNumber;
	}

	public int getPageAmount() {
		return pageAmount;
	}

	public void setPageAmount(int pageAmount) {
		this.pageAmount = pageAmount;
	}

	public int getPrePageNumber() {
		return prePageNumber;
	}

	public void setPrePageNumber(int prePageNumber) {
		this.prePageNumber = prePageNumber;
	}

	public int getCurPageSize() {
		return curPageSize;
	}

	public void setCurPageSize(int curPageSize) {
		this.curPageSize = curPageSize;
	}

	public int getRecordAmount() {
		return recordAmount;
	}

	public void setRecordAmount(int recordAmount) {
		this.recordAmount = recordAmount;
	}
}
