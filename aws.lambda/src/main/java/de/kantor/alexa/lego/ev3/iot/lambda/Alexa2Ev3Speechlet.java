package de.kantor.alexa.lego.ev3.iot.lambda;

import java.io.IOException;
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
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * This class {@link Alexa2Ev3Speechlet} implements {@link SpeechletV2} ....
 * 
 * @author kantor
 *
 */
public class Alexa2Ev3Speechlet implements SpeechletV2 {

	private static final Logger LOG = LoggerFactory.getLogger(Alexa2Ev3Speechlet.class);

	private static final String CRANE_COMMAND_INTENT = "CraneCommandIntent";

	private static final String BRICK_STATE_REQUEST_INTENT = "BrickStateRequestIntent";

	private static final String LAST_COMMAND = "lastCommand";

	private static final String ALEXA2EV3_CARD_TITLE = "Alexa2Lego Skill";

	private static final String WELCOME_TEXT = "Willkommen. Sag mir dein Kommando.";

	private static final String GOODBYE_TEXT = "Auf Wiedersehen!";

	private static final String HELP_TEXT = "Du kannst sagen zum Beispiel. Links zwanzig. Rechts. Greifen. Status von Batterie.";

	private static final String UNHANDLED_TEXT = "Ich habe Dein Kommando nicht verstanden. Bitte versuche noch ein mal.";

	private static final String ERROR_TEXT = "Es ist ein Fehler aufgetreten. Versuche noch mal, bitte.";

	private static final String SAY_AGAIN_TEXT = "Sag bitte noch mal";

	private static final String COMMAND_CONFIRMATION_TEXT = "okay: %s";

	private static final String SLOT_ACTION = "Action";

	private static final String SLOT_VALUE = "Value";

	private static final String SLOT_STATE_PARAMETER = "Parameter";

	private static final String STATE_REPROMT_TEXT = "Du kannst Status abfragen, indem Du zum Beispiel sagst. Status Batterie.";

	private static final String LAST_INTENT_FAIL_TEXT = "Ich habe noch kein Komando, das ich weiderholen kann";

	private Alexa2Ev3SnsClient snsClient;

	private Alexa2Ev3IotClient iotClient;

	public Alexa2Ev3Speechlet() {

		iotClient = Alexa2Ev3IotClient.getInstance();
		snsClient = new Alexa2Ev3SnsClient();
	}

