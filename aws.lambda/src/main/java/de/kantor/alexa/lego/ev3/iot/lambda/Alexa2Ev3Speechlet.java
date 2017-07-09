package de.kantor.alexa.lego.ev3.iot.lambda;

import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.COMMAND_CONFIRMATION_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.CURRENT_VOLTAGE_NOT_FOUND_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.CURRENT_VOLTAGE_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.ENTER_PIN_REPROMT_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.ERROR_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.GOODBYE_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.HELP_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.LAST_INTENT_FAIL_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.PIN_CORRECT_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.PIN_INCORRECT_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.SAY_AGAIN_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.STATE_REPROMT_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.UNHANDLED_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.WELCOME_TEXT;

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

	private static final String ENTER_PIN_INTENT = "EnterPinIntent";

	private static final String COMMAND_INTENT = "CommandIntent";

	private static final String STATE_REQUEST_INTENT = "StateRequestIntent";

	private static final String SESSION_LAST_ATTRIBUTE = "lastCommand";

	private static final String SESSION_PIN_ATTRIBUTE = "PIN";

	private static final String ALEXA2EV3_CARD_TITLE = "Alexa2Lego Skill";

	private static final String SLOT_ACTION = "Action";

	private static final String SLOT_VALUE = "Value";

	private static final String SLOT_PIN = "PIN";

	private static final String SLOT_STATE_PARAMETER = "Parameter";

	// private Alexa2Ev3SnsClient snsClient;

	private Alexa2Ev3IotClient iotClient;

	public Alexa2Ev3Speechlet() {

		iotClient = Alexa2Ev3IotClient.getInstance();
		// snsClient = new Alexa2Ev3SnsClient();
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

		return getAskResponse(WELCOME_TEXT.getDeText());
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
		setPinInSession(session, null);
		session.setAttribute(SESSION_LAST_ATTRIBUTE, null);
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
			case ENTER_PIN_INTENT:
				try {
					response = handleEnterPinIntent(intent, session);
				} catch (Alexa2Ev3Exception e) {
					LOG.error(e.getMessage(), e);
					response = getAskResponse(ERROR_TEXT.getDeText());
				}
				break;
			case COMMAND_INTENT:
				try {
					response = handleCommandIntent(intent, session);
				} catch (Alexa2Ev3Exception e) {
					LOG.error(e.getMessage(), e);
					response = getAskResponse(ERROR_TEXT.getDeText());
				}
				break;
			case STATE_REQUEST_INTENT:
				try {
					response = handleStateRequestIntent(intent);
				} catch (Alexa2Ev3Exception e) {
					LOG.error(e.getMessage(), e);
					response = getAskResponse(ERROR_TEXT.getDeText());
				}
				break;
			case "AMAZON.StopIntent":
			case "AMAZON.CancelIntent":
				response = handleStopIntent(session);
				break;
			case "AMAZON.HelpIntent":
				response = getAskResponse(HELP_TEXT.getDeText());
				break;
			case "AMAZON.RepeatIntent":
				try {
					response = handleRepeatIntent(session);
				} catch (Alexa2Ev3Exception e) {
					LOG.error(e.getMessage(), e);
					response = getAskResponse(ERROR_TEXT.getDeText());
				}
				break;
			default:
				response = getAskResponse(UNHANDLED_TEXT.getDeText());
			}
		} else {
			response = getAskResponse(UNHANDLED_TEXT.getDeText());
		}
		return response;
	}

	private SpeechletResponse handleEnterPinIntent(final Intent intent, final Session session)
			throws Alexa2Ev3Exception {
		Slot pinSlot = intent.getSlot(SLOT_PIN);
		if (pinSlot != null) {

			Ev3Thing thing = iotClient.getThingState();

			return getPinValidateResponse(thing, pinSlot.getValue(), session);
		}
		return getAskResponse(UNHANDLED_TEXT.getDeText());
	}

	private SpeechletResponse handleRepeatIntent(Session session) throws Alexa2Ev3Exception {
		SpeechletResponse response;

		Alexa2Ev3Command lastCommand = getLastCommand(session);
		if (lastCommand != null) {
			iotClient.sendCommand(lastCommand);
			response = getConfirmResponse(lastCommand.getAction().getDeAction(), lastCommand.getValue());
		} else {
			response = getAskResponse(LAST_INTENT_FAIL_TEXT.getDeText());
		}
		return response;
	}

	private Alexa2Ev3Command getLastCommand(Session session) throws Alexa2Ev3Exception {
		Alexa2Ev3Command command = null;
		try {
			Object foundedCommandJson = session.getAttribute(SESSION_LAST_ATTRIBUTE);
			if (foundedCommandJson != null) {
				command = Alexa2Ev3Command.fromJson(foundedCommandJson.toString());
			}
		} catch (IOException e) {
			throw new Alexa2Ev3Exception("getting command failed", e);
		}
		return command;
	}

	private SpeechletResponse handleCommandIntent(Intent intent, Session session) throws Alexa2Ev3Exception {
		String pin = (String) session.getAttribute(SESSION_PIN_ATTRIBUTE);
		if (!Strings.isEmpty(pin)) {
			Alexa2Ev3Command command = getEV3Command(intent);
			if (command != null) {
				try {
					String commandJson = command.toJson();
					LOG.info("save last command: " + commandJson);
					session.setAttribute(SESSION_LAST_ATTRIBUTE, command.toJson());
				} catch (JsonProcessingException e) {
					throw new Alexa2Ev3Exception("setting attribute failed", e);
				}
				iotClient.sendCommand(command);
				return getConfirmResponse(command.getAction().getDeAction(), command.getValue());
			}
		} else {
			return getAskResponse(ENTER_PIN_REPROMT_TEXT.getDeText());
		}
		return getAskResponse(UNHANDLED_TEXT.getDeText());
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

	private SpeechletResponse handleStateRequestIntent(final Intent intent) throws Alexa2Ev3Exception {
		Slot parameterSlot = intent.getSlot(SLOT_STATE_PARAMETER);
		if (parameterSlot != null) {

			Ev3Thing thing = iotClient.getThingState();

			return getStateDeviceResponse(thing, parameterSlot.getValue());
		}
		return getAskResponse(UNHANDLED_TEXT.getDeText());
	}

	private SpeechletResponse handleStopIntent(Session session) {
		PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
		outputSpeech.setText(GOODBYE_TEXT.getDeText());
		setPinInSession(session, null);
		return SpeechletResponse.newTellResponse(outputSpeech);
	}

	private SpeechletResponse getAskResponse(String speechText) {
		StandardCard card = getStandardCard(ALEXA2EV3_CARD_TITLE, speechText);
		PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);
		Reprompt reprompt = createReprompt(SAY_AGAIN_TEXT.getDeText());
		return SpeechletResponse.newAskResponse(speech, reprompt, card);
	}

	/**
	 * Creates a {@code SpeechletResponse} for the confirmation.
	 * 
	 * @param action
	 * @param value
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getConfirmResponse(String action, String value) {
		String speechText = String.format(COMMAND_CONFIRMATION_TEXT.getDeText(),
				action + " " + (value != null ? value : ""));

		Reprompt reprompt = createReprompt(SAY_AGAIN_TEXT.getDeText());
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
	private SpeechletResponse getStateDeviceResponse(Ev3Thing thingState, String parameter) {

		Reprompt reprompt = createReprompt(STATE_REPROMT_TEXT.getDeText());
		String cardText = "";
		String speachText = "";
		if (thingState != null) {
			switch (parameter) {
			case "batterie":

				String voltage = thingState.state.reported.battery.get("voltageVolts");
				if (!Strings.isEmpty(voltage)) {
					String voltageValue = convertVoltageValue(voltage);
					speachText = String.format(CURRENT_VOLTAGE_TEXT.getDeText(), voltageValue);
					cardText = thingState.state.reported.battery.toString();
				} else {
					speachText = CURRENT_VOLTAGE_NOT_FOUND_TEXT.getDeText();
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

	private String convertVoltageValue(String voltage) {
		DecimalFormat f = new DecimalFormat("#0.00");
		String voltageValue = f.format(Double.valueOf(voltage) / 1000000);
		voltageValue = voltageValue.replace(".", " Komma ");
		return voltageValue;
	}

	private SpeechletResponse getPinValidateResponse(Ev3Thing thingState, String pin, final Session session) {

		Reprompt reprompt = createReprompt(ENTER_PIN_REPROMT_TEXT.getDeText());
		String cardText = "";
		String speachText = "";
		if (thingState != null) {
			String expectedPin = thingState.state.reported.pin;
			LOG.info("pin:" + pin + ", expectedPin=" + expectedPin);
			if (!Strings.isEmpty(expectedPin) && !Strings.isEmpty(pin) && pin.equals(expectedPin)) {
				speachText = PIN_CORRECT_TEXT.getDeText();
				cardText = PIN_CORRECT_TEXT.getDeText();
				setPinInSession(session, pin);
			} else {
				speachText = PIN_INCORRECT_TEXT.getDeText();
				cardText = PIN_INCORRECT_TEXT.getDeText();
			}
		}
		StandardCard card = getStandardCard(ALEXA2EV3_CARD_TITLE, cardText);

		PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speachText);

		return SpeechletResponse.newAskResponse(speech, reprompt, card);
	}

	private void setPinInSession(final Session session, final String pin) {
		session.setAttribute(SESSION_PIN_ATTRIBUTE, pin);
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
