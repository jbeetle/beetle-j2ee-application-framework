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
public class ActionAttr
    implements Serializable {
  private static final long serialVersionUID = -19760224l;

  private String className;
  private String pointCut;
  private boolean threadSafe;

  /**
   * @return Returns the className.
   */
  public String getClassName() {
    return className;
  }

  /**
   * @param className The className to set.
   */
  public void setClassName(String className) {
    this.className = className;
  }

  /**
   * @return Returns the pointCut.
   */
  public String getPointCut() {
    return pointCut;
  }

  public boolean isThreadSafe() {
    return threadSafe;
  }

  /**
   * @param pointCut The pointCut to set.
   */
  public void setPointCut(String pointCut) {
    this.pointCut = pointCut.toLowerCase();
  }

  public void setThreadSafe(boolean threadSafe) {
    this.threadSafe = threadSafe;
  }
}
