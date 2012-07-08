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

import java.io.Serializable;

/**
 * @author Henry Yu 2005-4-11
 *
 */
public class ActionSignal
    implements Serializable {
  private static final long serialVersionUID = -19760224l;

  /**
   * 过程终止，遇到此标记，主流程执行到此终止执行
   */
  public final static int PROCESS_BREAK = 10001;
  /**
   * 主流程继续执行
   */
  public final static int PROCESS_CONTINUE = 10002;
  private int processFlag = PROCESS_CONTINUE;
  public ActionSignal() {

  }

  public ActionSignal(int processFlag) {
    this.processFlag = processFlag;
  }

  /**
   * @return Returns the processFlag.
   */
  public int getProcessFlag() {
    return processFlag;
  }

  /**
   * @param processFlag The processFlag to set.
   */
  public void setProcessFlag(int processFlag) {
    this.processFlag = processFlag;
  }
}
