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
package com.beetle.framework.resource.define;

import com.beetle.framework.util.ObjectUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseDTO extends HashMap<String, Object> {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public BaseDTO() {
	}

	/**
	 * 传输自定义的可序列化的对象
	 * 
	 * 
	 * @param key
	 * @param userDefinedObject
	 *            --必须可序列化，否则为null
	 */
	public void putDTO(String key, Serializable DTO) {
		if (DTO == null) {
			this.put(key, null);
		} else {
			byte[] bytes = ObjectUtil.objToBytes(DTO);
			if (bytes == null) {
				this.put(key, null);
			} else {
				this.put(key, bytes);
			}
		}
	}

	/**
	 * 获取传输的自定义对象
	 * 
	 * @param key
	 * @return
	 */
	public Object getValueAsDTO(String key) {
		Object o = this.get(key);
		if (o == null) {
			return null;
		} else {
			byte[] bytes = (byte[]) o;
			Object o2 = ObjectUtil.bytesToObj(bytes);
			o = null;
			return o2;
		}
	}

	/**
	 * 设置内含java基本类型数据的Map
	 * 
	 * @param key
	 * @param value
	 */

	public String getValueAsString(String key) {
		return (String) this.get(key);
	}

	public Integer getValueAsInteger(String key) {
		return (Integer) this.get(key);
	}

	public Long getValueAsLong(String key) {
		return (Long) this.get(key);
	}

	public Float getValueAsFloat(String key) {
		return (Float) this.get(key);
	}

	public Double getValueAsDouble(String key) {
		return (Double) this.get(key);
	}

	public Boolean getValueAsBoolean(String key) {
		return (Boolean) this.get(key);
	}

	public Character getValueAsCharacter(String key) {
		return (Character) this.get(key);
	}

	public Date getValueAsDate(String key) {
		return (Date) this.get(key);
	}

	public List<?> getValueAsList(String key) {
		return (List<?>) this.get(key);
	}

	public Map<?, ?> getValueAsMap(String key) {
		return (Map<?, ?>) this.get(key);
	}

	public Short getValueAsShort(String key) {
		return (Short) this.get(key);
	}

	public Byte getValueAsByte(String key) {
		return (Byte) this.get(key);
	}

	public Object getValueAsObject(String key) {
		return this.get(key);
	}

	@Override
	public Object put(String key, Object value) {
		return super.put(key, value);
	}

	@Override
	public Object get(Object key) {
		// TODO Auto-generated method stub
		return super.get(key);
	}

}
