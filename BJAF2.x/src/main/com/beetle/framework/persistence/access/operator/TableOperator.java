package com.beetle.framework.persistence.access.operator;

import com.beetle.framework.AppRuntimeException;
import com.beetle.framework.log.AppLogger;
import com.beetle.framework.persistence.access.ConnectionException;
import com.beetle.framework.persistence.access.ConnectionFactory;
import com.beetle.framework.persistence.access.DBHelper;
import com.beetle.framework.util.ObjectUtil;
import com.beetle.framework.util.cache.ICache;
import com.beetle.framework.util.cache.StrongCache;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * <p>
 * Title: Beetle Persistence Framework
 * </p>
 * <p/>
 * <p>
 * Description:
 * </p>
 * <p/>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p/>
 * <p>
 * Company:
 * </p>
 * 
 * @version 1.0
 */
final public class TableOperator<T> {
	private final static ICache SQL_CACHER = new StrongCache(134);

	private static final String SYSDATASOURCE_DEFAULT = "SYSDATASOURCE_DEFAULT";

	private String dsName = SYSDATASOURCE_DEFAULT;

	private final String tbName;

	private Set<String> filedSet;

	private Class<T> voClass;

	private boolean autoGenerateKey = false; // 是否拥有自动增量的key

	private String autoKeyFiledName;

	private final String primaryKeyName;

	private final static AppLogger logger = AppLogger
			.getInstance(TableOperator.class);

	/**
	 * TableOperator（默认为此表的主键为非自动增量）
	 * 
	 * @param dataSourceName
	 *            数据源名称
	 * @param tableName
	 *            数据库表名称
	 * @param valueObjectClass
	 *            表对应的值对象
	 */
	@SuppressWarnings("unchecked")
	public TableOperator(String dataSourceName, String tableName,
			Class<T> valueObjectClass) {
		this.dsName = dataSourceName;
		Connection conn = null;
		try {
			conn = ConnectionFactory.newDsConncetion(this.dsName);
			String dbname = DBHelper.getDBMSName(conn);
			String tableName2 = tableName;
			if (dbname != null && dbname.equalsIgnoreCase("oracle")) {
				tableName2 = tableName.toUpperCase();
			}
			this.tbName = tableName2;
			this.voClass = valueObjectClass;
			this.autoGenerateKey = false;
			filedSet = (Set<String>) SQL_CACHER.get(this.tbName);
			String primaryKeyName_ = (String) SQL_CACHER.get(this.tbName
					+ "_pk");
			if (filedSet == null || primaryKeyName_ == null) {
				filedSet = DBHelper.getTableFields(this.tbName, conn);
				if (filedSet == null || filedSet.isEmpty()) {
					throw new DBOperatorException(
							"Can't retrieved from the database of this "
									+ this.tbName + " structured data!");
				}
				SQL_CACHER.put(this.tbName, filedSet);
				primaryKeyName_ = DBHelper.getTablePrimaryKeyFieldName(
						this.tbName, conn);
				SQL_CACHER.put(this.tbName + "_pk", primaryKeyName_);
				logger.debug("filedSet:{}", filedSet);
				logger.debug("primaryKeyName:{}", primaryKeyName_);
				logger.info(this.tbName + "-->inited!");
			}
			this.primaryKeyName = primaryKeyName_;
		} catch (ConnectionException ce) {
			logger.error(ce);
			throw new DBOperatorException(ce);
		} catch (SQLException ex) {
			logger.error(ex);
			throw new DBOperatorException(ex);
		} finally {
			if (conn != null) {
				try {
					if (!conn.isClosed()) {
						conn.close();
					}
				} catch (Exception e) {
					logger.error(e);
				}
			}
		}
	}

	/**
	 * TableOperator
	 * 
	 * @param dataSourceName
	 *            数据源名称
	 * @param tableName
	 *            数据库表名称
	 * @param valueObjectClass
	 *            表对应的值对象
	 * @param autoKeyFiledName
	 *            自动增量的key的字段名称
	 */

