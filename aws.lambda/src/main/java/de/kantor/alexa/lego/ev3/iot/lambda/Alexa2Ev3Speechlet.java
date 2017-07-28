package de.kantor.alexa.lego.ev3.iot.lambda;

import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.COMMAND_CONFIRMATION_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.COMMAND_MISUNDERSTOOD_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.ERROR_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.GOODBYE_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.HELP_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.SAY_AGAIN_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.STATE_REPROMT_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.UNHANDLED_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.WELCOME_TEXT;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
 * This class {@link Alexa2Ev3Speechlet} implements {@link SpeechletV2} ....
 * 
 * @author kantor
 *
 */
public class Alexa2Ev3Speechlet implements SpeechletV2 {

	private static final Logger LOG = LoggerFactory.getLogger(Alexa2Ev3Speechlet.class);

	private static final String COMMAND_INTENT1 = "GenericIntentA";
	private static final String COMMAND_INTENT2 = "GenericIntentB";
	private static final String COMMAND_INTENT3 = "GenericIntentC";
	private static final String COMMAND_INTENT4 = "GenericIntentD";
	private static final String COMMAND_INTENT5 = "GenericIntentE";

	private static final String STATE_REQUEST_INTENT = "StateRequestIntent";

	private static final String ALEXA2EV3_CARD_TITLE = "Alexa2Robot";

	private static final String SLOT_RECEIVER = "SlotA";

	private static final String SLOT_ACTION = "SlotB";

	private static final String SLOT_OPTION = "SlotC";

	private static final String SLOT_VALUE = "Value";

	private Alexa2Ev3IotClient iotClient;

	private Alexa2Ev3DynamoDBClient dynamoDBClient;

	private List<Ev3Device> devices;

	public Alexa2Ev3Speechlet() {

		dynamoDBClient = Alexa2Ev3DynamoDBClient.getInstance();
		devices = dynamoDBClient.findAllDevices();
		LOG.info(devices.toString());
		iotClient = Alexa2Ev3IotClient.getInstance(devices);

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
		String infoCardText = getAllCommandsAsText();
		String devicesText = devices.stream().map(d -> d.getAliasName()).collect(Collectors.joining(","));
		return getAskResponseAndInfoCard(String.format(WELCOME_TEXT.getDeText(), devicesText), infoCardText);
	}

