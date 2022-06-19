package ir.limoo.driver.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JacksonUtils {

	private static final Logger logger = LoggerFactory.getLogger(JacksonUtils.class);

	private static ObjectMapper objectMapper = null;
	private static ObjectMapper forgivingObjectMapper = null;

	public static ObjectMapper getObjectMapper() {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
			objectMapper.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
			objectMapper.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
			objectMapper.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
			objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		}
		return objectMapper;
	}

	public static ObjectMapper getForgivingObjectMapper() {
		if (forgivingObjectMapper == null) {
			forgivingObjectMapper = new ObjectMapper();
			forgivingObjectMapper.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
			forgivingObjectMapper.configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false);
			forgivingObjectMapper.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
			forgivingObjectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			forgivingObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}
		return forgivingObjectMapper;
	}

	public static ObjectNode createEmptyObjectNode() {
		return getObjectMapper().createObjectNode();
	}

	public static JsonNode convertStringToJsonNode(String objectNodeStr) throws IOException {
		return getObjectMapper().readTree(objectNodeStr);
	}

	public static <T> T deserializeObject(JsonNode jsonNode, Class<T> clazz) throws JsonProcessingException {
		return getForgivingObjectMapper().treeToValue(jsonNode, clazz);
	}

	public static void deserializeIntoObject(JsonNode jsonNode, Object obj) throws IOException {
		getForgivingObjectMapper().readerForUpdating(obj).readValue(jsonNode);
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> deserializeObjectToList(JsonNode jsonNode, Class<T> clazz) {
		List<T> output = new ArrayList<>();
		if (jsonNode == null)
			return output;
		ObjectMapper mapper = getForgivingObjectMapper();
		try (JsonParser jsonParser = mapper.treeAsTokens(jsonNode)) {
			Class<?> arrayClazz = Class.forName("[L" + clazz.getName() + ";");
			T[] readValue = (T[]) mapper.readValue(jsonParser, arrayClazz);
			Collections.addAll(output, readValue);
			return output;
		} catch (IOException | ClassNotFoundException e) {
			logger.error("", e);
			return null;
		}
	}

	public static String serializeObjectAsString(Object obj) {
		if (obj == null)
			return null;
		ObjectMapper mapper = getObjectMapper();
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static JsonNode serializeObjectAsJsonNode(Object obj) {
		if (obj == null)
			return null;
		return getObjectMapper().valueToTree(obj);
	}
}
