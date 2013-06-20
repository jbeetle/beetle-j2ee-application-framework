package com.beetle.component.search.def;

public enum StoreType {
	MEMORY_ENGLISH(0), MEMORY_CHINESE(1), FILE_ENGLISH(2), FILE_CHINESE(3);
	private int id;

	private StoreType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

}
