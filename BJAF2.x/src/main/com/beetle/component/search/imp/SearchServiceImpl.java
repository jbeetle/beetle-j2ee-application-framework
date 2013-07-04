package com.beetle.component.search.imp;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import com.beetle.component.search.SearchService;
import com.beetle.component.search.SearchServiceException;
import com.beetle.component.search.def.Record;
import com.beetle.component.search.def.StoreType;
import com.beetle.framework.AppProperties;
import com.beetle.framework.log.AppLogger;
import com.webssky.jcseg.core.Config;
import com.webssky.jcseg.lucene.JcsegAnalyzer4X;

public class SearchServiceImpl implements SearchService {
	private static final AppLogger logger = AppLogger
			.getInstance(SearchServiceImpl.class);

	public SearchServiceImpl() {
		super();
	}

	@Override
	public void createStore(StoreType storeType, String uid, String path)
			throws SearchServiceException {
		if (storeType.equals(StoreType.MEMORY_ENGLISH)) {
			dealCache(storeType, uid, path,
					new StopAnalyzer(Version.LUCENE_43), new RAMDirectory());
		} else if (storeType.equals(StoreType.MEMORY_CHINESE)) {
			Config.LOAD_CJK_PINYIN = true;
			dealCache(storeType, uid, path, new JcsegAnalyzer4X(
					Config.COMPLEX_MODE), new RAMDirectory());
		} else if (storeType.equals(StoreType.FILE_ENGLISH)) {
			try {
				dealCache(storeType, uid, path, new StopAnalyzer(
						Version.LUCENE_43), FSDirectory.open(new File(path)));
			} catch (IOException e) {
				throw new SearchServiceException(e);
			}
		} else if (storeType.equals(StoreType.FILE_CHINESE)) {
			try {
				Config.LOAD_CJK_PINYIN = true;
				dealCache(storeType, uid, path, new JcsegAnalyzer4X(
						Config.COMPLEX_MODE), FSDirectory.open(new File(path)));
			} catch (IOException e) {
				throw new SearchServiceException(e);
			}
		} else {
			throw new SearchServiceException("not support this storeType!");
		}
	}

	private void dealCache(StoreType storeType, String uid, String path,
			Analyzer analyzer, Directory dir) {
		StoreInfo sm = new StoreInfo();
		sm.setPath(path);
		sm.setStoreType(storeType);
		sm.setUid(uid);
		sm.setAnalyzer(analyzer);
		sm.setDir(dir);
		StoreManager.getInstance().putIntoInfoCache(uid, sm);
	}

