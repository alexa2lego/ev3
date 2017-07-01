'use strict';


var aws = require('aws-sdk');
var ev3dev = require('ev3dev-lang');
var awsIot = require('aws-iot-device-sdk');
var path = require('path');
var ev3ThingName = 'EV3Arm';
var battery = new ev3dev.PowerSupply();
var update_state_interval_ms= 120000;
var maximum_reconnect_time_ms=8000;
var aws_iot_connection_debug=false;
var clientTokenUpdate;
var AWSConfiguration = require('./aws_config.js');


var BatteryInfo = (function () {
    function BatteryInfo(technology, batteryType, measuredCurrent, currentAmps, measuredVoltage, voltageVolts, maxVoltage, minVoltage) {
        this.technology = technology;
        this.batteryType = batteryType;
        this.measuredCurrent = measuredCurrent;
        this.currentAmps = currentAmps;
        this.measuredVoltage = measuredVoltage;
        this.voltageVolts = voltageVolts;
        this.maxVoltage = maxVoltage;
        this.minVoltage = minVoltage;
    }
    BatteryInfo.prototype.batteryInfo = function () {
        return {
            "Technology": this.technology,
            "Type": this.batteryType,
            "CurrentMicroamps": this.measuredCurrent,
            "CurrentAmps": this.currentAmps,
            "VoltageMicrovolts": this.measuredVoltage,
            "VoltageVolts": this.voltageVolts,
            "MaxVoltage": this.maxVoltage,
            "MinVoltage": this.minVoltage
        };
    };
    return BatteryInfo;
}());


var shadow = awsIot.thingShadow({
    keyPath: path.join(__dirname, './certs/private.pem.key'),
    certPath: path.join(__dirname, './certs/certificate.pem.crt'),
    caPath: path.join(__dirname, './certs/root-CA.crt'),
    region: AWSConfiguration.aws_region,
    host: AWSConfiguration.aws_iot_host,
    clientId: ev3ThingName,
    maximumReconnectTimeMs: maximum_reconnect_time_ms,
    protocol: 'mqtts',
    debug: aws_iot_connection_debug,
    accessKeyId: '',
    secretKey: '',
    sessionToken: ''
});


module.exports = {

    setupThingShadow: function () {

        shadow.on('connect', function () {
            shadow.register(ev3ThingName, {}, function () {
                console.log("ev3 thing shadow is registered");
            });
        });

        shadow.on('timeout', function () {
            console.log('timeout');
        });

        shadow.on('status', function (thingName, stat, clientToken, stateObject) {
            console.log("thingName: " + thingName + "\n stat: " + stat + "\n clientToken: " + clientToken + "\n stateObject: " + JSON.stringify(stateObject));
        });

        setInterval(function () {
            var batteryInfo = new BatteryInfo(
                battery.technology,
                battery.type,
                battery.measuredCurrent,
                battery.currentAmps,
                battery.measuredVoltage,
                battery.voltageVolts,
                battery.maxVoltage,
                battery.minVoltage);

            var batteryState = {
                "battery": batteryInfo
            };
            var stateShadow = {
                "state": {
                    "reported": batteryState
                }
            };

            var params = {
                "thingName": ev3ThingName,
                "payload": JSON.stringify(stateShadow)
            };

            console.log('\n Updating Shadow:\n', JSON.stringify(params));
            clientTokenUpdate = shadow.update(ev3ThingName, stateShadow);
            if (clientTokenUpdate === null) {
                console.log('update shadow failed, operation still in progress');
            } else {
                console.log('update shadow successfully: ' + clientTokenUpdate);
            }

        }, update_state_interval_ms);
    }
};


