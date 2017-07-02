
package de.kantor.alexa.lego.ev3.iot.lambda;

/**
 * This is an exception that indicates that a failure occurred.
 * 
 * @author kantor
 *
 */
public class Alexa2Ev3Exception extends Exception {

    private static final long serialVersionUID = 1L;

    public Alexa2Ev3Exception(String message, Exception e) {
        super(message, e);
    }

    public Alexa2Ev3Exception(String message) {
        super(message);
    }

    public Alexa2Ev3Exception(Exception e) {
        super(e);
    }
}
