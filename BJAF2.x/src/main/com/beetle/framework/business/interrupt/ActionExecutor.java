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

import com.beetle.framework.business.command.CommandImp;
import com.beetle.framework.business.interrupt.cmd.ICmdAction;
import com.beetle.framework.log.AppLogger;
import com.beetle.framework.util.cache.ICache;
import com.beetle.framework.util.cache.StrongCache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Henry Yu 2005-4-11
 * 
 */

public class ActionExecutor {
	private final static String begin = "begin";
	private final static String end = "end";
	private static ICache cache = new StrongCache();
	private static AppLogger logger = AppLogger
			.getInstance(ActionExecutor.class);

	public static void endPointCutExecute(CommandImp cmd) {
		Map<String, List<ActionAttr>> map = InterruptConfig.getCommandActions();
		if (!map.isEmpty()) {
			List<ActionAttr> actions = new ArrayList<ActionAttr>();
			loadCmdActions(cmd, map, actions);
			if (actions.isEmpty()) {
				return;
			}
			Iterator<ActionAttr> it = actions.iterator();
			while (it.hasNext()) {
				ActionAttr attr = it.next();
				if (attr.getPointCut().equals(end)) {
					try {
						Object obj;
						if (attr.isThreadSafe()) {
							obj = cache.get(attr.getClassName());
							if (obj == null) {
								obj = Class.forName(attr.getClassName())
										.newInstance();
								cache.put(attr.getClassName(), obj);
								if (logger.isDebugEnabled()) {
									logger.debug("get action by new operate");
								}
							} else {
								if (logger.isDebugEnabled()) {
									logger.debug("get action from cache");
								}
							}
						} else {
							obj = Class.forName(attr.getClassName())
									.newInstance();
							if (logger.isDebugEnabled()) {
								logger.debug("get action by new operate");
							}
						}
						ICmdAction action = (ICmdAction) obj;
						action.execute(cmd);
						if (logger.isDebugEnabled()) {
							logger.debug("executed["
									+ action.getClass().getName()
									+ "]action at [" + cmd.getClass().getName()
									+ "] end pointcut");
						}
					} catch (Exception e) {
						logger.error(e);
					}
				}
			}
			actions.clear();
		}
	}

	/**
	 * beginPointCutExecute
	 * 
	 * @param cmd
	 *            CommandImp
	 * @return int
	 */
	private static final String ALL_COMMAND = "ALL_COMMAND";

	public static int beginPointCutExecute(CommandImp cmd) {
		Map<String, List<ActionAttr>> map = InterruptConfig.getCommandActions();
		if (!map.isEmpty()) {
			List<ActionAttr> actions = new ArrayList<ActionAttr>();
			loadCmdActions(cmd, map, actions);
			if (actions.isEmpty()) {
				return ActionSignal.PROCESS_CONTINUE;
			}
			Iterator<ActionAttr> it = actions.iterator();
			while (it.hasNext()) {
				ActionAttr attr = it.next();
				if (attr.getPointCut().equals(begin)) {
					try {
						Object obj;
						if (attr.isThreadSafe()) {
							obj = cache.get(attr.getClassName());
							if (obj == null) {
								obj = Class.forName(attr.getClassName())
										.newInstance();
								if (logger.isDebugEnabled()) {
									logger.debug("get action by new operate");
								}
								cache.put(attr.getClassName(), obj);
							} else {
								if (logger.isDebugEnabled()) {
									logger.debug("get action from cache");
								}
							}
						} else {
							obj = Class.forName(attr.getClassName())
									.newInstance();
							if (logger.isDebugEnabled()) {
								logger.debug("get action by new operate");
							}
						}
						ICmdAction action = (ICmdAction) obj;
						ActionSignal signal = action.execute(cmd);
						if (logger.isDebugEnabled()) {
							logger.debug("executed["
									+ action.getClass().getName()
									+ "]action at [" + cmd.getClass().getName()
									+ "] begen pointcut");
						}
						if (signal.getProcessFlag() == ActionSignal.PROCESS_BREAK) {
							actions.clear();
							return ActionSignal.PROCESS_BREAK;
						}
					} catch (Exception e) {
						logger.error(e);
					}
				}
			}
			actions.clear();
		}
		return ActionSignal.PROCESS_CONTINUE;
	}

	private static void loadCmdActions(CommandImp cmd,
			Map<String, List<ActionAttr>> map, List<ActionAttr> actions) {
		List<ActionAttr> all_list = map.get(ALL_COMMAND);
		if (all_list != null && !all_list.isEmpty()) {
			actions.addAll(all_list);
			// all_list.clear();
			if (logger.isDebugEnabled()) {
				logger.debug("load [ALL_COMMAND] actions");
			}
		}
		String key = cmd.getClass().getName();
		int pos = key.indexOf('.');
		String path = key.substring(0, pos + 1);
		List<ActionAttr> path_list = map.get(path);
		if (path_list != null && !path_list.isEmpty()) {
			actions.addAll(path_list);
			// path_list.clear();
			if (logger.isDebugEnabled()) {
				logger.debug("load [package path] actions");
			}
		}
		List<ActionAttr> class_list = map.get(key);
		if (class_list != null && !class_list.isEmpty()) {
			actions.addAll(class_list);
			// class_list.clear();
			if (logger.isDebugEnabled()) {
				logger.debug("load [class] actions");
			}
		}
	}

}