	private String getAllCommandsAsText() {
		// TODO TEXT formatieren!
		return devices.toString();
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
			case COMMAND_INTENT1:
			case COMMAND_INTENT2:
			case COMMAND_INTENT3:
			case COMMAND_INTENT4:
			case COMMAND_INTENT5:
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
			default:
				response = getAskResponse(UNHANDLED_TEXT.getDeText());
			}
		} else {
			response = getAskResponse(UNHANDLED_TEXT.getDeText());
		}
		return response;
	}

	private SpeechletResponse handleCommandIntent(Intent intent, Session session) throws Alexa2Ev3Exception {
		Slot receiverSlot = intent.getSlot(SLOT_RECEIVER);

		if (receiverSlot != null && !Strings.isEmpty(receiverSlot.getValue())) {
			String receiver = receiverSlot.getValue();
			Slot actionSlot = intent.getSlot(SLOT_ACTION);
			Alexa2Ev3Command command = null;
			String action = "";
			String option = "";
			String value = "";
			if (actionSlot != null) {
				action = actionSlot.getValue();
				if (!Strings.isEmpty(action)) {
					Slot optionSlot = intent.getSlot(SLOT_OPTION);

					if (optionSlot != null && optionSlot.getValue() != null) {
						option = optionSlot.getValue();
					}
					Slot valueSlot = intent.getSlot(SLOT_VALUE);

					if (valueSlot != null && valueSlot.getValue() != null) {
						value = valueSlot.getValue();
					}
					command = generateEv3Command(receiver, action, option, value);
				}
			}

			if (command != null) {
				iotClient.sendCommand(receiver, command);
				return getConfirmResponse(receiver, command.getAction(), command.getValue());
			} else {
				return getAskResponse(
						String.format(COMMAND_MISUNDERSTOOD_TEXT.getDeText(), receiver + " " + action + " " + option));
			}
		}
		return getAskResponse(UNHANDLED_TEXT.getDeText());
	}

	private Alexa2Ev3Command generateEv3Command(final String receiver, final String action, final String option,
			final String value) {
		String key = (action + " " + option).toLowerCase();

		Optional<Map<String, String>> oCommand = devices.stream()
				.filter(d -> d.getAliasName().equalsIgnoreCase(receiver)).map(d -> d.getCommands())
				.filter(c -> c.containsKey(key)).findFirst();
		if (oCommand.isPresent()) {
			return new Alexa2Ev3Command(oCommand.get().get(key.trim()), value);
		}
		LOG.info("cannot generate Ev3Command for receiver: " + receiver + " and key: " + key);
		return null;
	}

	private SpeechletResponse handleStateRequestIntent(final Intent intent) throws Alexa2Ev3Exception {
		Slot receiverSlot = intent.getSlot(SLOT_RECEIVER);
		if (receiverSlot != null && receiverSlot.getValue() != null) {

			Ev3Device thing = iotClient.getThingState(receiverSlot.getValue());

			return getStateDeviceResponse(thing, receiverSlot.getValue());
		}
		return getAskResponse(UNHANDLED_TEXT.getDeText());
	}

	private SpeechletResponse handleStopIntent(Session session) {
		PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
		outputSpeech.setText(GOODBYE_TEXT.getDeText());
		return SpeechletResponse.newTellResponse(outputSpeech);
	}

	private SpeechletResponse getAskResponse(String speechText) {
		StandardCard card = getStandardCard(ALEXA2EV3_CARD_TITLE, speechText);
		PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);
		Reprompt reprompt = createReprompt(SAY_AGAIN_TEXT.getDeText());
		return SpeechletResponse.newAskResponse(speech, reprompt, card);
	}

	private SpeechletResponse getAskResponseAndInfoCard(String speechText, String cardInfoText) {
		StandardCard card = getStandardCard(ALEXA2EV3_CARD_TITLE, cardInfoText);
		PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);
		Reprompt reprompt = createReprompt(SAY_AGAIN_TEXT.getDeText());
		return SpeechletResponse.newAskResponse(speech, reprompt, card);
	}

	/**
	 * Creates a {@code SpeechletResponse} for the confirmation.
	 * 
	 * @param receiver
	 * @param action
	 * @param value
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getConfirmResponse(String receiver, String action, String value) {
		String speechText = String.format(COMMAND_CONFIRMATION_TEXT.getDeText(),
				receiver + ", " + action + " " + (value != null ? value : ""));

		Reprompt reprompt = createReprompt(SAY_AGAIN_TEXT.getDeText());
		StandardCard card = getStandardCard(ALEXA2EV3_CARD_TITLE,
				receiver + ", " + action + " " + (value != null ? value : ""));

		PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

		return SpeechletResponse.newAskResponse(speech, reprompt, card);
	}

	/**
	 * Creates a {@code SpeechletResponse} for the State Request intent.
	 * 
	 * @param stateDevice
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getStateDeviceResponse(Ev3Device thingState, String receiver) {

		Reprompt reprompt = createReprompt(STATE_REPROMT_TEXT.getDeText());
		String cardText = "";
		String speachText = "";
		// if (thingState != null) {
		//
		// // TODO Status zeigen vollständig!
		// String voltage = thingState.state.reported.battery.get("voltageVolts");
		// if (!Strings.isEmpty(voltage)) {
		// String voltageValue = convertVoltageValue(voltage);
		// speachText = String.format(CURRENT_VOLTAGE_TEXT.getDeText(), voltageValue);
		// cardText = thingState.state.reported.battery.toString();
		// } else {
		// speachText = CURRENT_VOLTAGE_NOT_FOUND_TEXT.getDeText();
		// cardText = speachText + " :-(";
		// }
		// }

		StandardCard card = getStandardCard(ALEXA2EV3_CARD_TITLE, cardText);

		PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speachText);

		return SpeechletResponse.newAskResponse(speech, reprompt, card);
	}

	private String convertVoltageValue(String voltage) {
		DecimalFormat f = new DecimalFormat("#0.00");
		String voltageValue = f.format(Double.valueOf(voltage));
		voltageValue = voltageValue.replace(".", " Komma ");
		return voltageValue;
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