	public TableOperator(String dataSourceName, String tableName,
			Class<T> valueObjectClass, String autoKeyFiledName) {
		this(dataSourceName, tableName, valueObjectClass);
		if (autoKeyFiledName != null) {
			if (!autoKeyFiledName.equals("")) {
				this.autoGenerateKey = true;
				this.autoKeyFiledName = autoKeyFiledName;
			}
		}
	}

	/**
	 * TableOperator
	 * 
	 * @param dataSourceName
	 *            数据源名称
	 * @param tableName
	 *            数据库表名称
	 * @param valueObjectClass
	 *            表对应的值对象
	 * @param autoKeyFlag
	 *            此表的主键是否为自动增量的
	 */
	public TableOperator(String dataSourceName, String tableName,
			Class<T> valueObjectClass, boolean autoKeyFlag) {
		this(dataSourceName, tableName, valueObjectClass);
		this.autoGenerateKey = autoKeyFlag;
		this.autoKeyFiledName = primaryKeyName;
	}

	/**
	 * 根据主键查找记录
	 * 
	 * @param pk
	 *            Object
	 * @return 返回此记录的表值对象
	 * @throws DBOperatorException
	 */
	@SuppressWarnings("unchecked")
	public T selectByPrimaryKey(Object pk) throws DBOperatorException {
		Object o = null;
		if (this.primaryKeyName == null) {
			throw new AppRuntimeException("此表没有定义主键或为组合主键，不支持此方法");
		}
		QueryOperator query = new QueryOperator();
		query.setDataSourceName(this.dsName);
		query.setSql(SqlGenerator.generateSelectByPKSql(this.filedSet,
				this.tbName, this.primaryKeyName));
		query.addParameter(pk);
		RsDataSet rs = null;
		try {
			query.access();
			if (query.resultSetAvailable()) {
				rs = new RsDataSet(query.getSqlResultSet());
				o = rowToObj(rs);
			}
			return (T) o;
		} finally {
			if (rs != null)
				rs.clearAll();
		}
	}