	@Override
	public void addRecordsToStore(String uid, List<Record> records)
			throws SearchServiceException {
		final StoreInfo info = StoreManager.getInstance().getFromInfoCache(uid);
		if (info == null) {
			throw new SearchServiceException(uid + " store can't be found!");
		}
		synchronized (info) {
			IndexWriter writer = null;
			try {
				Directory dir = info.getDir();
				if (dir == null) {
					dir = reNewDir(info);
				}
				Analyzer analyzer = info.getAnalyzer();
				if (analyzer == null) {
					analyzer = reNewAnalyzer(info);
				}
				writer = new IndexWriter(dir, new IndexWriterConfig(
						Version.LUCENE_43, analyzer));
				for (Record rec : records) {
					Document doc = new Document();
					doc.add(new StringField(Record.RECORD_KEY, rec.getId(),
							Field.Store.YES));
					Iterator<Entry<String, String>> it = rec
							.getToIndexAttributes().entrySet().iterator();
					if (info.getIndexKeys() == null
							|| info.getIndexKeys().length == 0) {
						String[] tmp = new String[rec.getToIndexAttributes()
								.size()];
						rec.getToIndexAttributes().keySet().toArray(tmp);
						info.setIndexKeys(tmp);
					}
					while (it.hasNext()) {
						Map.Entry<String, String> kv = it.next();
						doc.add(new TextField(kv.getKey(), kv.getValue(),
								Field.Store.NO));
					}
					rec.clear();
					if (logger.isDebugEnabled()) {
						logger.debug("doc:{}", doc);
					}
					writer.addDocument(doc);
				}
			} catch (IOException e) {
				logger.error(e);
			} finally {
				records.clear();
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						logger.error(e);
					}
				}
			}
		}
	}

	private Analyzer reNewAnalyzer(final StoreInfo info) {
		Analyzer analyzer;
		if (info.getStoreType().equals(StoreType.MEMORY_ENGLISH)
				|| info.getStoreType().equals(StoreType.FILE_ENGLISH)) {
			analyzer = new StopAnalyzer(Version.LUCENE_43);
		} else {
			analyzer = new JcsegAnalyzer4X(Config.COMPLEX_MODE);
		}
		info.setAnalyzer(analyzer);
		return info.getAnalyzer();
	}

	private Directory reNewDir(final StoreInfo info) throws IOException {
		if (info.getStoreType().equals(StoreType.MEMORY_ENGLISH)
				|| info.getStoreType().equals(StoreType.MEMORY_CHINESE)) {
			info.setDir(new RAMDirectory());
		} else {
			info.setDir(FSDirectory.open(new File(info.getPath())));
		}
		return info.getDir();
	}

	@Override
	public void deleteStore(String uid) throws SearchServiceException {
		final StoreInfo info = StoreManager.getInstance().getFromInfoCache(uid);
		if (info == null) {
			throw new SearchServiceException(uid + " store can't be found!");
		}
		Directory dir = info.getDir();
		try {
			if (dir == null) {
				dir = reNewDir(info);
			}
			dir.deleteFile(info.getPath());
		} catch (IOException e) {
			throw new SearchServiceException(e);
		} finally {
			try {
				dir.close();
			} catch (IOException e) {
				logger.error(e);
			}
			StoreManager.getInstance().removeFromCache(uid);
		}
	}

	@Override
	public List<Record> search(String uid, String queryExpression)
			throws SearchServiceException {
		final StoreInfo info = StoreManager.getInstance().getFromInfoCache(uid);
		if (info == null) {
			throw new SearchServiceException(uid + " store can't be found!");
		}
		if (info.getIndexKeys() == null || info.getIndexKeys().length == 0) {
			logger.warn("info[{}]index keys no data,return it", info);
			throw new SearchServiceException(
					"info[{}]index keys no data found ,interupt search!");
		}
		Directory dir = info.getDir();
		if (dir == null) {
			synchronized (info) {
				if (info.getDir() == null) {
					try {
						dir = reNewDir(info);
					} catch (IOException e) {
						throw new SearchServiceException(e);
					}
				}
			}
		}
		Analyzer analyzer = info.getAnalyzer();
		if (analyzer == null) {
			synchronized (info) {
				if (info.getAnalyzer() == null) {
					analyzer = reNewAnalyzer(info);
				}
			}
		}
		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(dir);
			IndexSearcher searcher = new IndexSearcher(reader);
			MultiFieldQueryParser qp = new MultiFieldQueryParser(
					Version.LUCENE_43, info.getIndexKeys(), analyzer);
			TopDocs docs = searcher.search(
					qp.parse(queryExpression.toLowerCase()), null,
					AppProperties.getAsInt("component.search.topN", 100));
			logger.debug("hits:{}", docs.totalHits);
			List<Record> reslist = new LinkedList<Record>();
			for (int i = 0; i < docs.totalHits; i++) {
				ScoreDoc sdoc = docs.scoreDocs[i];
				Document doc = searcher.doc(sdoc.doc);
				if (logger.isDebugEnabled()) {
					logger.debug("doc:{}", doc);
				}
				String keyvalue = doc.get(Record.RECORD_KEY);
				Record rec = new Record(keyvalue);
				rec.setDocID(sdoc.doc);
				reslist.add(rec);
			}
			return reslist;
		} catch (Exception e) {
			throw new SearchServiceException("create index searcher err", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
	}

	@Override
	public void deleteRecordsFromStore(String uid, String queryExpression)
			throws SearchServiceException {
		final StoreInfo info = StoreManager.getInstance().getFromInfoCache(uid);
		if (info == null) {
			throw new SearchServiceException(uid + " store can't be found!");
		}
		synchronized (info) {
			if (info.getIndexKeys() == null || info.getIndexKeys().length == 0) {
				logger.warn("info[{}]index keys no data,return it", info);
				return;
			}
			IndexWriter writer = null;
			try {
				Directory dir = info.getDir();
				if (dir == null) {
					dir = reNewDir(info);
				}
				Analyzer analyzer = info.getAnalyzer();
				if (analyzer == null) {
					analyzer = reNewAnalyzer(info);
				}
				writer = new IndexWriter(dir, new IndexWriterConfig(
						Version.LUCENE_43, analyzer));
				MultiFieldQueryParser qp = new MultiFieldQueryParser(
						Version.LUCENE_43, info.getIndexKeys(), analyzer);
				writer.deleteDocuments(qp.parse(queryExpression));
				writer.commit();
			} catch (Exception e) {
				logger.error(e);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						logger.error(e);
					}
				}
			}
		}
	}

	@Override
	public String highlight(String uid, String source, String queryExpression)
			throws SearchServiceException {
		final StoreInfo info = StoreManager.getInstance().getFromInfoCache(uid);
		if (info == null) {
			throw new SearchServiceException(uid + " store can't be found!");
		}
		Analyzer analyzer = info.getAnalyzer();
		if (analyzer == null) {
			analyzer = reNewAnalyzer(info);
		}
		MultiFieldQueryParser qp = new MultiFieldQueryParser(Version.LUCENE_43,
				info.getIndexKeys(), analyzer);
		Formatter formatter = new SimpleHTMLFormatter(
				AppProperties
						.get("component.search.highlight.preTag",
								"<span style='color:red;background:yellow;'>"),
				AppProperties.get("component.search.highlight.postTag",
						"</span>"));
		try {
			Scorer scorer = new QueryScorer(qp.parse(queryExpression));
			Highlighter highlighter = new Highlighter(formatter, scorer);
			Fragmenter fragmenter = new SimpleFragmenter(
					AppProperties.getAsInt(
							"component.search.highlight.fragmenter.length",
							source.length() * 2));
			highlighter.setTextFragmenter(fragmenter);
			String rt = null;
			for (int i = 0; i < info.getIndexKeys().length; i++) {
				rt = highlighter.getBestFragment(analyzer,
						info.getIndexKeys()[i], source);
				if (rt != null) {
					break;
				}
			}
			return rt;
		} catch (Exception e) {
			throw new SearchServiceException(e);
		}
	}
}
