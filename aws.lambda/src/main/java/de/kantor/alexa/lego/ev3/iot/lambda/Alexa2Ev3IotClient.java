package de.kantor.alexa.lego.ev3.iot.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.iot.client.AWSIotConnectionStatus;
import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link Alexa2Ev3IotClient} creates a IoT client that retrieves current state
 * of EV3 IoT
 * 
 * @author kantor
 *
 */
public class Alexa2Ev3IotClient {

	private static final Logger LOG = LoggerFactory.getLogger(Alexa2Ev3IotClient.class);
	private static Alexa2Ev3IotClient instance;
	private static AWSIotDevice device;
	private static ObjectMapper objectMapper;
	private static AWSIotMqttClient iotClient;

	private Alexa2Ev3IotClient(String clientEndpoint, String clientId, String awsAccessKeyId, String awsSecretAccessKey,
			String thingName) {
		iotClient = new AWSIotMqttClient(clientEndpoint, clientId, awsAccessKeyId, awsSecretAccessKey);
		iotClient.setKeepAliveInterval(5);

		device = new AWSIotDevice(thingName);
		try {
			iotClient.attach(device);
		} catch (AWSIotException e) {
			LOG.error("Device can not be attached", e);
		}
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static synchronized Alexa2Ev3IotClient getInstance() {
		if (instance != null) {
			throw new IllegalStateException("already initialized");
		}

		instance = new Alexa2Ev3IotClient(System.getenv("aws_iot_endpoint"), System.getenv("aws_iot_client"),
				System.getenv("aws_iot_accessKeyId"), System.getenv("aws_iot_secretAccessKey"),
				System.getenv("aws_iot_thing_name"));

		return instance;
	}

	/**
	 * Retrieves state from the EV3 thing shadow
	 * 
	 * @return object of {@link Ev3ThingState}
	 * @throws Alexa2Ev3Exception
	 */
	public Ev3ThingState getThingState() throws Alexa2Ev3Exception {
		try {

			if (iotClient.getConnectionStatus().equals(AWSIotConnectionStatus.DISCONNECTED)) {
				iotClient.connect();
			}

			String shadowState = device.get();

			Ev3ThingState thingState = objectMapper.readValue(shadowState, Ev3ThingState.class);
			LOG.info(String.format("IoT Device has shadowState: %s", shadowState));

			return thingState;
		} catch (Exception e) {
			throw new Alexa2Ev3Exception("sending request for current state failed", e);
		}
	}

}