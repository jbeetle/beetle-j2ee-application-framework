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
package com.beetle.framework.web.controller;

import com.beetle.framework.web.common.CommonUtil;
import com.beetle.framework.web.common.WebConst;
import com.beetle.framework.web.controller.upload.FileObj;
import com.beetle.framework.web.controller.upload.IUpload;
import com.beetle.framework.web.controller.upload.UploadFactory;
import com.beetle.framework.web.controller.upload.UploadForm;
import com.beetle.framework.web.view.View;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * Title: BeetleWeb
 * </p>
 * 
 * <p>
 * Description: 文件上传控制器
 * 
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * 
 * <p>
 * Company: 甲壳虫软件
 * 
 * </p>
 * 
 * @author 余浩东(hdyu@beetlesoft.net)
 * @version 1.0
 */
public class UploadController extends ControllerImp {
	public UploadController() {
		super();
		this.setCacheSeconds(0);
		this.setInstanceCacheFlag(false);
	}

	/**
	 * execute
	 * 
	 * @param webInput
	 *            WebInput
	 * @return Model
	 * @throws ServletException
	 * @todo Implement this com.beetle.framework.web.controller.ControllerImp method
	 */
	public View perform(WebInput webInput) throws ControllerException {
		HttpServletRequest request = webInput.getRequest();
		return doupload(webInput, request);
	}

	/**
	 * use the overdue upload package
	 * 
	 * @param webInput
	 * @param request
	 * @return
	 * @throws ControllerException
	 */
	private View doupload(WebInput webInput, HttpServletRequest request)
			throws ControllerException {
		UploadForm fp = null;
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload sfu = new ServletFileUpload(factory);
		List<?> fileItems = null;
		try {
			IUpload upload = UploadFactory.getUploadInstance(webInput
					.getControllerName(), (String) webInput.getRequest()
					.getAttribute(CommonUtil.controllerimpclassname)); // 2007-03-21
			long sizeMax = webInput.getParameterAsLng("sizeMax");
			if (sizeMax == 0) {
				sfu.setSizeMax(IUpload.sizeMax);
			} else {
				sfu.setSizeMax(sizeMax);
			}
			int sizeThreshold = webInput.getParameterAsInt("sizeThreshold");
			if (sizeThreshold == 0) {
				factory.setSizeThreshold(IUpload.sizeThreshold);
			} else {
				factory.setSizeThreshold(sizeThreshold);
			}
			Map<String, String> fieldMap = new HashMap<String, String>();
			List<FileObj> fileList = new ArrayList<FileObj>();
			fileItems = sfu.parseRequest(request);
			Iterator<?> i = fileItems.iterator();
			while (i.hasNext()) {
				FileItem fi = (FileItem) i.next();
				if (fi.isFormField()) {
					fieldMap.put(fi.getFieldName(), fi.getString());
				} else {
					fileList.add(new FileObj(fi));
				}
			}
			fp = new UploadForm(fileList, fieldMap, request,
					webInput.getResponse());
			return upload.processUpload(fp);
		} catch (Exception ex) {
			throw new ControllerException(WebConst.WEB_EXCEPTION_CODE_UPLOAD,
					ex);
		} finally {
			if (fileItems != null) {
				fileItems.clear();
			}
			if (fp != null) {
				fp.clear();
			}
			sfu = null;
		}
	}
}
