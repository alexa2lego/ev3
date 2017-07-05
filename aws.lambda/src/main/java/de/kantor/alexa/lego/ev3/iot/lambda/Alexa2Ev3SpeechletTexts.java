package de.kantor.alexa.lego.ev3.iot.lambda;

/**
 * This interface  has all texts for {@link Alexa2Ev3Speechlet}
 * 
 * @author kantor
 *
 */
public interface Alexa2Ev3SpeechletTexts {


	static final String WELCOME_TEXT = "Hallo! Zuerst brauche ich die PIN deines Roboters. Sag, zum Beispiel, PIN ist... eins, zwei, drei, vier, fünf.";

    static final String PIN_CORRECT_TEXT = "PIN ist korrekt. Jetzt gib mir Dein Kommando.";

	static final String PIN_INCORRECT_TEXT = "PIN ist nicht korrekt. Versuche es noch mal.";

	static final String ENTER_PIN_REPROMT_TEXT = "Bitte gib mir die beim Start Deines Robotes generierte PIN. Sag. PIN ist. Und dann die fünfstellige Nummer, Ziffer für Ziffer.";

	static final String GOODBYE_TEXT = "Auf Wiedersehen!";

	static final String HELP_TEXT = "Du kannst sagen zum Beispiel. Links zwanzig. Rechts. Greifen. Status von Batterie.";

	static final String UNHANDLED_TEXT = "Ich habe Dein Kommando nicht verstanden. Bitte versuche noch ein mal.";

	static final String ERROR_TEXT = "Es ist ein Fehler aufgetreten. Versuche noch mal, bitte.";

	static final String SAY_AGAIN_TEXT = "Sag bitte noch mal.";

	 static final String COMMAND_CONFIRMATION_TEXT = "okay: %s.";

	 static final String STATE_REPROMT_TEXT = "Du kannst Status abfragen, indem Du zum Beispiel sagst. Status Batterie.";

	 static final String LAST_INTENT_FAIL_TEXT = "Ich habe noch kein Kommando, das ich weiderholen kann.";


}
