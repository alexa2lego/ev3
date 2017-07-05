'use strict';


var aws = require('aws-sdk');
var ev3dev = require('ev3dev-lang');
var awsIot = require('aws-iot-device-sdk');
var path = require('path');
var ev3ThingName = 'EV3Arm';
var battery = new ev3dev.PowerSupply();
var update_state_interval_ms = 60000;
var maximum_reconnect_time_ms = 8000;
var aws_iot_connection_debug = false;
var AWSConfiguration = require('./aws_config.js');
var pin;

var BatteryInfo = (function () {
    function BatteryInfo(technology, batteryType, measuredCurrent, currentAmps, measuredVoltage, voltageVolts, maxVoltage, minVoltage) {
        this.measuredCurrent = measuredCurrent;
        this.currentAmps = currentAmps;
        this.measuredVoltage = measuredVoltage;
        this.voltageVolts = voltageVolts;
        this.maxVoltage = maxVoltage;
        this.minVoltage = minVoltage;
    }

    BatteryInfo.prototype.batteryInfo = function () {
        return {
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

        function generatePin(){
            pin = Math.floor(10000 + Math.random() * 90000);
            console.log("PIN: " + pin);
        }

        shadow.on('connect', function () {
                generatePin();
                initiateState();
                console.log("ev3 thing shadow is initialized");
        });

        shadow.on('timeout', function () {
            console.log('timeout');
        });

        shadow.on('close', function () {
            shadow.unregister(ev3ThingName);
            console.error('close connection' );
        });

        shadow.on('error', function (err) {
            console.error('error:' + err);
        });

        shadow.on('status', function (thingName, stat, clientToken, stateObject) {
            console.log("thingName: " + thingName + "\n stat: " + stat + "\n clientToken: " + clientToken + "\n stateObject: " + JSON.stringify(stateObject));
        });

        var batteryInfo = new BatteryInfo(
            battery.measuredCurrent,
            battery.currentAmps,
            battery.measuredVoltage,
            battery.voltageVolts,
            battery.maxVoltage,
            battery.minVoltage);

        var deviceState = {
            "pin": pin,
            "battery": batteryInfo
        };
        var stateShadow = {
            "state": {
                "reported": deviceState
            }
        };

        var params = {
            "thingName": ev3ThingName,
            "payload": JSON.stringify(stateShadow)
        };

        function updateState() {
            var clientTokenUpdate = shadow.update(ev3ThingName, stateShadow);
            if (clientTokenUpdate === null) {
                console.log('update shadow failed, operation still in progress');
            } else {
                console.log('update shadow successfully: ' + clientTokenUpdate);
            }
        }

        function initiateState() {
            shadow.register(ev3ThingName, {
                persistentSubscribe: false,
                enableVersioning: false
            }, function () {
                updateState();
            });
        }

        setInterval(function () {
            console.log('\n Updating Shadow:\n', JSON.stringify(params));
            updateState();
        }, update_state_interval_ms);
    }
};


