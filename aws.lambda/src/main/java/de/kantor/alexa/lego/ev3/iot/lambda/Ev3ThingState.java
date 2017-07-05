package de.kantor.alexa.lego.ev3.iot.lambda;

import java.util.HashMap;
import java.util.Map;

public class Ev3ThingState {

	public State state = new State();

	public static class State {

		public Reported reported = new Reported();

		@Override
		public String toString() {
			return "State [reported=" + reported + "]";
		}

	}

	public static class Reported {
		public Map<String, String> battery = new HashMap<>();
		public String pin = "";
		@Override
		public String toString() {
			return "Reported [battery=" + battery + ", pin=" + pin + "]";
		}
				
	}

	@Override
	public String toString() {
		return "Ev3ThingState [state=" + state + "]";
	}

}
