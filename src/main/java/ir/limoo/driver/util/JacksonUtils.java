package ir.limoo.driver.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JacksonUtils {

	private static ObjectMapper objectMapper = null;

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

	private static ObjectMapper forgivingObjectMapper = null;

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

	public static <T> T deserilizeObject(JsonNode jsonNode, Class<T> clazz) throws JsonProcessingException {
		return getForgivingObjectMapper().treeToValue(jsonNode, clazz);
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> deserilizeObjectToList(JsonNode jsonNode, Class<T> clazz) {
		List<T> output = new ArrayList<>();
		if (jsonNode == null)
			return output;
		ObjectMapper mapper = getForgivingObjectMapper();
		try (JsonParser jsonParser = mapper.treeAsTokens(jsonNode)) {
			Class<?> arrayClazz = Class.forName("[L" + clazz.getName() + ";");
			T[] readValue = (T[]) mapper.readValue(jsonParser, arrayClazz);
			for (T t : readValue) {
				output.add(t);
			}
			return output;
		} catch (IOException | ClassNotFoundException e) {
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
}
