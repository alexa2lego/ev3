package de.kantor.alexa.lego.ev3.iot.lambda;

import java.text.DecimalFormat;

import org.eclipse.paho.client.mqttv3.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.StandardCard;

/**
 * This class {@link Alexa2EV3Speechlet} implements {@link SpeechletV2} ....
 * 
 * @author kantor
 *
 */
public class Alexa2EV3Speechlet implements SpeechletV2 {

	private static final String STOP_INTENT = "StopIntent";

	private static final String ARM_COMMAND_INTENT = "ArmCommandIntent";

	private static final String ARM_STATE_REQUEST_INTENT = "ArmStateRequestIntent";

	private static final Logger LOG = LoggerFactory.getLogger(Alexa2EV3Speechlet.class);

	/**
	 * This is the default title that this skill will be using for cards.
	 */
	private static final String ALEXA2EV3_CARD_TITLE = "Alexa2Lego Skill";

	private static final String WELCOME_TEXT = "Willkommen beim Alexa2EV3 Skill. Sag mir dein Kommando.";

	private static final String GOODBYE_TEXT = "Auf Wiedersehen!";

	private static final String HELP_TEXT = "Du kannst sagen zum Beispiel. Geradeaus, zwei. Rückwärts, fünf. Links. Rechts.";

	private static final String UNHANDLED_TEXT = "Ich habe Dein Kommando nicht verstanden. Bitte versuche noch ein mal.";

	private static final String ERROR_TEXT = "Es ist ein Fehler aufgetreten. Versuche noch mal, bitte.";

	private static final String MOVE_RESPONSE_TEXT = "okay!";

	private static final String SLOT_ACTION = "Action";

	private static final String SLOT_VALUE = "Value";

	private static final String SLOT_STATE_PARAMETER = "Parameter";

	private static final String STATE_REPROMT_TEXT = "Du kannst Status abfragen, indem Du zum Beispiel sagst. Status Batterien!";

	private Alexa2EV3SnsClient snsClient;

	private Alexa2EV3IotClient iotClient;

	public Alexa2EV3Speechlet() {

		iotClient = Alexa2EV3IotClient.getInstance();
		snsClient = new Alexa2EV3SnsClient();
	}