	private Object rowToObj(RsDataSet rs) throws AppRuntimeException {
		Object o;
		try {
			o = voClass.newInstance();
		} catch (Exception ex) {
			throw new AppRuntimeException("没法实例化对象", ex);
		}
		Iterator<String> it = this.filedSet.iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Object value = rs.getFieldValue(key);
			if (value == null) {
				continue;
			}
			try {
				ObjectUtil.setValue(key, o, value);
			} catch (IllegalArgumentException ille) {
				value = null;
				Class<?> type = ObjectUtil.getType(key, o);
				String tstr = type.toString();
				// System.out.println("---->:"+tstr);
				if (tstr.equals(Integer.class.toString())) {
					ObjectUtil.setValue(key, o, rs.getFieldValueAsInteger(key));
				} else if (tstr.equals(Long.class.toString())) {
					ObjectUtil.setValue(key, o, rs.getFieldValueAsLong(key));
				} else if (tstr.equals(Float.class.toString())) {
					ObjectUtil.setValue(key, o, rs.getFieldValueAsFloat(key));
				} else if (tstr.equals(Double.class.toString())) {
					ObjectUtil.setValue(key, o, rs.getFieldValueAsDouble(key));
				} else if (tstr.equals(Short.class.toString())) {
					ObjectUtil.setValue(key, o, rs.getFieldValueAsShort(key));
				} else if (tstr.equals(Byte.class.toString())) {
					ObjectUtil.setValue(key, o, rs.getFieldValueAsByte(key));
				}
				tstr = null;
				type = null;
			}
		}
		return o;
	}

	/*
	 * private Object rowToObj(RsDataSet rs) throws AppRuntimeException { Object
	 * o = null; try { o = valueObjectClass.newInstance(); } catch (Exception
	 * ex) { throw new AppRuntimeException("没法实例化对象", ex); } BeanUtilsBean bu =
	 * BeanUtilsBean.getInstance(); Iterator it = this.filedSet.iterator();
	 * while (it.hasNext()) { String key = (String) it.next(); Object value =
	 * rs.getFieldValue(key); try { if (value == null) { continue; }
	 * bu.copyProperty(o, key, value); } catch (Exception ex1) { throw new
	 * AppRuntimeException("对此对象装配解析出现问题,请检查对象属性是否和数据库字段一致", ex1); } } return o;
	 * }
	 */

	/**
	 * 根据条件查找
	 * 
	 * @param whereStr
	 *            sql语句where条件子语句
	 * @param values
	 *            where语句参数值，按参数顺序填充数组
	 * @return 查询结果值对象列表 如果记录数为1时，通过list.get(0)获取
	 * @throws DBOperatorException
	 */

	@SuppressWarnings("unchecked")
	public List<T> selectByWhereCondition(String whereStr, Object values[])
			throws DBOperatorException {
		QueryOperator query = new QueryOperator();
		query.setDataSourceName(this.dsName);
		query.setSql(SqlGenerator.generateSelectAllSql(this.filedSet,
				this.tbName) + whereStr);
		int i = whereStr.indexOf("?");
		if (i > 0) {
			for (int j = 0; j < values.length; j++) {
				query.addParameter(values[j]);
			}
		}
		RsDataSet rs = null;
		try {
			query.access();
			if (query.resultSetAvailable()) {
				rs = new RsDataSet(query.getSqlResultSet());
				List<T> rd = new ArrayList<T>(rs.rowCount);
				for (int ii = 0; ii < rs.rowCount; ii++) {
					Object o = rowToObj(rs);
					rd.add((T) o);
					rs.next();
				}
				return rd;
			} else {
				return new ArrayList<T>();
			}
		} finally {
			if (rs != null)
				rs.clearAll();
		}
	}

	/**
	 * 插入一条记录
	 * 
	 * @param valueObject
	 *            表的值对象
	 * @return 操作影响条数
	 * @throws DBOperatorException
	 */
	public int insert(T valueObject) throws DBOperatorException {
		Map<?, ?> valueMap = objToMap(valueObject, this.filedSet);
		UpdateOperator update = new UpdateOperator();
		update.setDataSourceName(this.dsName);
		update.setSql(SqlGenerator.generateInsertSql(this.filedSet,
				this.tbName, this.autoGenerateKey, this.autoKeyFiledName));
		Iterator<String> it = this.filedSet.iterator();
		while (it.hasNext()) {
			Object key = it.next();
			if (autoGenerateKey) {
				if (key.equals(autoKeyFiledName)) {
					continue;
				}
			}
			addUpdateParams(valueMap, update, key);
		}
		try {
			update.access();
			return update.getEffectCounts();
		} finally {
			valueMap.clear();
		}
	}

	private void addUpdateParams(Map<?, ?> valueMap, UpdateOperator update,
			Object key) {
		ValueInfo vi = (ValueInfo) valueMap.get(key);
		if (vi.getValue() != null) {
			update.addParameter(vi.getValue());
		} else {
			Class<?> type = vi.getType();
			if (type.equals(Integer.class)) {
				update.addParameter(new SqlParameter(SqlType.INTEGER, null));
			} else if (type.equals(String.class)) {
				update.addParameter(new SqlParameter(SqlType.VARCHAR, null));
			} else if (type.equals(Long.class)) {
				update.addParameter(new SqlParameter(SqlType.BIGINT, null));
			} else if (type.equals(java.sql.Timestamp.class)) {
				update.addParameter(new SqlParameter(SqlType.TIMESTAMP, null));
			} else if (type.equals(java.sql.Time.class)) {
				update.addParameter(new SqlParameter(SqlType.TIME, null));
			} else if (type.equals(Double.class)) {
				update.addParameter(new SqlParameter(SqlType.DOUBLE, null));
			} else if (type.equals(java.sql.Date.class)) {
				update.addParameter(new SqlParameter(SqlType.DATE, null));
			} else { // ...
						// System.out.println("-----"+key);
				update.addParameter(new SqlParameter(SqlType.NUMERIC, null));
			}
		}
	}

	private static class ValueInfo {
		private Object value;

		private Class<?> type;

		public ValueInfo(Object value, Class<?> type) {
			this.value = value;
			this.type = type;
		}

		public Object getValue() {
			return value;
		}

		public Class<?> getType() {
			return type;
		}
	}

	private Map<String, ValueInfo> mapToMap(Map<String, Object> vs,
			Set<String> fields) {
		Map<String, ValueInfo> map = new HashMap<String, ValueInfo>();
		Iterator<String> it = fields.iterator();
		while (it.hasNext()) {
			String fn = (String) it.next();
			Class<?> type = vs.get(fn).getClass();
			Object value = vs.get(fn);
			if (value != null) {
				map.put(fn, new ValueInfo(value, type));
			} else {
				map.put(fn, new ValueInfo(null, type));
			}
		}
		return map;
	}

	private Map<String, ValueInfo> objToMap(Object obj, Set<String> fields) {
		Map<String, ValueInfo> map = new HashMap<String, ValueInfo>();
		Iterator<String> it = fields.iterator();
		while (it.hasNext()) {
			String fn = (String) it.next();
			Class<?> type = ObjectUtil.getType(fn, obj);
			Object value = ObjectUtil.getValue(fn, obj);
			if (value != null) {
				map.put(fn, new ValueInfo(value, type));
			} else {
				map.put(fn, new ValueInfo(null, type));
			}
		}
		return map;
	}

	/*
	 * private Map objToMap(Object obj, Set fields) { Map map = new HashMap();
	 * BeanUtilsBean bub = BeanUtilsBean.getInstance(); PropertyUtilsBean pup =
	 * bub.getPropertyUtils(); PropertyDescriptor dt[] =
	 * pup.getPropertyDescriptors(obj); for (int i = 0; i < dt.length; i++) {
	 * String name = dt[i].getName(); Class type = dt[i].getPropertyType(); try
	 * { Object value = pup.getProperty(obj, name); Iterator it =
	 * fields.iterator(); while (it.hasNext()) { String fn = (String) it.next();
	 * if (fn.equals(name)) { map.put(fn, new ValueInfo(value, type)); break; }
	 * } } catch (Exception e) { throw new
	 * org.wcc.framework.AppRuntimeException("对此对象解析出现问题，不支持", e); } } return
	 * map; }
	 */

	/**
	 * 批量插入记录
	 * 
	 * @param valueObjectList
	 *            List--此列表不会清空
	 * @return int[]
	 * @throws DBOperatorException
	 */
	public int[] insertBatch(List<T> valueObjectList)
			throws DBOperatorException {
		UpdateOperator update = new UpdateOperator();
		update.setDataSourceName(this.dsName);
		update.setSql(SqlGenerator.generateInsertSql(this.filedSet,
				this.tbName, this.autoGenerateKey, this.autoKeyFiledName));
		for (int i = 0; i < valueObjectList.size(); i++) {
			Object valueObject = valueObjectList.get(i);
			SqlParameterSet r = new SqlParameterSet();
			Map<String, ValueInfo> valueMap = objToMap(valueObject,
					this.filedSet);
			Iterator<String> it = this.filedSet.iterator();
			while (it.hasNext()) {
				Object key = it.next();
				if (autoGenerateKey) {
					if (key.equals(autoKeyFiledName)) {
						continue;
					}
				}
				addBatchParams(r, valueMap, key);
			}
			update.addBatchParameter(r); //
			valueMap.clear();
		}
		try {
			update.access();
			return update.getBatchEffectCounts();
		} finally {
			// valueObjectList.clear();
		}
	}

	private void addBatchParams(SqlParameterSet r,
			Map<String, ValueInfo> valueMap, Object key) {
		ValueInfo vi = (ValueInfo) valueMap.get(key);
		if (vi.getValue() != null) {
			r.addParameter(vi.getValue());
		} else {
			Class<?> type = vi.getType();
			if (type.equals(Integer.class)) {
				r.addParameter(new SqlParameter(SqlType.INTEGER, null));
			} else if (type.equals(String.class)) {
				r.addParameter(new SqlParameter(SqlType.VARCHAR, null));
			} else if (type.equals(Long.class)) {
				r.addParameter(new SqlParameter(SqlType.BIGINT, null));
			} else if (type.equals(java.sql.Timestamp.class)) {
				r.addParameter(new SqlParameter(SqlType.TIMESTAMP, null));
			} else if (type.equals(java.sql.Time.class)) {
				r.addParameter(new SqlParameter(SqlType.TIME, null));
			} else if (type.equals(Double.class)) {
				r.addParameter(new SqlParameter(SqlType.DOUBLE, null));
			} else if (type.equals(java.sql.Date.class)) {
				r.addParameter(new SqlParameter(SqlType.DATE, null));
			} else { // ...
				r.addParameter(new SqlParameter(SqlType.NUMERIC, null));
			}
		}
	}

	/**
	 * 根据主键删除记录
	 * 
	 * @param pk
	 *            主键
	 * @return 影响记录数
	 * @throws DBOperatorException
	 */
	public int deleteByPrimaryKey(Object pk) throws DBOperatorException {
		if (this.primaryKeyName == null) {
			throw new AppRuntimeException("此表没有定义主键或为组合主键，不支持此方法");
		}
		UpdateOperator update = new UpdateOperator();
		update.setDataSourceName(this.dsName);
		update.setSql(SqlGenerator.generateDeleteByPKSql(this.tbName,
				this.primaryKeyName));
		update.addParameter(pk);
		try {
			update.access();
			return update.getEffectCounts();
		} catch (DBOperatorException ex) {
			logger.error(ex);
			throw new DBOperatorException(ex);
		}
	}

	/**
	 * 自动填充一行（将sql返回的字段值与其对应的值对象自动匹配起来）
	 * 
	 * @param rs
	 *            RsDataSet
	 * @return Object
	 */
	@SuppressWarnings("unchecked")
	public T autoRowFill(RsDataSet rs) {
		try {
			Object o = rowToObj(rs);
			return (T) o;
		} finally {
			rs.clearAll();
		}
	}

	/**
	 * 自动填充所有的结果
	 * 
	 * @param rs
	 *            查询结果集－RsDataSet
	 * @return List
	 */
	@SuppressWarnings("unchecked")
	public List<T> autoAllFill(RsDataSet rs) {
		List<T> l = new ArrayList<T>(rs.rowCount);
		try {
			for (int ii = 0; ii < rs.rowCount; ii++) {
				Object o = rowToObj(rs);
				l.add((T) o);
				rs.next();
			}
		} finally {
			rs.clearAll();
		}
		return l;
	}

	/**
	 * 根据主键批量删除
	 * 
	 * @param pks
	 *            List,此列表不会清空
	 * @throws DBOperatorException
	 */
	@SuppressWarnings("rawtypes")
	public int[] deleteBatchByPrimaryKey(List pks) throws DBOperatorException {
		if (this.primaryKeyName == null) {
			throw new AppRuntimeException("此表没有定义主键或为组合主键，不支持此方法");
		}
		UpdateOperator update = new UpdateOperator();
		update.setDataSourceName(this.dsName);
		update.setSql(SqlGenerator.generateDeleteByPKSql(this.tbName,
				this.primaryKeyName));
		for (int i = 0; i < pks.size(); i++) {
			Object pk = pks.get(i);
			SqlParameterSet r = new SqlParameterSet();
			SqlParameter p = new SqlParameter(pk);
			r.addParameter(p);
			update.addBatchParameter(r);
		}
		try {
			update.access();
			return update.getBatchEffectCounts();
		} catch (DBOperatorException ex) {
			logger.error(ex);
			throw new DBOperatorException(ex);
		} finally {
			// pks.clear();
		}
	}

	public int update(final Map<String, Object> fieldValues)
			throws DBOperatorException {
		if (this.primaryKeyName == null) {
			throw new AppRuntimeException("此表没有定义主键或为组合主键，不支持此方法");
		}
		Set<String> fss = fieldValues.keySet();
		Map<?, ?> valueMap = mapToMap(fieldValues, fss);
		UpdateOperator update = new UpdateOperator();
		update.setDataSourceName(this.dsName);
		update.setSql(SqlGenerator.generateUpdateByPKSql(fss, this.tbName,
				this.primaryKeyName, this.autoGenerateKey,
				this.autoKeyFiledName));
		Iterator<String> it = fss.iterator();
		Object pkValue = null;
		while (it.hasNext()) {
			Object key = it.next();
			ValueInfo vi = (ValueInfo) valueMap.get(key);
			if (key.equals(this.primaryKeyName)) {
				pkValue = vi.getValue();
				if (this.autoGenerateKey) {
					continue;
				}
			}
			// update.addParameter(o);
			addUpdateParams(valueMap, update, key);
		}
		update.addParameter(pkValue);
		try {
			update.access();
			return update.getEffectCounts();
		} finally {
			valueMap.clear();
		}
	}

	/**
	 * 更新记录
	 * 
	 * @param valueObject
	 *            Object
	 * @return 操作影响记录数
	 * @throws DBOperatorException
	 */
	public int update(T valueObject) throws DBOperatorException {
		if (this.primaryKeyName == null) {
			throw new AppRuntimeException("此表没有定义主键或为组合主键，不支持此方法");
		}
		Map<?, ?> valueMap = objToMap(valueObject, this.filedSet);
		UpdateOperator update = new UpdateOperator();
		update.setDataSourceName(this.dsName);
		update.setSql(SqlGenerator.generateUpdateByPKSql(this.filedSet,
				this.tbName, this.primaryKeyName, this.autoGenerateKey,
				this.autoKeyFiledName));
		Iterator<String> it = this.filedSet.iterator();
		Object pkValue = null;
		while (it.hasNext()) {
			Object key = it.next();
			ValueInfo vi = (ValueInfo) valueMap.get(key);
			if (key.equals(this.primaryKeyName)) {
				pkValue = vi.getValue();
				if (this.autoGenerateKey) {
					continue;
				}
			}
			// update.addParameter(o);
			addUpdateParams(valueMap, update, key);
		}
		update.addParameter(pkValue);
		try {
			update.access();
			return update.getEffectCounts();
		} finally {
			valueMap.clear();
		}
	}

	/**
	 * 批量更新记录
	 * 
	 * @param valueObjectList
	 *            List --此列表不会清空
	 * @return int[]
	 * @throws DBOperatorException
	 */
	public int[] updateBatch(List<T> valueObjectList)
			throws DBOperatorException {
		if (this.primaryKeyName == null) {
			throw new AppRuntimeException("此表没有定义主键或为组合主键，不支持此方法");
		}
		UpdateOperator update = new UpdateOperator();
		update.setDataSourceName(this.dsName);
		update.setSql(SqlGenerator.generateUpdateByPKSql(this.filedSet,
				this.tbName, this.primaryKeyName, this.autoGenerateKey,
				this.autoKeyFiledName));
		for (int i = 0; i < valueObjectList.size(); i++) {
			Object valueObject = valueObjectList.get(i);
			SqlParameterSet r = new SqlParameterSet();
			Map<String, ValueInfo> valueMap = objToMap(valueObject,
					this.filedSet);
			Iterator<String> it = this.filedSet.iterator();
			Object pkValue = null;
			while (it.hasNext()) {
				Object key = it.next();
				ValueInfo vi = (ValueInfo) valueMap.get(key);
				if (key.equals(this.primaryKeyName)) {
					pkValue = vi.getValue();
					if (this.autoGenerateKey) {
						continue;
					}
				}
				addBatchParams(r, valueMap, key); //
			}
			r.addParameter(pkValue);
			update.addBatchParameter(r);
			valueMap.clear();
		}
		try {
			update.access();
			return update.getBatchEffectCounts();
		} finally {
			// valueObjectList.clear();
		}
	}

	/**
	 * 生成此表字段名字符串
	 * 
	 * @return String
	 */
	public String generateFieldsString() {
		return SqlGenerator.generateFieldsFormatStr(this.filedSet);
	}

	/**
	 * 此表是否为自动生成id
	 * 
	 * @return boolean
	 */
	public boolean isAutoGenerateKey() {
		return autoGenerateKey;
	}

	/**
	 * 获取表名
	 * 
	 * @return String
	 */
	public String getTableName() {
		return this.tbName;
	}

	/**
	 * 获取主键名称
	 * 
	 * @return String
	 */
	public String getPrimaryKeyFieldName() {
		return this.primaryKeyName;
	}

}
