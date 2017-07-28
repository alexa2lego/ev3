package de.kantor.alexa.lego.ev3.iot.lambda;

public enum Alexa2Ev3SpeechTexts {

	// @formatter:off
	WELCOME_TEXT(
			"Hallo! Du kannst folgende Geräte per Sprache steuern: %s. Ich schicke Dir Liste von allen möglichen Befehlen. Bitte gebe mir Dein Kommando...",""),

	DEVICE_INACTIVE("Der Roboter scheint nicht aktiv zu sein",""),

	GOODBYE_TEXT("Chao!", ""),

	HELP_TEXT("Wenn Du nicht weisst, wie Du Deine Roboter mit Sprachbefehlen steuern kannst, schaue in die Liste, die ich Dir geschickt habe.", ""),

	UNHANDLED_TEXT("Ich habe Dein Kommando nicht verstanden. Bitte versuche es noch ein mal.", ""),

	COMMAND_MISUNDERSTOOD_TEXT("Dein Kommando: %s habe ich nicht verstanden. Bitte versuche es noch ein mal.", ""),

	ERROR_TEXT("Es ist ein Fehler aufgetreten. Versuche noch mal, bitte.", ""),

	SAY_AGAIN_TEXT("Sag bitte noch mal.", ""),

	COMMAND_CONFIRMATION_TEXT("okay: %s", ""),

	STATE_REPROMT_TEXT("Du kannst Status Deiner Roboter abfragen, indem Du Name vom Roboter und Status sagst", ""),

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
