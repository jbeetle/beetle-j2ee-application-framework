/*
 * BJAF - Beetle J2EE Application Framework
 * 甲壳虫J2EE企业应用开发框架
 * 版权所有2003-2015 余浩东 (www.beetlesoft.net)
 * 
 * 这是一个免费开源的软件，您必须在
 *<http://www.apache.org/licenses/LICENSE-2.0>
 *协议下合法使用、修改或重新发布。
 *
 * 感谢您使用、推广本框架，若有建议或问题，欢迎您和我联系。
 * 邮件： <yuhaodong@gmail.com/>.
 */
package com.beetle.framework.persistence.access.datasource;

import com.beetle.framework.AppRuntimeException;
import com.beetle.framework.persistence.access.ConnectionException;
import com.beetle.framework.persistence.access.IConnPool;
import com.beetle.framework.util.UUIDGenerator;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;

import javax.transaction.TransactionManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ProxoolPool implements IConnPool {
	private String alias2;
	private Properties info;
	private String poolurl;
	private int max;
	private int min;
	private String driverName;
	private String conURL;
	private String username;
	private String password;
	private String alias;
	private String testSql;

	public ProxoolPool(int max, int min, String driverName, String conURL,
			String username, String password, String alias, String testSql) {
		this.max = max;
		this.min = min;
		this.driverName = driverName;
		this.conURL = conURL;
		this.username = username;
		this.password = password;
		this.alias = alias;
		this.testSql = testSql;
	}

	public ProxoolPool() {
		this(0, 0, "", "", "", "", UUIDGenerator.generateUUID(), "");
	}

	public void closeAllConnections() {
		try {
			ProxoolFacade.killAllConnections(this.alias2, "kill it by hand");
		} catch (ProxoolException e) {
			e.printStackTrace();
		}

	}

	protected void finalize() throws Throwable {
		this.closeAllConnections();
		ProxoolFacade.removeConnectionPool(this.alias2);
		super.finalize();
	}

	public Connection getConnection() throws ConnectionException {
		try {
			return DriverManager.getConnection(this.alias2);
		} catch (SQLException e) {
			throw new ConnectionException(e);
		}
	}

	public void setTransactionManager(TransactionManager tm) {
		throw new UnsupportedOperationException();
	}

	public void setConURL(String conURL) {
		this.conURL = conURL;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void shutdown() {
		this.closeAllConnections();
		try {
			ProxoolFacade.removeConnectionPool(this.alias2);
		} catch (ProxoolException e) {
			e.printStackTrace();
		}

	}

	public void start() {
		this.alias2 = "proxool." + alias;
		info = new Properties();
		info.setProperty("proxool.maximum-connection-count",
				String.valueOf(max));
		info.setProperty("proxool.minimum-connection-count",
				String.valueOf(min));
		if (testSql != null && testSql.length() > 1) {
			info.setProperty("proxool.house-keeping-test-sql", testSql);
		}
		info.setProperty("user", username);
		info.setProperty("password", password);
		poolurl = "proxool." + alias + ":" + driverName + ":" + conURL;
		try {
			Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
			ProxoolFacade.registerConnectionPool(poolurl, info);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppRuntimeException(e);
		}
	}

	@Override
	public void setTestSql(String testSql) {
		this.testSql = testSql;
	}

}
