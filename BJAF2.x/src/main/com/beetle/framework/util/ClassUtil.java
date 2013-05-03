package com.beetle.framework.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;

public final class ClassUtil {

	public static ClassLoader getClassLoader() {
		ClassLoader classLoader = null;
		try {
			classLoader = Thread.currentThread().getContextClassLoader();
		} catch (Exception t1) {
			try {
				classLoader = ClassUtil.class.getClassLoader();
			} catch (Exception t2) {
				t2.printStackTrace();
			}
		}
		return classLoader;
	}

	public static Class<?> loadClass(final String className) {
		Class<?> clazz;
		try {
			clazz = getClassLoader().loadClass(className);
		} catch (Exception t1) {
			try {
				clazz = Class.forName(className);
			} catch (Exception t2) {
				t2.printStackTrace();
				return null;
			}
		}
		return clazz;
	}

	/**
	 * 找出某个接口或抽象类在某个jar中所有的实现类
	 * 
	 * @param clazz
	 * @param jarfile
	 * @return
	 * @throws IOException
	 */
	public static Class<?>[] findImpClass(Class<?> clazz, String jarfile,
			ClassLoader loader) throws IOException {
		JarFile jarFile = new JarFile(jarfile);
		Enumeration<?> ee = jarFile.entries();
		ArrayList<Class<?>> result = new ArrayList<Class<?>>();
		try {
			while (ee.hasMoreElements()) {
				String entry = ee.nextElement().toString();
				if (entry.endsWith(".class") && entry.indexOf('$') == -1) {
					String clazzName = entry.replace('/', '.');
					clazzName = clazzName.substring(0, clazzName.length() - 6);
					Class<?> testClass = loadclass(loader, clazzName);
					if (testClass == null) {
						continue;
					}
					if (clazz.isInterface()) {
						if (hasInterface(testClass, clazz))
							result.add(testClass);
					} else {
						if (isSubClassOf(testClass, clazz))
							result.add(testClass);

					}
				}
			}
			Class<?>[] cls = (Class[]) result.toArray(new Class[result.size()]);
			return cls;
		} finally {
			result.clear();
			jarFile.close();
		}
	}

