package example.search;

import java.util.ArrayList;
import java.util.List;

import com.beetle.component.search.SearchService;
import com.beetle.component.search.def.Record;
import com.beetle.component.search.def.StoreType;
import com.beetle.component.search.imp.SearchServiceImpl;

public class Client {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Throwable {
		 SearchService ss = new SearchServiceImpl();
		//SearchService ss = ServiceFactory.serviceLookup(SearchService.class);
		String uid = "mySearch";
		ss.createStore(StoreType.FILE_CHINESE, uid, "D:\\temp");
		List<Record> records = new ArrayList<Record>();
		Record rec = new Record("1001");
		rec.addForIndex("txt",
				"Java,数组,TkRoot,我是刘德华，我在深圳,I am Henry Yu, i am a boy");
		rec.addForIndex("auther", "余浩东");
		records.add(rec);
		ss.addRecordsToStore(uid, records);
		List<Record> results = ss.search(uid, "余浩东");
		System.out.println(results);

	}

}
