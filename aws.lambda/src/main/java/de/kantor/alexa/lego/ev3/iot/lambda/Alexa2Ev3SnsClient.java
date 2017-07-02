
package de.kantor.alexa.lego.ev3.iot.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

/**
 * {@link Alexa2Ev3SnsClient} creates a command messages and publishts it using
 * Amazon SNS topic.
 * 
 * @author kantor
 *
 */
public class Alexa2Ev3SnsClient {

	private static final Logger LOG = LoggerFactory.getLogger(Alexa2Ev3SnsClient.class);
	private static final AmazonSNS snsClient = AmazonSNSClientBuilder.standard().build();

	private String ev3Topic;

	public Alexa2Ev3SnsClient() {
		this.ev3Topic = System.getenv("aws_ev3_sns_arn");
	}

	public void publish(final Alexa2Ev3Command command) throws Alexa2Ev3Exception {
		try {
			PublishRequest publishRequest = new PublishRequest(ev3Topic, command.toJson());
			PublishResult publishResult = snsClient.publish(publishRequest);
			LOG.info(String.format("Command %s sent: %s", command.toJson(), publishResult.getMessageId()));
		} catch (Exception e) {
			throw new Alexa2Ev3Exception("command can not be published", e);
		}
	}
}
