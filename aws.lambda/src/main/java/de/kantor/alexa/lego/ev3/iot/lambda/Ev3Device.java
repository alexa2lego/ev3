package de.kantor.alexa.lego.ev3.iot.lambda;

import java.util.Map;

public class Ev3Device {
	private String serialNumber;
	private String iotName;
	private String deviceName;
	private Map<String, String> commands;

	public Ev3Device(String serialNumber, String iotName, String deviceName) {
		super();
		this.serialNumber = serialNumber;
		this.iotName = iotName;
		this.deviceName = deviceName;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getIotName() {
		return iotName;
	}

	public void setIotName(String iotName) {
		this.iotName = iotName;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public Map<String, String> getCommands() {
		return commands;
	}

	public void setCommands(Map<String, String> commands) {
		this.commands = commands;
	}

	@Override
	public String toString() {
		return "Ev3Device [serialNumber=" + serialNumber + ", deviceName=" + deviceName + ", iotName=" + iotName
				+ ", commands=" + commands + "]";
	}

}
