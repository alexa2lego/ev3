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
 * {@link Alexa2Ev3IotClient} creates an IoT client that sends command to EV3 devices
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

    private Map<String, String> deviceMap = new HashMap<>();

    private Alexa2Ev3IotClient(String clientEndpoint, String clientId, String awsAccessKeyId,
            String awsSecretAccessKey, List<Ev3Device> ev3Devices) {
        for (Ev3Device ev3Device : ev3Devices) {
            AWSIotMqttClient iotClient = new AWSIotMqttClient(clientEndpoint, clientId, awsAccessKeyId,
                    awsSecretAccessKey);
            iotClient.setKeepAliveInterval(5);

            AWSIotDevice iotDevice = new AWSIotDevice(ev3Device.getIotName());
            iotDevices.put(ev3Device.getDeviceName(), iotDevice);
            deviceMap.put(ev3Device.getDeviceName(), ev3Device.getIotName());
            try {
                iotClient.attach(iotDevice);
                LOG.info("Device " + ev3Device.getIotName() + " was attached");
                iotClients.put(ev3Device.getDeviceName(), iotClient);
            } catch (AWSIotException e) {
                LOG.error("Device " + ev3Device.getIotName() + " can not be attached", e);
            }
        }
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static synchronized Alexa2Ev3IotClient getInstance(List<Ev3Device> ev3Devices) {
        if (instance == null) {

            instance = new Alexa2Ev3IotClient(System.getenv("aws_iot_endpoint"), System.getenv("aws_iot_client"),
                    System.getenv("aws_iot_accessKeyId"), System.getenv("aws_iot_secretAccessKey"), ev3Devices);
        }
        return instance;
    }

    public void sendCommand(String deviceId, final Alexa2Ev3Command command) throws Alexa2Ev3Exception {
        try {
            AWSIotMqttClient iotClient = iotClients.get(deviceId);
            if (iotClient == null) {
                LOG.info("iotClients:" + iotClients);
                throw new Alexa2Ev3Exception("ioT-Client " + deviceId + " can not be found");
            }

            if (iotClient.getConnectionStatus().equals(AWSIotConnectionStatus.DISCONNECTED)) {
                LOG.info("iotClient " + iotClients + " reconnected");
                iotClient.connect();
            }

            Alexa2Ev3IotMessage message = new Alexa2Ev3IotMessage(deviceMap.get(deviceId), AWSIotQos.QOS0,
                    command.toJson());

            iotClient.publish(message, PUBLISH_TIMEOUT);
            LOG.info(String.format("Command %s sent to receiver %s", command.toJson(), deviceId));
        } catch (Exception e) {
            throw new Alexa2Ev3Exception("command can not be sent to receiver", e);
        }
    }

}
