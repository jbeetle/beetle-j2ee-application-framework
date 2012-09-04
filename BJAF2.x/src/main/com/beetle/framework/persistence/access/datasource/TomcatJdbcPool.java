package com.beetle.framework.persistence.access.datasource;

import java.sql.Connection;

import com.beetle.framework.persistence.access.ConnectionException;
import com.beetle.framework.persistence.access.IConnPool;

public class TomcatJdbcPool implements IConnPool{

	@Override
	public Connection getConnection() throws ConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void closeAllConnections() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDriverName(String driverName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConURL(String conURL) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUsername(String username) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPassword(String password) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMin(int min) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTestSql(String testSql) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMax(int max) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

}
