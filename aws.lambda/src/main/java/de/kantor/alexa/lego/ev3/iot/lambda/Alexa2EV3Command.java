package de.kantor.alexa.lego.ev3.iot.lambda;



/**
 * command for EV3 robot
 * 
 * @author kantor
 *
 */
public class Alexa2EV3Command {
    private String action;

    private String value;

    public Alexa2EV3Command(String action, String value) {
        super();
        this.action = action;
        this.value = value;
    }

    public String getAction() {
        return action;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "EV3Command [action=" + action + ", value=" + value + "]";
    }

	public String toJson() {
		// TODO Auto-generated method stub
		return "{ \"action\": " + "\"" + action + "\", \"value\": " + "\"" + value + "\" }";
	}

}
