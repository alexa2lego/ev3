
package de.kantor.alexa.lego.ev3.iot.lambda;

/**
 * This is an exception thrown from the {@link Alexa2EV3SnsClient} that indicates that a failure occurred.
 * 
 * @author kantor
 *
 */
public class Alexa2EV3Exception extends Exception {

    private static final long serialVersionUID = 1L;

    public Alexa2EV3Exception(String message, Exception e) {
        super(message, e);
    }

    public Alexa2EV3Exception(String message) {
        super(message);
    }

    public Alexa2EV3Exception(Exception e) {
        super(e);
    }
}
