package de.kantor.alexa.lego.ev3.iot.lambda;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Alexa2Ev3IotClientTest {
	private ObjectMapper objectMapper = new ObjectMapper();

	@Before
	public void setup() {
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Test
	public void test() throws URISyntaxException, UnsupportedEncodingException, IOException {

		Path path = Paths.get("src/test/resources/shadow.json");

		String json = java.nio.file.Files.lines(path).collect(Collectors.joining());
		assertNotNull(json);
		Ev3Device thingState = objectMapper.readValue(json, Ev3Device.class);
		assertNotNull(thingState);
	}

}
