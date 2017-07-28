package de.kantor.alexa.lego.ev3.iot.lambda;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.iot.client.AWSIotConnectionStatus;
import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
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
	private static final long PUBLISH_TIMEOUT = 3000;
	private static Alexa2Ev3IotClient instance;
	private Map<String, AWSIotDevice> iotDevices = new HashMap<>();
	private static ObjectMapper objectMapper;
	private Map<String, AWSIotMqttClient> iotClients = new HashMap<>();

	private Alexa2Ev3IotClient(String clientEndpoint, String clientId, String awsAccessKeyId, String awsSecretAccessKey,
			List<Ev3Device> ev3Devices) {
		for (Ev3Device ev3Device : ev3Devices) {
			AWSIotMqttClient iotClient = new AWSIotMqttClient(clientEndpoint, clientId, awsAccessKeyId,
					awsSecretAccessKey);
			iotClient.setKeepAliveInterval(5);

			AWSIotDevice iotDevice = new AWSIotDevice(ev3Device.getDeviceName());
			iotDevices.put(ev3Device.getAliasName(), iotDevice);

			try {
				iotClient.attach(iotDevice);
				LOG.info("Device " + ev3Device.getDeviceName() + " was attached");
				iotClients.put(ev3Device.getAliasName(), iotClient);
			} catch (AWSIotException e) {
				LOG.error("Device " + ev3Device.getDeviceName() + " can not be attached", e);
			}
		}
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static synchronized Alexa2Ev3IotClient getInstance(List<Ev3Device> ev3Devices) {
		if (instance != null) {
			throw new IllegalStateException("already initialized");
		}

		instance = new Alexa2Ev3IotClient(System.getenv("aws_iot_endpoint"), System.getenv("aws_iot_client"),
				System.getenv("aws_iot_accessKeyId"), System.getenv("aws_iot_secretAccessKey"), ev3Devices);

		return instance;
	}

	/**
	 * Retrieves state from the EV3 thing shadow
	 * 
	 * @param receiver
	 * @return object of {@link Ev3Device}
	 * @throws Alexa2Ev3Exception
	 */
	public Ev3Device getThingState(String receiver) throws Alexa2Ev3Exception {
		try {
			AWSIotMqttClient iotClient = iotClients.get(receiver);
			if (iotClient == null) {

				throw new Alexa2Ev3Exception("ioT-Client " + receiver + " can not be found");
			}
			if (iotClient.getConnectionStatus().equals(AWSIotConnectionStatus.DISCONNECTED)) {
				iotClient.connect();
			}
			AWSIotDevice iotDevice = iotDevices.get(receiver);
			if (iotDevice == null) {
				throw new Alexa2Ev3Exception("Device " + receiver + " can not be found");
			}
			String shadowState = iotDevice.get();

			Ev3Device thingState = objectMapper.readValue(shadowState, Ev3Device.class);
			// TODO
			LOG.info(String.format("IoT Device has shadowState: %s", shadowState));

			return thingState;
		} catch (Exception e) {
			throw new Alexa2Ev3Exception("sending request for current state failed", e);
		}
	}

	public void sendCommand(String receiver, final Alexa2Ev3Command command) throws Alexa2Ev3Exception {
		try {
			AWSIotMqttClient iotClient = iotClients.get(receiver);
			if (iotClient == null) {
				LOG.info("iotClients:" + iotClients);
				throw new Alexa2Ev3Exception("ioT-Client " + receiver + " can not be found");
			}

			if (iotClient.getConnectionStatus().equals(AWSIotConnectionStatus.DISCONNECTED)) {
				iotClient.connect();
			}

			Alexa2Ev3IotMessage message = new Alexa2Ev3IotMessage(receiver, AWSIotQos.QOS0, command.toJson());

			iotClient.publish(message, PUBLISH_TIMEOUT);
			LOG.info(String.format("Command %s sent to receiver %s", command.toJson(), receiver));
		} catch (Exception e) {
			throw new Alexa2Ev3Exception("command can not be sent to receiver", e);
		}
	}

}