	private static Class<?> loadclass(ClassLoader loader, String clazzName) {
		Class<?> testClass = null;
		try {
			testClass = loader.loadClass(clazzName);
		} catch (Error e) {
			// e.printStackTrace();
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return testClass;
	}

	/**
	 * 根据包路径返回其所有的类
	 * 
	 * @param packageName
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static Class[] getClasses(String packageName)
			throws ClassNotFoundException, IOException {
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = ClassUtil.class.getClassLoader()
				.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class> classes = new ArrayList<Class>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	private static List<Class> findClasses(File directory, String packageName)
			throws ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				classes.addAll(findClasses(file,
						packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(Class.forName(packageName
						+ '.'
						+ file.getName().substring(0,
								file.getName().length() - 6)));
			}
		}
		return classes;
	}

	/**
	 * 找出某个接口或抽象类在某个jar中所有的实现类
	 * 
	 * @param clazz
	 * @param jarfile
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Class[] findImpClass(Class clazz, String jarfile)
			throws IOException {
		JarFile jarFile = new JarFile(jarfile);
		ArrayList<Class> result = new ArrayList<Class>();
		try {
			Enumeration ee = jarFile.entries();
			while (ee.hasMoreElements()) {
				String entry = ee.nextElement().toString();
				if (entry.endsWith(".class") && entry.indexOf('$') == -1) {
					String clazzName = entry.replace('/', '.');
					clazzName = clazzName.substring(0, clazzName.length() - 6);
					Class testClass = null;
					try {
						testClass = Class.forName(clazzName);
					} catch (Error e) {

					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					if (testClass == null) {
						continue;
					}
					if (clazz.isInterface()) {
						if (hasInterface(testClass, clazz)) {
							result.add(testClass);
						}
					} else {
						if (isSubClassOf(testClass, clazz)) {
							result.add(testClass);
						}
					}
				}
			}
			Class[] cls = result.toArray(new Class[result.size()]);
			return cls;
		} finally {
			result.clear();
			jarFile.close();
		}
	}

	@SuppressWarnings({ "rawtypes" })
	public static boolean isSubClassOf(Class clazz, Class superClass) {
		return clazz.getSuperclass().equals(superClass);
	}

	public static boolean isRootSubClassOf(Class<?> clazz, Class<?> superClass) {
		// return clazz.getSuperclass().equals(superClass);
		Class<?> c = clazz.getSuperclass();
		if (c.equals(superClass)) {
			return true;
		} else if (c.equals(Object.class)) {
			return false;
		}
		return isRootSubClassOf(c, superClass);
	}

	@SuppressWarnings({ "rawtypes" })
	private static boolean hasInterface(Class clazz, Class theInterface) {
		Class cc[] = clazz.getInterfaces();
		for (int i = 0; i < cc.length; i++) {
			if (cc[i].equals(theInterface)) {
				return true;
			}
		}
		return false;
	}

	public static <T> T newInstance(Class<T> clazz) {
		if (!clazz.isInterface()) {
			try {
				return clazz.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes" })
	public static Object newInstance(final String className,
			final Class[] constrParamTypes, final Object[] constrParamValues)
			throws Exception {
		Object instance;
		try {
			instance = loadClass(className).getConstructor(constrParamTypes)
					.newInstance(constrParamValues);

			if (instance == null) {
				return null;
			}
		} catch (InvocationTargetException ite) {
			Throwable t = ite.getTargetException();

			if (t instanceof Exception) {
				throw (Exception) t;
			}

			throw ite;
		} catch (Exception t) {
			throw t;
		}
		return instance;
	}

	@SuppressWarnings({ "rawtypes" })
	public static Object invoke(final Object object, final String methodName,
			final Class[] methodParamTypes, final Object[] methodParamValues)
			throws Exception {
		Object result = null;
		try {
			result = object.getClass().getMethod(methodName, methodParamTypes)
					.invoke(object, methodParamValues);
		} catch (InvocationTargetException ite) {
			Throwable t = ite.getTargetException();
			if (t instanceof Exception) {
				throw (Exception) t;
			}
			throw ite;
		} catch (Exception t) {
			t.printStackTrace();
		}
		return result;
	}

	public static Method getMethod(Object o, String methodName) {
		if ((methodName == null) || (o == null)) {
			return null;
		}
		Method[] ms = o.getClass().getMethods();
		for (int i = 0; i < ms.length; i++) {
			Method m = ms[i];
			if (m.getName().equals(methodName)) {
				return m;
			}
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes" })
	public static Object invoke(Object obj, String method, Object[] params,
			Class[] param_types) throws IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		Method m = obj.getClass().getMethod(method, param_types);
		return m.invoke(obj, params);
	}

	@SuppressWarnings({ "rawtypes" })
	public static Object invoke(Object obj, String method, Object[] params)
			throws IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {
		Method[] all_methods = obj.getClass().getMethods();
		Method method2invoke = null;
		// find method with the same name and matching argument types
		for (int i = 0; i < all_methods.length; i++) {
			Method m = all_methods[i];
			if (m.getName().equals(method)) {
				Class[] pt = m.getParameterTypes();
				boolean match = true;
				int match_loops = pt.length;
				if (match_loops != params.length) {
					continue;
				}
				for (int j = 0; j < match_loops; j++) {
					if (pt[i].isInstance(params[i]) == false) {
						match = false;
						break;
					}
				}
				if (match == true) {
					method2invoke = m;
				}
			}
		}

		// throw an exception if no method to invoke
		if (method2invoke == null) {
			String t = "(";
			for (int i = 0; i < params.length; i++) {
				if (i != 0) {
					t += ", ";
				}
				t += params[i].getClass().getName();
			}
			t += ")";
			throw new NoSuchMethodException(obj.getClass().getName() + "."
					+ method + t);
		}

		// finally, invoke founded method
		return method2invoke.invoke(obj, params);
	}

	@SuppressWarnings({ "rawtypes" })
	public static boolean isContainMethod(Class c, String methodName) {
		boolean bl = false;
		Method ms[] = c.getDeclaredMethods();
		for (int i = 0; i < ms.length; i++) {
			if (ms[i].getName().equals(methodName)) {
				bl = true;
				break;
			}
		}
		return bl;
	}

	@SuppressWarnings({ "rawtypes" })
	public static boolean isThreadSafe(Class c) {
		boolean bl = true;
		Field[] fs = c.getDeclaredFields();
		for (int i = 0; i < fs.length; i++) {
			int r = fs[i].getModifiers();
			if (r >= 0 && r <= 2) {
				bl = false;
				break;
			}
		}
		return bl;
	}

	public static <T> T newInstance(Constructor<T> ctor, Object... args)
			throws IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		makeAccessible(ctor);
		return ctor.newInstance(args);
	}

	public static Method findMethod(Class<?> clazz, String methodName,
			Class<?>... paramTypes) {
		try {
			return clazz.getMethod(methodName, paramTypes);
		} catch (NoSuchMethodException e) {
			return findDeclaredMethod(clazz, methodName, paramTypes);
		}
	}

	public static Method findDeclaredMethod(Class<?> clazz, String methodName,
			Class<?>[] paramTypes) {
		try {
			return clazz.getDeclaredMethod(methodName, paramTypes);
		} catch (NoSuchMethodException ex) {
			if (clazz.getSuperclass() != null) {
				return findDeclaredMethod(clazz.getSuperclass(), methodName,
						paramTypes);
			}
			return null;
		}
	}



	public static void makeAccessible(Constructor<?> ctor) {
		if ((!Modifier.isPublic(ctor.getModifiers()) || !Modifier.isPublic(ctor
				.getDeclaringClass().getModifiers())) && !ctor.isAccessible()) {
			ctor.setAccessible(true);
		}
	}

}
