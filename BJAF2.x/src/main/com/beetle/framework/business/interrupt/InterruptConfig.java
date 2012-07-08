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
package com.beetle.framework.business.interrupt;

/**
 * @author Henry Yu 2005-4-11
 *
 */

import com.beetle.framework.AppProperties;
import com.beetle.framework.log.AppLogger;
import com.beetle.framework.util.ClassUtil;
import com.beetle.framework.util.ResourceLoader;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class InterruptConfig {

	private static Map<String, List<ActionAttr>> COMMAND_ACTION = new HashMap<String, List<ActionAttr>>();
	private static boolean com_read_flag = false;
	private static boolean dele_read_flag = false;
	private static Map<String, List<ActionAttr>> DELEDATE_ACTION = new HashMap<String, List<ActionAttr>>();
	private static InterruptConfig instance = new InterruptConfig();

	private InterruptConfig() {

	}

	public static InterruptConfig getInstance() {
		return instance;
	}

	public void readCommandActions(File xmlfile, InputStream xmlFileInputStream) {
		COMMAND_ACTION.clear();
		SAXReader reader = new SAXReader();
		try {
			Document doc;
			if (xmlfile != null && xmlFileInputStream == null) {
				doc = reader.read(xmlfile);
			} else if (xmlfile == null && xmlFileInputStream != null) {
				doc = reader.read(xmlFileInputStream);
			} else {
				return;
			}
			List<?> list = doc.selectNodes("//Interrupt/Command/@ClassName");
			Iterator<?> it = list.iterator();
			while (it.hasNext()) {
				Node node = (Node) it.next();
				String commandKey = node.getText();
				List<ActionAttr> actions = new LinkedList<ActionAttr>();
				Element element = node.getParent();
				Iterator<?> iterator = element.elementIterator("Action");
				while (iterator.hasNext()) {
					ActionAttr attr = new ActionAttr();
					Element e = (Element) iterator.next();
					attr.setClassName(e.valueOf("@ClassName"));
					attr.setPointCut(e.valueOf("@PointCut"));
					// attr.setThreadSafe(toBool(e.valueOf("@ThreadSafe")));
					attr.setThreadSafe(checkClassIsThreadSafe(attr
							.getClassName()));
					actions.add(attr);
				}
				COMMAND_ACTION.put(commandKey, actions);
			}
		} catch (Exception de) {
			de.printStackTrace();
		}
	}

	private boolean checkClassIsThreadSafe(String classname) {
		boolean abl;
		try {
			abl = ClassUtil.isThreadSafe(Class.forName(classname));
		} catch (ClassNotFoundException ex) {
			throw new com.beetle.framework.AppRuntimeException(
					"ClassNotFoundException:[" + classname + "]not found!", ex);
		}
		return abl;
	}

	/*
	 * private boolean toBool(String a) { boolean abl = false; if
	 * (a.toLowerCase().equals("true")) { abl = true; } return abl; }
	 */
	public void readDelegateActions(File xmlfile, InputStream xmlFileInputStream) {
		DELEDATE_ACTION.clear();
		SAXReader reader = new SAXReader();
		try {
			Document doc;
			if (xmlfile != null && xmlFileInputStream == null) {
				doc = reader.read(xmlfile);
			} else if (xmlfile == null && xmlFileInputStream != null) {
				doc = reader.read(xmlFileInputStream);
			} else {
				return;
			}
			List<?> list = doc.selectNodes("//Interrupt/Delegate/@ClassName");
			Iterator<?> it = list.iterator();
			// System.out.println(list.size());
			while (it.hasNext()) {
				Node node = (Node) it.next();
				String commandKey = node.getText();
				List<ActionAttr> actions = new LinkedList<ActionAttr>();
				Element element = node.getParent();
				Iterator<?> iterator = element.elementIterator("Action");
				while (iterator.hasNext()) {
					ActionAttr attr = new ActionAttr();
					Element e = (Element) iterator.next();
					attr.setClassName(e.valueOf("@ClassName"));
					attr.setPointCut(e.valueOf("@PointCut"));
					// attr.setThreadSafe(toBool(e.valueOf("@ThreadSafe")));
					attr.setThreadSafe(checkClassIsThreadSafe(attr
							.getClassName()));
					actions.add(attr);
				}
				DELEDATE_ACTION.put(commandKey, actions);
			}
		} catch (Exception de) {
			de.printStackTrace();
		}
	}

	public static Map<String, List<ActionAttr>> getCommandActions() {
		if (!com_read_flag) {
			File f = new File(AppProperties.getAppHome()
					+ "Interrupt.xml");
			if (f.exists()) {
				InterruptConfig.getInstance().readCommandActions(f, null);
				AppLogger.getInstance(InterruptConfig.class).info(
						"from file:[" + f.getPath() + "]");
			} else {
				try {
					InterruptConfig.getInstance().readCommandActions(
							null,
							ResourceLoader
									.getResAsStream("Interrupt.xml"));
					AppLogger.getInstance(InterruptConfig.class).info(
							"from jar:["
									+ ResourceLoader.getClassLoader()
											.toString() + "]");
				} catch (IOException ex) {
					AppLogger.getInstance(InterruptConfig.class).warn(
							"no Interrupt.xml found!");
				}
			}
			com_read_flag = true;
		}
		return COMMAND_ACTION;
	}

	public static Map<String, List<ActionAttr>> getDelegateActions() {
		if (!dele_read_flag) {
			File f = new File(AppProperties.getAppHome()
					+ "Interrupt.xml");
			if (f.exists()) {
				InterruptConfig.getInstance().readDelegateActions(f, null);
				AppLogger.getInstance(InterruptConfig.class).info(
						"from file:[" + f.getPath() + "]");
			} else {
				try {
					InterruptConfig.getInstance().readDelegateActions(
							null,
							ResourceLoader
									.getResAsStream("Interrupt.xml"));
					AppLogger.getInstance(InterruptConfig.class).info(
							"from jar:["
									+ ResourceLoader.getClassLoader()
											.toString() + "]");
				} catch (IOException ex) {
					AppLogger.getInstance(InterruptConfig.class).warn(
							"no Interrupt.xml found!");
				}
			}
			dele_read_flag = true;
		}
		return DELEDATE_ACTION;
	}

}
