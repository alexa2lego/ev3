package de.kantor.alexa.lego.ev3.iot.lambda;

import java.util.HashMap;
import java.util.Map;

public class Ev3ThingState {

	public State state = new State();

	public static class State {

		public Battery reported = new Battery();

		@Override
		public String toString() {
			return "State [reported=" + reported + "]";
		}

	}

	public static class Battery {
		public Map<String, String> battery = new HashMap<>();

		@Override
		public String toString() {
			return battery.toString();
		}

	}

	@Override
	public String toString() {
		return "Ev3ThingState [state=" + state + "]";
	}

}
