package de.kantor.alexa.lego.ev3.iot.lambda;

import java.util.HashSet;
import java.util.Set;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

/**
 * 
 * 
 * This class {@link Alexa2EV3RequestHandler} is a handler for an AWS Lambda
 * function is called by Alexa2EV3 Skill.
 * 
 * @author kantor
 *
 */
public class Alexa2EV3SpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {

	private static final Set<String> supportedApplicationIds;

	static {
		/*
		 * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit"
		 * the relevant Alexa Skill and put the relevant Application Ids in this Set.
		 */
		supportedApplicationIds = new HashSet<>();
		supportedApplicationIds.add(System.getenv("alexa_app_id"));
	}

	public Alexa2EV3SpeechletRequestStreamHandler() {
		super(new Alexa2EV3Speechlet(), supportedApplicationIds);
	}
}