package net.certiv.antlr.project.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reflect {

	public static final Class<?>[] emptyParams = new Class[] {};
	public static final Object[] emptyArgs = new Object[] {};

	private Reflect() {}

	public static void set(Object target, String fieldName, Object value) {
		try {
			Field f = target.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(target, value);
		} catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static void setSuper(Object target, String fieldName, Object value) {
		try {
			Field f = target.getClass().getSuperclass().getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(target, value);
		} catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static Object get(Object target, String fieldName) throws NoSuchFieldException {
		try {
			Field f = target.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			return f.get(target);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object getSuper(Object target, String fieldName) {
		try {
			Field f = target.getClass().getSuperclass().getDeclaredField(fieldName);
			f.setAccessible(true);
			return f.get(target);
		} catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object invoke(boolean quiet, Object target, String methodName) {
		return invoke(quiet, target, methodName, emptyParams, emptyArgs);
	}

	public static Object invoke(boolean quiet, Object target, String methodName, Object... args) {
		if (args == null) {
			return invoke(quiet, target, methodName, emptyParams, emptyArgs);
		}
		Class<?>[] params = new Class[args.length];
		for (int idx = 0; idx < args.length; idx++) {
			params[idx] = args[idx].getClass();
		}
		return invoke(quiet, target, methodName, params, args);
	}

	public static Object invoke(boolean quiet, Object target, String methodName, Class<?>[] params, Object[] args) {

		try {
			Method m = target.getClass().getMethod(methodName, params);
			m.setAccessible(true);
			return m.invoke(target, args);
		} catch (SecurityException | NoSuchMethodException | IllegalArgumentException
				| IllegalAccessException | InvocationTargetException e) {
			if (!quiet) e.printStackTrace();
		}
		return null;
	}

	public static Object invokeSuperDeclared(Object target, String methodName, Class<?>[] params, Object[] args) {

		try {
			Method m = target.getClass().getSuperclass().getDeclaredMethod(methodName, params);
			m.setAccessible(true);
			return m.invoke(target, args);
		} catch (SecurityException | NoSuchMethodException | IllegalArgumentException
				| IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<String, Class<?>> declaredClasses(Object target) {
		Map<String, Class<?>> classNames = new HashMap<>();
		for (Class<?> c : target.getClass().getDeclaredClasses()) {
			classNames.put(c.getSimpleName(), c);
		}
		return classNames;
	}

	public static Object make(Class<?> clazz, Object[] args) {
		Constructor<?> c = clazz.getDeclaredConstructors()[0];
		c.setAccessible(true);
		Object object = null;
		try {
			object = c.newInstance(args);
		} catch (SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return object;
	}

	public static String simpleClassName(Object arg) {
		String name = arg.getClass().getCanonicalName();
		if (name != null) {
			int mark = name.lastIndexOf('.');
			if (mark > 0) {
				return name.substring(mark + 1);
			}
		}
		return "<unknown>";
	}

	public static <T> T instantiate(final String className, final Class<T> type) {
		try {
			return type.cast(Class.forName(className).newInstance());
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<String> getMethodNames(Object target, String prefix) {
		List<String> results = new ArrayList<>();
		try {
			Method[] methods = target.getClass().getMethods();
			for (int idx = 0; idx < methods.length; idx++) {
				Method method = methods[idx];
				if (method.getName().startsWith(prefix)) {
					results.add(method.getName());
				}
			}
		} catch (SecurityException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		return results;
	}
}
