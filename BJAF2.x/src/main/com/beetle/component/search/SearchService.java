package com.beetle.component.search;

import java.util.List;

import com.beetle.component.search.def.Record;
import com.beetle.component.search.def.StoreType;

public interface SearchService {
	/**
	 * 创建索引库
	 * 
	 * @param storeType
	 *            索引库的存储类型，是放内存还是文件
	 * @param uid
	 *            索引库唯一标识
	 * @param path
	 *            索引的存储路径
	 * @throws SearchServiceException
	 */
	void createStore(StoreType storeType, String uid, String path)
			throws SearchServiceException;

	/**
	 * 添加索引记录到索引库
	 * 
	 * @param uid
	 *            索引库唯一标识
	 * @param records
	 *            索引记录，添加成功后，List内容会被清空
	 * @throws SearchServiceException
	 */
	void addRecordsToStore(String uid, List<Record> records)
			throws SearchServiceException;

	/**
	 * 根据查询表达式删除索引库的记录
	 * 
	 * @param uid
	 *            索引库唯一标识
	 * @param queryExpression
	 *            查询表达式
	 * @throws SearchServiceException
	 */
	void deleteRecordsFromStore(String uid, String queryExpression)
			throws SearchServiceException;

	/**
	 * 删除索引库，删除后必须重新建立
	 * 
	 * @param uid
	 *            索引库唯一标识
	 * @throws SearchServiceException
	 */
	void deleteStore(String uid) throws SearchServiceException;

	/**
	 * 检索
	 * 
	 * @param uid
	 *            索引库唯一标识
	 * @param queryExpression
	 *            查询表达式
	 * @return 结果记录列表
	 * @throws SearchServiceException
	 */
	List<Record> search(String uid, String queryExpression)
			throws SearchServiceException;
}
