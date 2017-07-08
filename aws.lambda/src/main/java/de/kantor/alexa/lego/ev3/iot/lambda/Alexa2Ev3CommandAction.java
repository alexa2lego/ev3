package de.kantor.alexa.lego.ev3.iot.lambda;

import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Alexa2Ev3CommandAction {
	// @formatter:off
	LEFT("links", "left"), 
	RIGHT("rechts", "right"), 
	STOP("stopp", "stop"), 
	UP("hoch", "up"), 
	DOWN("runter", "down"), 
	CATCH("greifen", "catch"), 
	RELEASE("loslassen", "release"), 
	OPEN("öffnen", "release"),
	FORWARDS("geradeaus", "forwards"),
	BACKWARDS("rückwärts","backwards");
	// @formatter:on

	private String deAction;
	private String enAction;

	Alexa2Ev3CommandAction(String deAction, String enAction) {
		this.deAction = deAction;
		this.enAction = enAction;
	}

	Alexa2Ev3CommandAction(@JsonProperty("action") String deAction) {
		this.deAction = deAction;
		Alexa2Ev3CommandAction action = Alexa2Ev3CommandAction.getAction(deAction);
		if (action != null) {
			this.enAction = action.enAction;
		}
	}

	public String getDeAction() {
		return deAction;
	}

	public String getEnAction() {
		return enAction;
	}

	public static Alexa2Ev3CommandAction getAction(String actionDe) {

		Optional<Alexa2Ev3CommandAction> action = Arrays.asList(Alexa2Ev3CommandAction.values()).stream()
				.filter(a -> actionDe.equals(a.deAction)).findFirst();
		return action.isPresent() ? action.get() : null;
	}

}
