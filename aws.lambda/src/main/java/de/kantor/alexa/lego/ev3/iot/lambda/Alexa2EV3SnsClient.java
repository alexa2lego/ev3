
package de.kantor.alexa.lego.ev3.iot.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

/**
 * {@link Alexa2EV3SnsClient} creates a command messages and publishts it using
 * Amazon SNS topic.
 * 
 * @author kantor
 *
 */
public class Alexa2EV3SnsClient {

	private static final Logger LOG = LoggerFactory.getLogger(Alexa2EV3SnsClient.class);
	private String ev3Topic;

	public Alexa2EV3SnsClient() {
		this.ev3Topic = System.getenv("aws_ev3_sns_arn");
	}

	private static final AmazonSNS snsClient = AmazonSNSClientBuilder.standard().build();

	public void sendCommand(String action, String value) throws Alexa2EV3Exception {
		try {
			String request = createNotification(action, value);
			PublishRequest publishRequest = new PublishRequest(ev3Topic, request);
			PublishResult publishResult = snsClient.publish(publishRequest);
			LOG.info(String.format("Command %s sent: %s", request, publishResult.getMessageId()));
		} catch (Exception e) {
			throw new Alexa2EV3Exception("Alexa2EV3SnsClient failed to successfully send the comman", e);
		}
	}

	private String createNotification(String action, String value) {
		Alexa2EV3Command command = new Alexa2EV3Command(action, value);
		return command.toJson();
	}
}
