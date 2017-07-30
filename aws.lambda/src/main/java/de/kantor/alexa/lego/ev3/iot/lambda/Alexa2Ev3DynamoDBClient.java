package de.kantor.alexa.lego.ev3.iot.lambda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

/**
 * {@link Alexa2Ev3DynamoDBClient} ...
 * 
 * @author kantor
 *
 */
public class Alexa2Ev3DynamoDBClient {

	private static final String EV3_DEVICES_TABLE = "ev3device";
	private static final Logger LOG = LoggerFactory.getLogger(Alexa2Ev3DynamoDBClient.class);
	private static Alexa2Ev3DynamoDBClient instance;

	private static AmazonDynamoDB amazonDynamoDbClient;

	private Alexa2Ev3DynamoDBClient(String clientEndpoint, String clientId, String awsAccessKeyId,
			String awsSecretAccessKey) {
		BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
		amazonDynamoDbClient = AmazonDynamoDBClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();
	}

	public static synchronized Alexa2Ev3DynamoDBClient getInstance() {
		if (instance != null) {
			throw new IllegalStateException("already initialized");
		}

		instance = new Alexa2Ev3DynamoDBClient(System.getenv("aws_iot_endpoint"), System.getenv("aws_iot_client"),
				System.getenv("aws_iot_accessKeyId"), System.getenv("aws_iot_secretAccessKey"));

		return instance;
	}

	public List<Ev3Device> findAllDevices() {

		List<Ev3Device> ev3Devices = new ArrayList<>();

		ScanResult result = null;

		do {
			ScanRequest req = new ScanRequest();
			req.setTableName(EV3_DEVICES_TABLE);

			if (result != null) {
				req.setExclusiveStartKey(result.getLastEvaluatedKey());
			}

			result = amazonDynamoDbClient.scan(req);

			List<Map<String, AttributeValue>> rows = result.getItems();

			for (Map<String, AttributeValue> map : rows) {
				try {
					String iotName = map.get("thing").getS();
					Map<String, AttributeValue> device = map.get("payload").getM().get("state").getM().get("reported")
							.getM().get("device").getM();

					List<AttributeValue> commands = device.get("commands").getL();
					Map<String, String> commandsMap = new HashMap<>();
					for (AttributeValue command : commands) {
						Entry<String, AttributeValue> entry = command.getM().entrySet().iterator().next();
						String actionword = entry.getKey();
						String commandTexts = entry.getValue().getS();
						List<String> commandArray = new ArrayList<>(Arrays.asList(commandTexts.split(",")));
						commandArray.stream()
								.forEach(c -> commandsMap.put(c.trim().toLowerCase(), actionword.toUpperCase()));

					}
					Ev3Device ev3Device = new Ev3Device(device.get("serialNumber").getS(), iotName,
							device.get("deviceName").getS().toLowerCase());
					ev3Device.setCommands(commandsMap);
					ev3Devices.add(ev3Device);

				} catch (NumberFormatException e) {
					LOG.error(e.getMessage());
				}
			}
		} while (result.getLastEvaluatedKey() != null);

		LOG.info("count of ev3Devices: " + ev3Devices.size());

		return ev3Devices;
	}

}
