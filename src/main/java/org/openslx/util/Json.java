package org.openslx.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.apache.thrift.TBase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

public class Json {

	private static final Logger LOGGER = Logger.getLogger(Json.class);

	/**
	 * Global static instance. The Gson object is thread-safe.
	 */
	private static final AtomicReference<Gson> gsonRef = new AtomicReference<>();

	private static final GsonBuilder gsonThriftBuilder = new GsonBuilder();
	
	public static <T extends TBase<?, ?>> void registerThriftClass(Class<T> thriftClass) {
		// Determine all relevant fields
		Field[] fieldArray = thriftClass.getFields();
		List<ThriftField> fields = new ArrayList<>(fieldArray.length);
		for ( Field field : fieldArray ) {
			if ( "__isset_bitfield".equals( field.getName() ) )
				continue;
			if ( Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())
					|| Modifier.isTransient( field.getModifiers() ))
				continue;
			String upperName = field.getName().substring(0, 1).toUpperCase()
					+ field.getName().substring(1);
			try {
				Method getter;
				try {
					getter = thriftClass.getMethod( "get" + upperName );
				} catch (NoSuchMethodException e) {
					getter = thriftClass.getMethod( "is" + upperName );
				}
				fields.add( new ThriftField( field,
						getter,
						thriftClass.getMethod( "set" + upperName, field.getType() ),
						thriftClass.getMethod( "isSet" + upperName) ) );
			} catch (NoSuchMethodException e) {
				LOGGER.warn( "Nein", e );
			}
		}
		synchronized ( Json.class ) {
			gsonThriftBuilder.registerTypeAdapter(thriftClass, new JsonThriftHandler<T>(thriftClass, fields));
			gsonRef.set( null );
		}
	}
	
	private static Gson getInstance()
	{
		Gson gson = gsonRef.get();
		if (gson == null) {
			synchronized ( Json.class ) {
   			gson = gsonThriftBuilder.create();
   			gsonRef.set( gson );
			}
		}
		return gson;
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
			return getInstance().fromJson(data, classOfData);
		} catch (JsonSyntaxException e) {
			LOGGER.warn("Cannot deserialize to " + classOfData.getSimpleName(), e);
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
		return getInstance().toJson(object);
	}

	private static class JsonThriftHandler<T> implements JsonDeserializer<T>, JsonSerializer<T> {
		private final Class<T> clazz;
		private final List<ThriftField> fields;

		public JsonThriftHandler(Class<T> classOfData, List<ThriftField> fields) {
			this.clazz = classOfData;
			this.fields = fields;
		}

		@Override
		public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			if (!(json instanceof JsonObject))
				throw new JsonParseException("Need a json object, have " + json.getClass().getSimpleName());
			// We're deserializing a json object {..} here
			JsonObject obj = (JsonObject) json;
			// Create the Thrift object we want to deserialize into
			final T inst;
			try {
				inst = clazz.newInstance();
			} catch (Exception e) {
				LOGGER.warn("Could not deserialize to class " + clazz.getName(), e);
				throw new JsonParseException("Cannot instantiate class " + clazz.getSimpleName());
			}
			// Iterate over all fields in the Thrift object
			for (ThriftField field : fields) {
				JsonElement element = obj.get(field.field.getName());
				if (element == null || element.isJsonNull())
					continue;
				try {
					field.setter.invoke(inst, context.deserialize(element, field.field.getType()));
				} catch (Exception e) {
					LOGGER.warn("Could not call " + field.setter.getName() + " on " + clazz.getSimpleName(), e);
				}
			}
			return inst;
		}

		@Override
		public JsonElement serialize( T thriftClass, Type typeOfT, JsonSerializationContext context )
		{
			JsonObject o = new JsonObject();
			for ( ThriftField thrift : fields ) {
				try {
					Object ret = thrift.isset.invoke( thriftClass );
					if ( !(ret instanceof Boolean) || !(Boolean)ret )
						continue;
					Object value = thrift.getter.invoke( thriftClass );
					if ( value == null )
						continue;
					JsonElement jo = null;
					if ( value instanceof Number ) {
						jo = new JsonPrimitive( (Number)value );
					} else if ( value instanceof String ) {
						jo = new JsonPrimitive( (String)value );
					} else if ( value instanceof Character ) {
						jo = new JsonPrimitive( (Character)value );
					} else if ( value instanceof Boolean ) {
						jo = new JsonPrimitive( (Boolean)value );
					} else {
						jo = context.serialize( value );
					}
					o.add( thrift.field.getName(), jo );
				} catch ( Exception e ) {
					LOGGER.warn( "Cannot serialize field " + thrift.field.getName() + " of thift class "
							+ thriftClass.getClass().getSimpleName(), e );
				}
			}
			return o;
		}

	}
	
	private static class ThriftField
	{
		public final Method getter, setter, isset;
		public final Field field;
		ThriftField(Field field, Method getter, Method setter, Method isset)
		{
			this.field = field;
			this.getter = getter;
			this.setter = setter;
			this.isset = isset;
		}
	}

}
