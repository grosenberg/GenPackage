package net.certiv.antlr.project.base;

import java.io.StringWriter;
import java.lang.reflect.Type;
import java.text.DateFormat;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

public class GsonGen {

	private GsonBuilder builder;
	private Gson gson;
	private String indent = "  ";
	private JsonWriter jwriter;
	private StringWriter writer;

	public GsonGen() {
		super();
		this.builder = new GsonBuilder();
	}

	/**
	 * Configures builder for pretty printing with 4-space tabstops
	 */
	public void configDefaultBuilder() {
		configBasicBuilder();
		this.builder.setPrettyPrinting();
		setIndent("    ");
	}

	/**
	 * Configures compact builder with 2-space tabstops
	 */
	public void configBasicBuilder() {
		this.builder.enableComplexMapKeySerialization()
				.disableHtmlEscaping()
				.serializeNulls()
				.setDateFormat(DateFormat.LONG)
				.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
				.setVersion(1.0);
		setIndent(indent);
	}

	// ------------------------------------------------------------------------------
	// Gson delegates ---------------------------------------------------------------

	public void create() {
		this.gson = builder.create();
		if (this.jwriter == null) createJsonWriter();
	}

	public String toJson(Object src) {
		if (gson == null) create();
		gson.toJson(src, src.getClass(), jwriter);
		return writer.toString();
	}

	public <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
		return gson.fromJson(json, classOfT);
	}

	public <T> TypeAdapter<T> getAdapter(TypeToken<T> type) {
		return gson.getAdapter(type);
	}

	public <T> TypeAdapter<T> getDelegateAdapter(TypeAdapterFactory skipPast, TypeToken<T> type) {
		return gson.getDelegateAdapter(skipPast, type);
	}

	public <T> TypeAdapter<T> getAdapter(Class<T> type) {
		return gson.getAdapter(type);
	}

	// ------------------------------------------------------------------------------
	// JsonWriter delegates ---------------------------------------------------------

	public String getIndent() {
		return this.indent;
	}

	public void setIndent(String indent) {
		this.indent = indent;
		createJsonWriter();
		this.jwriter.setIndent(indent);
	}

	private void createJsonWriter() {
		if (this.jwriter == null) {
			this.writer = new StringWriter();
			this.jwriter = new JsonWriter(writer);
		}
	}

	// ------------------------------------------------------------------------------
	// Builder delegates ------------------------------------------------------------

	public GsonGen setVersion(double ignoreVersionsAfter) {
		builder.setVersion(ignoreVersionsAfter);
		return this;
	}

	public GsonGen excludeFieldsWithModifiers(int... modifiers) {
		builder.excludeFieldsWithModifiers(modifiers);
		return this;
	}

	public GsonGen generateNonExecutableJson() {
		builder.generateNonExecutableJson();
		return this;
	}

	public GsonGen excludeFieldsWithoutExposeAnnotation() {
		builder.excludeFieldsWithoutExposeAnnotation();
		return this;
	}

	public GsonGen serializeNulls() {
		builder.serializeNulls();
		return this;
	}

	public GsonGen enableComplexMapKeySerialization() {
		builder.enableComplexMapKeySerialization();
		return this;
	}

	public GsonGen disableInnerClassSerialization() {
		builder.disableInnerClassSerialization();
		return this;
	}

	public GsonGen setLongSerializationPolicy(LongSerializationPolicy serializationPolicy) {
		builder.setLongSerializationPolicy(serializationPolicy);
		return this;
	}

	public GsonGen setFieldNamingPolicy(FieldNamingPolicy namingConvention) {
		builder.setFieldNamingPolicy(namingConvention);
		return this;
	}

	public GsonGen setFieldNamingStrategy(FieldNamingStrategy fieldNamingStrategy) {
		builder.setFieldNamingStrategy(fieldNamingStrategy);
		return this;
	}

	public GsonGen setExclusionStrategies(ExclusionStrategy... strategies) {
		builder.setExclusionStrategies(strategies);
		return this;
	}

	public GsonGen addSerializationExclusionStrategy(ExclusionStrategy strategy) {
		builder.addSerializationExclusionStrategy(strategy);
		return this;
	}

	public GsonGen addDeserializationExclusionStrategy(ExclusionStrategy strategy) {
		builder.addDeserializationExclusionStrategy(strategy);
		return this;
	}

	public GsonGen setPrettyPrinting() {
		builder.setPrettyPrinting();
		return this;
	}

	public GsonGen disableHtmlEscaping() {
		builder.disableHtmlEscaping();
		return this;
	}

	public GsonGen setDateFormat(String pattern) {
		builder.setDateFormat(pattern);
		return this;
	}

	public GsonGen setDateFormat(int style) {
		builder.setDateFormat(style);
		return this;
	}

	public GsonGen setDateFormat(int dateStyle, int timeStyle) {
		builder.setDateFormat(dateStyle, timeStyle);
		return this;
	}

	public GsonGen registerTypeAdapter(Type type, Object typeAdapter) {
		builder.registerTypeAdapter(type, typeAdapter);
		return this;
	}

	public GsonGen registerTypeAdapterFactory(TypeAdapterFactory factory) {
		builder.registerTypeAdapterFactory(factory);
		return this;
	}

	public GsonGen registerTypeHierarchyAdapter(Class<?> baseType, Object typeAdapter) {
		builder.registerTypeHierarchyAdapter(baseType, typeAdapter);
		return this;
	}

	public GsonGen serializeSpecialFloatingPointValues() {
		builder.serializeSpecialFloatingPointValues();
		return this;
	}

	public String toString() {
		return builder.toString();
	}
}
