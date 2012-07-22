package com.beetle.framework.resource.define;

import java.util.ArrayList;
import java.util.List;

public class MasterDetailDTO {

	public MasterDetailDTO(Object master, List<?> detail) {
		super();
		this.master = master;
		this.detail = new ArrayList<Object>();
		this.detail.addAll(detail);
	}

	private final Object master;
	private final List<Object> detail;

	public Object getMaster() {
		return master;
	}

	public List<Object> getDetail() {
		return detail;
	}

}