	/**
	 * This is fired when a session is started. Here we could potentially initialize
	 * a session in our own service for the user, or update a record in a table,
	 * etc.
	 * 
	 * @param speechletRequestEnvelope
	 *            container for the speechlet request.
	 */
	@Override
	public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> speechletRequestEnvelope) {
		SessionStartedRequest sessionStartedRequest = speechletRequestEnvelope.getRequest();
		Session session = speechletRequestEnvelope.getSession();

		LOG.info("onSessionStarted requestId={}, sessionId={}", sessionStartedRequest.getRequestId(),
				session.getSessionId());

	}

	/**
	 * When our skill session is started, a launch event will be triggered. In the
	 * case of this sample skill, we will return a welcome message, however the sky
	 * is the limit.
	 * 
	 * @param speechletRequestEnvelope
	 *            container for the speechlet request.
	 * @return SpeechletResponse our welcome message
	 */
	@Override
	public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> speechletRequestEnvelope) {
		LaunchRequest launchRequest = speechletRequestEnvelope.getRequest();
		Session session = speechletRequestEnvelope.getSession();
		LOG.info("onLaunch requestId={}, sessionId={}", launchRequest.getRequestId(), session.getSessionId());

		return getAskResponse(ALEXA2EV3_CARD_TITLE, WELCOME_TEXT);
	}

	/**
	 * When we receive an intent MoveIntent or StopIntent, this will be triggered.
	 * This function will handle the processing of that intent based on the
	 * intentName.
	 * 
	 * @param speechletRequestEnvelope
	 *            container for the speechlet request.
	 * @return SpeechletResponse a message of our address or an error message
	 */
	@Override
	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> speechletRequestEnvelope) {
		SpeechletResponse response = null;
		IntentRequest intentRequest = speechletRequestEnvelope.getRequest();
		Session session = speechletRequestEnvelope.getSession();

		LOG.info("onIntent requestId={}, sessionId={}", intentRequest.getRequestId(), session.getSessionId());

		Intent intent = intentRequest.getIntent();
		String intentName = getIntentName(intent);

		LOG.info("Intent received: {}", intentName);
		if (intentName != null) {
			switch (intentName) {
			case ARM_COMMAND_INTENT:
				try {
					response = handleArmCommandIntent(intent, session);
				} catch (Alexa2EV3Exception e) {
					LOG.error(e.getMessage(), e);
					response = getAskResponse(ALEXA2EV3_CARD_TITLE, ERROR_TEXT + e.getCause());
				}
				break;
			case ARM_STATE_REQUEST_INTENT:
				try {
					response = handleArmStateRequestIntent(intent, session);
				} catch (Alexa2EV3Exception e) {
					LOG.error(e.getMessage(), e);
					response = getAskResponse(ALEXA2EV3_CARD_TITLE, ERROR_TEXT);
				}
				break;
			case STOP_INTENT:
				response = handleStopIntent(session);
				break;
			case "AMAZON.HelpIntent":
				response = getAskResponse(ALEXA2EV3_CARD_TITLE, HELP_TEXT);
				break;
			default:
				response = getAskResponse(ALEXA2EV3_CARD_TITLE, UNHANDLED_TEXT);
			}
		}
		return response;
	}

	private SpeechletResponse handleArmCommandIntent(Intent intent, Session session) throws Alexa2EV3Exception {
		Slot actionSlot = intent.getSlot(SLOT_ACTION);
		String value = "";
		if (actionSlot != null) {
			Alexa2EV3Action action = Alexa2EV3Action.getAction(actionSlot.getValue());
			if (action != null) {
				Slot valueSlot = intent.getSlot(SLOT_VALUE);
				value = valueSlot != null ? valueSlot.getValue() : "";
				snsClient.sendCommand(action.getActionEn(), value);
				return getConfirmResponse(action.getActionDe(), value);
			}
		}
		return getAskResponse(ALEXA2EV3_CARD_TITLE, UNHANDLED_TEXT);
	}

	private SpeechletResponse handleArmStateRequestIntent(Intent intent, Session session) throws Alexa2EV3Exception {
		Slot parameterSlot = intent.getSlot(SLOT_STATE_PARAMETER);
		if (parameterSlot != null) {

			Ev3ArmThingState thing = iotClient.sendRequest();

			return getStateDeviceResponse(thing, parameterSlot.getValue());
		}
		return getAskResponse(ALEXA2EV3_CARD_TITLE, UNHANDLED_TEXT);
	}

	private SpeechletResponse handleStopIntent(Session session) {
		PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
		outputSpeech.setText(GOODBYE_TEXT);

		return SpeechletResponse.newTellResponse(outputSpeech);
	}

	/**
	 * Similar to onSessionStarted, this method will be fired when the skill session
	 * has been closed.
	 * 
	 * @param speechletRequestEnvelope
	 *            container for the speechlet request.
	 */
	@Override
	public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> speechletRequestEnvelope) {
		SessionEndedRequest sessionEndedRequest = speechletRequestEnvelope.getRequest();
		Session session = speechletRequestEnvelope.getSession();

		LOG.info("onSessionEnded requestId={}, sessionId={}", sessionEndedRequest.getRequestId(),
				session.getSessionId());

	}

	/**
	 * Helper method for retrieving an Ask response with a simple card and reprompt
	 * included.
	 * 
	 * @param cardTitle
	 *            Title of the card that you want displayed.
	 * @param speechText
	 *            speech text that will be spoken to the user.
	 * @return the resulting card and speech text.
	 */
	private SpeechletResponse getAskResponse(String cardTitle, String speechText) {
		StandardCard card = getStandardCard(cardTitle, speechText);
		PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);
		Reprompt reprompt = getReprompt(speech);

		return SpeechletResponse.newAskResponse(speech, reprompt, card);
	}

	/**
	 * Creates a {@code SpeechletResponse} for the Move intent.
	 * 
	 * @param action
	 * @param value
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getConfirmResponse(String action, String value) {
		String speechText = MOVE_RESPONSE_TEXT;

		PlainTextOutputSpeech repromtSpeech = getPlainTextOutputSpeech(speechText);
		Reprompt reprompt = getReprompt(repromtSpeech);
		StandardCard card = getStandardCard(ALEXA2EV3_CARD_TITLE, action + (value != null ? " (" + value + ")" : ""));

		PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

		return SpeechletResponse.newAskResponse(speech, reprompt, card);
	}

	/**
	 * Creates a {@code SpeechletResponse} for the State Request intent.
	 * 
	 * @param stateDevice
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getStateDeviceResponse(Ev3ArmThingState thingState, String parameter) {
		String repromtText = STATE_REPROMT_TEXT;

		PlainTextOutputSpeech repromtSpeech = getPlainTextOutputSpeech(repromtText);
		Reprompt reprompt = getReprompt(repromtSpeech);
		String cardText = "";
		String speachText = "";
		if (thingState != null) {
			switch (parameter) {
			case "batterie":

				String voltage = thingState.state.reported.battery.get("voltageVolts");
				if (!Strings.isEmpty(voltage)) {
					DecimalFormat f = new DecimalFormat("#0.00");
					String voltageValue = f.format(Double.valueOf(voltage));
					voltageValue = voltageValue.replace(".", " Komma ");
					speachText = String.format("Die Spannung beträgt %s Volt.", voltageValue);
					cardText = thingState.state.reported.battery.toString();
				} else {
					speachText = "Die Spannung konnte nicht ermittelt werden.";
					cardText = speachText + " :-(";
				}
				break;

			default:
				break;
			}
		}

		StandardCard card = getStandardCard(ALEXA2EV3_CARD_TITLE, cardText);

		PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speachText);

		return SpeechletResponse.newAskResponse(speech, reprompt, card);
	}

	/**
	 * Helper method that creates a card object.
	 * 
	 * @param title
	 *            title of the card
	 * @param content
	 *            body of the card
	 * @return StandardCard the display card to be sent along with the voice
	 *         response.
	 */
	private StandardCard getStandardCard(String title, String content) {
		StandardCard card = new StandardCard();
		card.setTitle(title);
		card.setText(content);

		return card;
	}

	/**
	 * Helper method that will get the intent name from a provided Intent object. If
	 * a name does not exist then this method will return null.
	 * 
	 * @param intent
	 *            intent object provided from a skill request.
	 * @return intent name or null.
	 */
	private String getIntentName(Intent intent) {
		return (intent != null) ? intent.getName() : null;
	}

	/**
	 * Helper method for retrieving an OutputSpeech object when given a string of
	 * TTS.
	 * 
	 * @param speechText
	 *            the text that should be spoken out to the user.
	 * @return an instance of SpeechOutput.
	 */
	private PlainTextOutputSpeech getPlainTextOutputSpeech(String speechText) {
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(speechText);

		return speech;
	}

	/**
	 * Helper method that returns a reprompt object. This is used in Ask responses
	 * where you want the user to be able to respond to your speech.
	 * 
	 * @param outputSpeech
	 *            The OutputSpeech object that will be said once and repeated if
	 *            necessary.
	 * @return Reprompt instance.
	 */
	private Reprompt getReprompt(OutputSpeech outputSpeech) {
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(outputSpeech);

		return reprompt;
	}

}
