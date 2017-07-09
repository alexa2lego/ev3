package de.kantor.alexa.lego.ev3.iot.lambda;

import java.util.HashMap;
import java.util.Map;

public class Ev3Thing {

	public State state = new State();

	public static class State {

		public Reported reported = new Reported();
		public Desired desired = new Desired();
	}

	public static class Reported {
		public Map<String, String> battery = new HashMap<>();
		public String pin = "";
	}

	public static class Desired {
		public Command command = new Command();
	}

	public static class Command {
		public String action = "";
		public String value = "";
	}

}
