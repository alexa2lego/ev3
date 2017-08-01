package de.kantor.alexa.lego.ev3.iot.lambda;

import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.COMMAND_CANNOT_BE_UNDERSTOOD_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.COMMAND_CONFIRMATION_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.ERROR_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.GOODBYE_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.HELP_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.SAY_AGAIN_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.UNHANDLED_TEXT;
import static de.kantor.alexa.lego.ev3.iot.lambda.Alexa2Ev3SpeechTexts.WELCOME_TEXT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.paho.client.mqttv3.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Directive;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.IntentRequest.DialogState;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.speechlet.dialog.directives.DelegateDirective;
import com.amazon.speech.speechlet.dialog.directives.DialogIntent;
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

	private static final String ALEXA2EV3_CARD_TITLE = "Alexa2LEGO";

	private static final String COMMAND_INTENT = "CommandIntent";

	private static final String SLOT_DEVICENAME = "deviceName";

	private static final String SLOT_ACTION = "action";

	private static final String SLOT_OPTION = "option";

	private static final String SLOT_VALUE = "value";

	private Alexa2Ev3IotClient iotClient;

	private Alexa2Ev3DynamoDBClient dynamoDBClient;

	private List<Ev3Device> devices;

	public Alexa2Ev3Speechlet() {
		dynamoDBClient = Alexa2Ev3DynamoDBClient.getInstance();
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
		devices = dynamoDBClient.findAllDevices();
		LOG.info(devices.toString());
		iotClient = Alexa2Ev3IotClient.getInstance(devices);
		int last = devices.size() - 1;
		List<String> deviceNames = devices.stream().map(d -> d.getDeviceName()).collect(Collectors.toList());
		String devicesText = String.join(" und ", String.join(", ", deviceNames.subList(0, last)),
				deviceNames.get(last));
		String infoCardText = getAllCommandsAsText();

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
		SpeechletResponse response = null;
		IntentRequest intentRequest = speechletRequestEnvelope.getRequest();
		Session session = speechletRequestEnvelope.getSession();

		LOG.info("onIntent requestId={}, sessionId={}", intentRequest.getRequestId(), session.getSessionId());

		Intent intent = intentRequest.getIntent();
		String intentName = getIntentName(intent);

		LOG.info("Intent received: {}", intentName);

		if (intentName != null) {
			switch (intentName) {
			case COMMAND_INTENT:
				DialogState dialogState = intentRequest.getDialogState();
				if (DialogState.STARTED.equals(dialogState) || DialogState.IN_PROGRESS.equals(dialogState)) {
					response = getDialogResponse(intent, dialogState, session);
				} else if (DialogState.COMPLETED.equals(dialogState)) {
					LOG.debug("dialogState  = COMPLETED ");
					try {
						response = handleCommandIntent(intent);
					} catch (Alexa2Ev3Exception e) {
						LOG.error(e.getMessage(), e);
						response = getAskResponse(ERROR_TEXT.getDeText());
					}
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

	private SpeechletResponse getDialogResponse(final Intent intent, final DialogState dialogueState,
			final Session session) {
		LOG.debug("dialogueState: " + dialogueState.name());
		DialogIntent dialogIntent = new DialogIntent(intent);
		DelegateDirective dd = new DelegateDirective();
		dd.setUpdatedIntent(dialogIntent);
		List<Directive> directiveList = new ArrayList<Directive>();
		directiveList.add(dd);
		SpeechletResponse speechletResp = new SpeechletResponse();
		speechletResp.setDirectives(directiveList);
		speechletResp.setShouldEndSession(false);
		LOG.debug("all SlotsValue: "
				+ intent.getSlots().values().stream().map(s -> s.getValue()).collect(Collectors.joining(",")));

		return speechletResp;
	}

	private SpeechletResponse handleCommandIntent(final Intent intent) throws Alexa2Ev3Exception {
		Alexa2Ev3Command command = null;

		String deviceName = intent.getSlot(SLOT_DEVICENAME).getValue();
		String action = intent.getSlot(SLOT_ACTION).getValue();
		String option = intent.getSlot(SLOT_OPTION) != null ? intent.getSlot(SLOT_OPTION).getValue() : null;
		String value = intent.getSlot(SLOT_VALUE) != null ? intent.getSlot(SLOT_VALUE).getValue() : null;
		String commandText = deviceName + " " + action + " " + option + " " + value;

		if (!Strings.isEmpty(deviceName) && !Strings.isEmpty(action)) {
			command = generateEv3Command(deviceName, action, option, value);
			LOG.debug("Command found: " + command);
		}
		if (command != null) {
			iotClient.sendCommand(deviceName, command);
			return getConfirmResponse(deviceName, command.getAction(), command.getValue());
		}
		return getAskResponse(String.format(COMMAND_CANNOT_BE_UNDERSTOOD_TEXT.getDeText(), commandText));

	}

	private Alexa2Ev3Command generateEv3Command(final String deviceName, final String action, final String option,
			final String value) {
		String commandKey = (action + " " + option).trim().toLowerCase();
		String v = value != null ? value : "";
		Optional<Map<String, String>> oCommand = devices.stream()
				.filter(d -> d.getDeviceName().equalsIgnoreCase(deviceName)).map(d -> d.getCommands())
				.filter(c -> c.containsKey(commandKey)).findFirst();
		if (oCommand.isPresent()) {
			return new Alexa2Ev3Command(oCommand.get().get(commandKey.trim()), v);
		}
		LOG.info("cannot generate Ev3Command for deviceName: " + deviceName + " and commandKey: " + commandKey
				+ ", value=" + v);
		return null;
	}

	private SpeechletResponse handleStopIntent(Session session) {
		PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
		outputSpeech.setText(GOODBYE_TEXT.getDeText());
		return SpeechletResponse.newTellResponse(outputSpeech);
	}

	private SpeechletResponse getAskResponse(String speechText) {
		StandardCard card = getStandardCard(ALEXA2EV3_CARD_TITLE, speechText);
		OutputSpeech speech = getPlainTextOutputSpeech(speechText);
		Reprompt reprompt = createReprompt(SAY_AGAIN_TEXT.getDeText());
		return SpeechletResponse.newAskResponse(speech, reprompt, card);
	}

	private SpeechletResponse getAskResponseAndInfoCard(String speechText, String cardInfoText) {
		StandardCard card = getStandardCard(ALEXA2EV3_CARD_TITLE, cardInfoText);
		OutputSpeech speech = getPlainTextOutputSpeech(speechText);
		Reprompt reprompt = createReprompt(SAY_AGAIN_TEXT.getDeText());
		return SpeechletResponse.newAskResponse(speech, reprompt, card);
	}

	/**
	 * Creates a {@code SpeechletResponse} for the confirmation.
	 * 
	 * @param deviceName
	 * @param action
	 * @param value
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getConfirmResponse(String deviceName, String action, String value) {
		String speechText = String.format(COMMAND_CONFIRMATION_TEXT.getDeText(),
				deviceName + ", " + action + " " + (value != null ? value : ""));

		Reprompt reprompt = createReprompt(SAY_AGAIN_TEXT.getDeText());
		StandardCard card = getStandardCard(ALEXA2EV3_CARD_TITLE,
				deviceName + ", " + action + " " + (value != null ? value : ""));

		OutputSpeech speech = getPlainTextOutputSpeech(speechText);

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
