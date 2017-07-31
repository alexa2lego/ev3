package de.kantor.alexa.lego.ev3.iot.lambda;

public enum Alexa2Ev3SpeechTexts {

	// @formatter:off
	WELCOME_TEXT(
			"Du kannst %s steuern. Ich schicke Dir Liste von allen möglichen Befehlen. Bitte gebe mir Dein Kommando...",""),

	DEVICE_INACTIVE("Der Roboter scheint nicht aktiv zu sein",""),

	GOODBYE_TEXT("Chao!", ""),

	HELP_TEXT("Wenn Du nicht weisst, wie Du Deine Roboter mit Sprachbefehlen steuern kannst, schaue in die Liste, die ich Dir geschickt habe.", ""),

	UNHANDLED_TEXT("Ich habe Dein Kommando nicht verstanden. Bitte versuche es noch ein mal.", ""),

	COMMAND_MISUNDERSTOOD_TEXT("Dein Kommando: %s kann ich nicht ausführen. Bitte versuche es noch ein mal.", ""),

	ERROR_TEXT("Es ist ein Fehler aufgetreten. Versuche es noch mal, bitte.", ""),

	SAY_AGAIN_TEXT("Sag bitte noch mal.", ""),

	COMMAND_CONFIRMATION_TEXT("okay: %s", "");

	
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
