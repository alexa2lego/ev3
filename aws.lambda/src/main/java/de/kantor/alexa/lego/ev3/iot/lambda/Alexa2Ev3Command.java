package de.kantor.alexa.lego.ev3.iot.lambda;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * class represents a command for EV3 robot
 * 
 * @author kantor
 *
 */
public class Alexa2Ev3Command {
	private static final ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

	}

	private String action;

	private String value;

	/**
	 * constructor
	 * 
	 * @param action
	 * @param value
	 */
	public Alexa2Ev3Command(@JsonProperty("action") String action, @JsonProperty("value") String value) {
		this.action = action;
		this.value = value;
	}

	public String getAction() {
		return action;
	}

	public String getValue() {
		return value;
	}

	public String toJson() throws JsonProcessingException {
		return objectMapper.writeValueAsString(this);
	}

	public static Alexa2Ev3Command fromJson(String json) throws IOException {
		return objectMapper.readValue(json, Alexa2Ev3Command.class);
	}

	@Override
	public String toString() {
		return "[action=" + action + ", value=" + value + "]";
	}

}
