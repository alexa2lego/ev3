package de.kantor.alexa.lego.ev3.iot.lambda;

import java.util.Map;

public class Ev3Device {
	private String serialNumber;
	private String deviceName;
	private String aliasName;
	private Map<String, String> commands;

	public Ev3Device(String serialNumber, String deviceName, String aliasName) {
		super();
		this.serialNumber = serialNumber;
		this.deviceName = deviceName;
		this.aliasName = aliasName;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getAliasName() {
		return aliasName;
	}

	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	public Map<String, String> getCommands() {
		return commands;
	}

	public void setCommands(Map<String, String> commands) {
		this.commands = commands;
	}

	@Override
	public String toString() {
		return "Ev3Device [serialNumber=" + serialNumber + ", deviceName=" + deviceName + ", aliasName=" + aliasName
				+ ", commands=" + commands + "]";
	}

}
