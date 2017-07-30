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

	@Override
	public void onSuccess() {
		// TODO Auto-generated method stub
		super.onSuccess();
	}

	@Override
	public void onFailure() {
		// TODO Auto-generated method stub
		super.onFailure();
	}

	@Override
	public void onTimeout() {
		// TODO Auto-generated method stub
		super.onTimeout();
	}

}
