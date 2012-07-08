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
package com.beetle.framework.persistence.dao;

import com.beetle.framework.AppProperties;
import com.beetle.framework.log.AppLogger;
import com.beetle.framework.util.ResourceLoader;
import com.beetle.framework.util.file.XMLReader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Title: 框架设计
 * </p>
 * <p>
 * Description: DAO对象工厂
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: 甲壳虫科技
 * </p>
 * 
 * @author 余浩东
 * 
 * @version 1.0
 */
public class DaoFactory {
	// private static DaoFactory f = new DaoFactory();
	private final static Map<String, String> classesTable = new HashMap<String, String>();
	private static final Object intObjLock = new Object();
	private static Map<String, String> paramMap = null;

	private static Map<String, Object> cacheMap = new HashMap<String, Object>(); // 缓存DaoImp对象

	private DaoFactory() {
	}

	static {
		paramMap = loadParameters();
		if (paramMap != null) {
			if (paramMap.isEmpty()) { // 为了兼容原来没有参数的版本
				initCache();
			} else {
				String initialCache = (String) paramMap.get("initialCache");
				if (initialCache != null
						&& initialCache.equalsIgnoreCase("true")) {
					initCache();
				}
			}
		}
	}

	public static void initialize() {

	}

	private static void initCache() {
		Map<String, String> m = getClassesTableInstance();
		Set<String> keys = m.keySet();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String face = (String) it.next();
			try {
				getDaoObject(face);
			} catch (DaoFactoryException de) {
				de.printStackTrace();
			}
		}
		m.clear();
		AppLogger.getInstance(DaoFactory.class).info(
				"all of the system dao's impobjects have bean cached!");
	}

	/**
	 * 根据指定的dao接口名称，从缓存中清除此对象
	 * 
	 * @param InterFaceName
	 *            String
	 */
	public static void removeDaoObjectFromCacheByID(String InterFaceName) {
		if (cacheMap.containsKey(InterFaceName)) {
			cacheMap.remove(InterFaceName);
		}
	}

	/**
	 * 从内存中清除所有的缓存对象
	 */
	public static void removeAllDaoObjectFromCache() {
		cacheMap.clear();
	}

	/**
	 * 直接通过DAO接口实现类来获取一个DAO接口实现对象<br>
	 * 如果此Dao实现类为线程安全类，则缓存此对象
	 * 
	 * @param daoImpClass
	 *            Dao接口实现类
	 * 
	 * @return Object
	 * @throws DaoFactoryException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getDaoObject(Class<T> daoImpClass)
			throws DaoFactoryException {
		Object dao = cacheMap.get(daoImpClass.getName());
		if (dao == null) {
			synchronized (intObjLock) {
				dao = newAndCache(daoImpClass);
			}
		}
		return (T) dao;
	}

	private static Object newAndCache(Class<?> daoImpClass) {
		Object dao = cacheMap.get(daoImpClass.getName());
		if (dao == null) {
			try {
				dao = daoImpClass.newInstance();
			} catch (Exception ex) {
				throw new DaoFactoryException("实例化DAO(" + daoImpClass.getName()
						+ ")对象出错", ex);
			}
			cacheMap.put(daoImpClass.getName(), dao);
		}
		return dao;
	}

	/**
	 * 根据配置文件的Dao接口名称获取一个接口实现对象<br>
	 * 如果此Dao实现类为线程安全类，则缓存此对象
	 * 
	 * @param InterFaceName
	 *            配置文件的Dao接口名称
	 * @return Object
	 * @throws DaoFactoryException
	 */
	public static Object getDaoObject(String interFaceName)
			throws DaoFactoryException {
		Object obj = cacheMap.get(interFaceName);
		if (obj == null) {
			synchronized (intObjLock) {
				obj = getDaoObject_syn(interFaceName);
			}
		}
		return obj;
	}

	private static Object getDaoObject_syn(String interFaceName)
			throws DaoFactoryException {
		Object obj = cacheMap.get(interFaceName);
		if (obj == null) {
			String className = (String) getClassesTableInstance().get(
					interFaceName);
			if (className != null) {
				try {
					obj = Class.forName(className).newInstance();
					// if (ReflectUtil.isThreadSafe(obj.getClass())) {
					cacheMap.put(interFaceName, obj);
					// }
				} catch (Exception e) {
					throw new DaoFactoryException("实例化DAO(" + className
							+ ")对象出错", e);
				}
			} else {
				throw new DaoFactoryException("找不到要实例化DAO(" + interFaceName
						+ ")对象");
			}
		}
		return obj;
	}

	private static Map<String, String> getClassesTableInstance() {
		if (classesTable.isEmpty()) {
			synchronized (classesTable) {
				Map<String, String> map = loadCommonClasses();
				if (!map.isEmpty()) {
					classesTable.putAll(map);
					map.clear();
				}
				/*
				 * Map m = loadClassesTable(); if (m != null && !m.isEmpty()) {
				 * classesTable.putAll(m); m.clear(); }
				 */
			}
		}
		return classesTable;
	}

	private final static Map<String, String> readeConfig(String v1, String v2,
			String v3) {
		Map<String, String> m = null;
		File f = null;
		try {
			String filename = AppProperties.getAppHome() + "DAOConfig.xml";
			f = new File(filename);
			if (f.exists()) {
				m = XMLReader.getProperties(filename, v1, v2, v3);
				AppLogger.getInstance(DaoFactory.class).info(
						"from file:[" + f.getPath() + "]");
			} else {
				m = XMLReader.getProperties(
						ResourceLoader.getResAsStream(filename), v1, v2, v3);
				AppLogger.getInstance(DaoFactory.class).info(
						"from jar:["
								+ ResourceLoader.getClassLoader().toString()
								+ "]");
			}
		} catch (IOException ex) {
			AppLogger.getInstance(DaoFactory.class).warn(ex.getMessage());
		} finally {
			if (f != null) {
				f = null;
			}
		}
		return m;
	}

	private static Map<String, String> loadCommonClasses() {
		return readeConfig("DAO.IMPLEMENT", "interfaceName", "impClass");
		/*
		 * Map map = XMLProperties.getProperties(ResourceReader.getAPP_HOME() +
		 * "DAOConfig.xml", "DAO-IMPLEMENT.COMMON", "InterFaceName",
		 * "ImpClassName"); return map;
		 */
	}

	private static Map<String, String> loadParameters() {
		return readeConfig("DAO.PARAMETERS", "name", "value");
	}
}
