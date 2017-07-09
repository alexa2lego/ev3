package de.kantor.alexa.lego.ev3.iot.lambda;

public enum Alexa2Ev3SpeechTexts {

	// @formatter:off
	WELCOME_TEXT(
			"Hallo! Zuerst brauche ich die PIN deines Roboters. Sag, zum Beispiel, PIN ist... eins, zwei, drei, vier, fünf.",""),

	PIN_CORRECT_TEXT("PIN ist korrekt. Jetzt gib mir Dein Kommando.", ""),

	PIN_INCORRECT_TEXT("PIN ist nicht korrekt. Versuche es noch mal.", ""),
	
	DEVICE_INACTIVE("Der Roboter scheint nicht aktiv zu sein",""),

	ENTER_PIN_REPROMT_TEXT(
			"Bitte gib mir die beim Start Deines Robotes generierte PIN. Sag. PIN ist. Und dann die fünfstellige Nummer, Ziffer für Ziffer.", ""),

	GOODBYE_TEXT("Auf Wiedersehen!", ""),

	HELP_TEXT("Du kannst sagen zum Beispiel. Links zwanzig. Rechts. Greifen. Status von Batterie.", ""),

	UNHANDLED_TEXT("Ich habe Dein Kommando nicht verstanden. Bitte versuche noch ein mal.", ""),

	ERROR_TEXT("Es ist ein Fehler aufgetreten. Versuche noch mal, bitte.", ""),

	SAY_AGAIN_TEXT("Sag bitte noch mal.", ""),

	COMMAND_CONFIRMATION_TEXT("okay: %s", ""),

	STATE_REPROMT_TEXT("Du kannst Status abfragen, indem Du zum Beispiel sagst. Status Batterie.", ""),

	LAST_INTENT_FAIL_TEXT("Ich habe noch kein Kommando, das ich wiederholen kann.", ""),

	CURRENT_VOLTAGE_NOT_FOUND_TEXT("Die Spannung konnte nicht ermittelt werden.", ""),

	CURRENT_VOLTAGE_TEXT("Die Spannung beträgt %s Volt.", "");
	
	// @formatter:on

	private String deText;
	private String enText;

	Alexa2Ev3SpeechTexts(String deText, String enText) {
		this.deText = deText;
		this.enText = enText;
	}

	public String getDeText() {
		return deText;
	}

	public String getEnText() {
		return enText;
	}

}
