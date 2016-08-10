package org.openslx.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import org.apache.log4j.Logger;
import org.apache.thrift.TBase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

public class Json {

	private static final Logger LOGGER = Logger.getLogger(Json.class);

	/**
	 * Global static instance. The Gson object is thread-safe.
	 */
	private static final Gson gson = new Gson();

	private static final GsonBuilder gsonThriftBuilder = new GsonBuilder();

	public static <T> void registerThriftClass(Class<T> thriftClass) {
		if (!TBase.class.isAssignableFrom(thriftClass))
			throw new IllegalArgumentException(thriftClass.getName() + " is not a thrift struct.");
		gsonThriftBuilder.registerTypeAdapter(thriftClass, new ThriftDeserializer<T>(thriftClass));
	}

	/**
	 * Deserialize the given json string to an instance of T.
	 * This will deserialize all fields, except transient ones.
	 * 
	 * @param data JSON formatted data
	 * @param classOfData class to instantiate
	 * @return instanceof T
	 */
	public static <T> T deserialize(String data, Class<T> classOfData) {
		try {
			return gson.fromJson(data, classOfData);
		} catch (JsonSyntaxException e) {
			LOGGER.warn("Cannot deserialize to " + classOfData.getSimpleName(), e);
			return null;
		}
	}

	public static <T> T deserializeThrift(String data, Class<T> thriftClass) {
		try {
			return gsonThriftBuilder.create().fromJson(data, thriftClass);
		} catch (JsonSyntaxException e) {
			LOGGER.warn("Cannot deserialize to " + thriftClass.getSimpleName(), e);
			return null;
		}
	}

	/**
	 * Serialize the given POJO. All fields except transient ones will be
	 * serialized.
	 * 
	 * @param object some object to serialize
	 * @return JSON formatted represenatation of <code>object</code>
	 */
	public static String serialize(Object object) {
		return gson.toJson(object);
	}

	private static class ThriftDeserializer<T> implements JsonDeserializer<T> {
		private final Class<T> clazz;

		public ThriftDeserializer(Class<T> classOfData) {
			this.clazz = classOfData;
		}

		@Override
		public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			if (!(json instanceof JsonObject))
				throw new JsonParseException("Need a json object, have " + json.getClass().getSimpleName());
			JsonObject obj = (JsonObject) json;
			final T inst;
			try {
				inst = clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				LOGGER.warn("Could not deserialize to class " + clazz.getName(), e);
				throw new JsonParseException("Cannot instantiate class " + clazz.getSimpleName());
			}
			for (Field field : clazz.getFields()) {
				if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()))
					continue;
				final String methodName = "set" + field.getName().substring(0, 1).toUpperCase()
						+ field.getName().substring(1);
				final Method setter;
				try {
					setter = clazz.getMethod(methodName, field.getType());
				} catch (NoSuchMethodException e) {
					LOGGER.warn(clazz.getSimpleName() + " has no method " + methodName);
					continue;
				}
				JsonElement element = obj.get(field.getName());
				if (element == null || element.isJsonNull())
					continue;
				try {
					setter.invoke(inst, context.deserialize(element, field.getType()));
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					LOGGER.warn("Could not call " + methodName + " on " + clazz.getSimpleName(), e);
				}
			}
			return inst;
		}

	}

}
