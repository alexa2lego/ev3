package de.kantor.alexa.lego.ev3.iot.lambda;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

public class Alexa2Ev3IotMessage extends AWSIotMessage {

    public Alexa2Ev3IotMessage(String topic, AWSIotQos qos) {
        super(topic, qos);
    }

    public Alexa2Ev3IotMessage(String topic, AWSIotQos qos, byte[] payload) {
        super(topic, qos, payload);
    }

    public Alexa2Ev3IotMessage(String topic, AWSIotQos qos, String payload) {
        super(topic, qos, payload);

    }

}
