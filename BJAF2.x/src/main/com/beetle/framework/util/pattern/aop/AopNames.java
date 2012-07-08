package com.beetle.framework.util.pattern.aop;

import com.google.inject.Binder;
import com.google.inject.Key;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

public class AopNames {
	private static class AopImpl implements Aop, Serializable {

		private final String value;

		public AopImpl(String value) {
			this.value = value;
		}

		public String id() {
			return this.value;
		}

		public int hashCode() {
			return (127 * "value".hashCode()) ^ value.hashCode();
		}

		public boolean equals(Object o) {
			if (!(o instanceof Aop)) {
				return false;
			}

			Aop other = (Aop) o;
			return value.equals(other.id());
		}

		public String toString() {
			return "@" + Aop.class.getName() + "(value=" + value + ")";
		}

		public Class<? extends Annotation> annotationType() {
			return Aop.class;
		}

		private static final long serialVersionUID = 0;
	}

	private AopNames() {
	}

	public static Aop named(String name) {
		return new AopImpl(name);
	}

	public static void bindProperties(Binder binder,
			Map<String, String> properties) {
		binder = binder.skipSources(AopNames.class);
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			binder.bind(Key.get(String.class, new AopImpl(key))).toInstance(
					value);
		}
	}

	public static void bindProperties(Binder binder, Properties properties) {
		binder = binder.skipSources(AopNames.class);

		// use enumeration to include the default properties
		for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements();) {
			String propertyName = (String) e.nextElement();
			String value = properties.getProperty(propertyName);
			binder.bind(Key.get(String.class, new AopImpl(propertyName)))
					.toInstance(value);
		}
	}
}
