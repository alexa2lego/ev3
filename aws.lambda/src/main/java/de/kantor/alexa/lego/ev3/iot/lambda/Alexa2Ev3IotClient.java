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
	private String ev3Topic;

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
		this.ev3Topic = System.getenv("aws_ev3_topic");
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
	 * @return object of {@link Ev3Thing}
	 * @throws Alexa2Ev3Exception
	 */
	public Ev3Thing getThingState() throws Alexa2Ev3Exception {
		try {

			if (iotClient.getConnectionStatus().equals(AWSIotConnectionStatus.DISCONNECTED)) {
				iotClient.connect();
			}

			String shadowState = device.get();

			Ev3Thing thingState = objectMapper.readValue(shadowState, Ev3Thing.class);
			LOG.info(String.format("IoT Device has shadowState: %s", shadowState));

			return thingState;
		} catch (Exception e) {
			throw new Alexa2Ev3Exception("sending request for current state failed", e);
		}
	}

	public void sendCommand(final Alexa2Ev3Command command) throws Alexa2Ev3Exception {
		try {
			if (iotClient.getConnectionStatus().equals(AWSIotConnectionStatus.DISCONNECTED)) {
				iotClient.connect();
			}
			Ev3Thing thing = new Ev3Thing();
			thing.state.desired.command.action = command.getAction().name();
			thing.state.desired.command.value = command.getValue();
			String jsonState = objectMapper.writeValueAsString(thing);
			device.update(jsonState);
			LOG.info(String.format("Command %s sent to device", command.toJson()));
		} catch (Exception e) {
			throw new Alexa2Ev3Exception("command can not be sent to device", e);
		}
	}

}
