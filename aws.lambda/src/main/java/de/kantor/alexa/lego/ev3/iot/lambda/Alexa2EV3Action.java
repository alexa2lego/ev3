package de.kantor.alexa.lego.ev3.iot.lambda;

import java.util.Arrays;
import java.util.Optional;

public enum Alexa2EV3Action {
	// @formatter:off
	LEFT("links", "left"), 
	RIGHT("rechts", "right"), 
	STOP("stopp", "stop"), 
	UP("hoch", "up"), 
	DOWN("runter", "down"), 
	CATCH("greifen", "catch"), 
	RELEASE("loslassen", "release"), 
	OPEN("öffnen", "release");
	// @formatter:on

	private String actionDe;
	private String actionEn;

	Alexa2EV3Action(String actionDe, String actionEn) {
		this.actionDe = actionDe;
		this.actionEn = actionEn;
	}

	public String getActionDe() {
		return actionDe;
	}

	public String getActionEn() {
		return actionEn;
	}

	public static Alexa2EV3Action getAction(String actionDe) {

		Optional<Alexa2EV3Action> action = Arrays.asList(Alexa2EV3Action.values()).stream()
				.filter(a -> actionDe.equals(a.actionDe)).findFirst();
		return action.isPresent() ? action.get() : null;
	}

}