	/**
	 * This is fired when a session is started. Here we could potentially initialize
	 * a session in our own service for the user, or update a record in a table,
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
	 * When our skill session is started, a launch event will be triggered.
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
	 * This method will be fired when the skill session has been closed.
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
	 * When we receive an intent this will be triggered. This function will handle
	 * the processing of that intent based on the intentName.
	 * 
	 * @param speechletRequestEnvelope
	 *            container for the speechlet request.
	 * @return SpeechletResponse a message of our address or an error message
	 */
	@Override
	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> speechletRequestEnvelope) {
		SpeechletResponse response;
		IntentRequest intentRequest = speechletRequestEnvelope.getRequest();
		Session session = speechletRequestEnvelope.getSession();

		LOG.info("onIntent requestId={}, sessionId={}", intentRequest.getRequestId(), session.getSessionId());

		Intent intent = intentRequest.getIntent();
		String intentName = getIntentName(intent);

		LOG.info("Intent received: {}", intentName);
		if (intentName != null) {
			switch (intentName) {
			case CRANE_COMMAND_INTENT:
				try {
					response = handleCraneCommandIntent(intent, session);
				} catch (Alexa2Ev3Exception e) {
					LOG.error(e.getMessage(), e);
					response = getAskResponse(ALEXA2EV3_CARD_TITLE, ERROR_TEXT + e.getCause());
				}
				break;
			case BRICK_STATE_REQUEST_INTENT:
				try {
					response = handleCraneStateRequestIntent(intent, session);
				} catch (Alexa2Ev3Exception e) {
					LOG.error(e.getMessage(), e);
					response = getAskResponse(ALEXA2EV3_CARD_TITLE, ERROR_TEXT);
				}
				break;
			case "AMAZON.StopIntent":
			case "AMAZON.CancelIntent":
				response = handleStopIntent(session);
				break;
			case "AMAZON.HelpIntent":
				response = getAskResponse(ALEXA2EV3_CARD_TITLE, HELP_TEXT);
				break;
			case "AMAZON.RepeatIntent":
				try {
					response = handleRepeatIntent(intent, session);
				} catch (Alexa2Ev3Exception e) {
					LOG.error(e.getMessage(), e);
					response = getAskResponse(ALEXA2EV3_CARD_TITLE, ERROR_TEXT);
				}
				break;
			default:
				response = getAskResponse(ALEXA2EV3_CARD_TITLE, UNHANDLED_TEXT);
			}
		} else {
			response = getAskResponse(ALEXA2EV3_CARD_TITLE, UNHANDLED_TEXT);
		}
		return response;
	}

	private SpeechletResponse handleRepeatIntent(Intent intent, Session session) throws Alexa2Ev3Exception {
		SpeechletResponse response = null;

		Alexa2Ev3Command lastCommand = getLastCommand(session);
		if (lastCommand != null) {
			snsClient.publish(lastCommand);
			response = getConfirmResponse(lastCommand.getAction().getDeAction(), lastCommand.getValue());
		} else {
			response = getAskResponse(ALEXA2EV3_CARD_TITLE, LAST_INTENT_FAIL_TEXT);
		}
		return response;
	}

	private Alexa2Ev3Command getLastCommand(Session session) throws Alexa2Ev3Exception {
		Alexa2Ev3Command command = null;
		try {
			String foundedCommandJson = session.getAttribute(LAST_COMMAND).toString();
			command = Alexa2Ev3Command.fromJson(foundedCommandJson);
		} catch (IOException e) {
			throw new Alexa2Ev3Exception("getting command failed", e);
		}
		return command;
	}

	private SpeechletResponse handleCraneCommandIntent(Intent intent, Session session) throws Alexa2Ev3Exception {

		Alexa2Ev3Command command = getEV3Command(intent);
		if (command != null) {
			try {
				String commandJson = command.toJson();
				LOG.info("save last command: " + commandJson);
				session.setAttribute(LAST_COMMAND, command.toJson());
			} catch (JsonProcessingException e) {
				throw new Alexa2Ev3Exception("setting attribute failed", e);
			}
			snsClient.publish(command);
			return getConfirmResponse(command.getAction().getDeAction(), command.getValue());
		}
		return getAskResponse(ALEXA2EV3_CARD_TITLE, UNHANDLED_TEXT);
	}

	private Alexa2Ev3Command getEV3Command(final Intent intent) {
		Alexa2Ev3Command command = null;
		Slot actionSlot = intent.getSlot(SLOT_ACTION);
		if (actionSlot != null) {
			Alexa2Ev3CommandAction action = Alexa2Ev3CommandAction.getAction(actionSlot.getValue());

			if (action != null) {
				Slot valueSlot = intent.getSlot(SLOT_VALUE);
				String value = valueSlot != null ? valueSlot.getValue() : "";
				command = new Alexa2Ev3Command(action, value);
			}
		}
		return command;
	}

	private SpeechletResponse handleCraneStateRequestIntent(final Intent intent, final Session session)
			throws Alexa2Ev3Exception {
		Slot parameterSlot = intent.getSlot(SLOT_STATE_PARAMETER);
		if (parameterSlot != null) {

			Ev3ThingState thing = iotClient.getThingState();

			return getStateDeviceResponse(thing, parameterSlot.getValue());
		}
		return getAskResponse(ALEXA2EV3_CARD_TITLE, UNHANDLED_TEXT);
	}

	private SpeechletResponse handleStopIntent(Session session) {
		PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
		outputSpeech.setText(GOODBYE_TEXT);

		return SpeechletResponse.newTellResponse(outputSpeech);
	}

	private SpeechletResponse getAskResponse(String cardTitle, String speechText) {
		StandardCard card = getStandardCard(cardTitle, speechText);
		PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);
		Reprompt reprompt = createReprompt(SAY_AGAIN_TEXT);
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
		String speechText = String.format(COMMAND_CONFIRMATION_TEXT, action + (value != null ? value : ""));

		Reprompt reprompt = createReprompt(SAY_AGAIN_TEXT);
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
	private SpeechletResponse getStateDeviceResponse(Ev3ThingState thingState, String parameter) {

		Reprompt reprompt = createReprompt(STATE_REPROMT_TEXT);
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
	private Reprompt createReprompt(String repormText) {
		OutputSpeech repromtSpeech = getPlainTextOutputSpeech(repormText);
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(repromtSpeech);
		return reprompt;
	}

}
