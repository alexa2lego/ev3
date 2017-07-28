'use strict';

var awsIot = require('aws-iot-device-sdk');
var path = require('path');


var config = require('./config.js');
var crane = require('./crane');
crane.doSetup();

var shadow = awsIot.thingShadow({
    keyPath: path.join(__dirname, './certs/private.pem.key'),
    certPath: path.join(__dirname, './certs/certificate.pem.crt'),
    caPath: path.join(__dirname, './certs/root-CA.crt'),
    region: config.aws_region,
    host: config.aws_iot_host,
    clientId: config.thingName,
    maximumReconnectTimeMs: 10000,
    protocol: 'mqtts',
    debug: false,
    accessKeyId: '',
    secretKey: '',
    sessionToken: ''
});


shadow.on('connect', function () {
    registerThing();
    console.log("shadow initialized");
});

shadow.on('message', function (topic, payload) {
    handleMessage(topic, payload);
});


shadow.on('timeout', function () {
    console.log('shadow has timeout');
});

shadow.on('close', function () {
    shadow.unregister(config.thingName);
    console.error('shadow connection closed');
});

shadow.on('error', function (err) {
    console.error('shadow error:' + err);
});


function registerThing() {
    shadow.register(config.thingName, {}, function () {
        onRegisterThing();
    });
    shadow.subscribe(config.thingName);
}


function onRegisterThing() {

    var updateToken = shadow.update(config.thingName, reportState());

    if (updateToken === null) {
        console.log('update shadow failed, operation still in progress');
    } else {
        console.log('update shadow successfully: ' + updateToken);
    }
}

function handleMessage(topic, message) {
    console.log('got \'' + message + '\' on: ' + topic)
    runCommand(message);
}

function reportState() {
    return {
        state: {
            reported:
                {
                    device: {
                        deviceName: "Kran",
                        serialNumber: "abc12345Z",
                        status: {
                            "motors": crane.getMotorsState(),
                            "sensors": crane.getSensorsState(),
                            "battery": crane.getBatteryState()
                        },
                        commands: crane.getCommands()
                    }
                }

        }
    };
}

function runCommand(command) {
    var cmd = JSON.parse(command);
    var action = cmd.action;
    var value = cmd.value;
    console.log("action " + action + ", value: " + value);
    if (action !== null && action != '') {
        switch (action) {
            case "RIGHT":
                crane.doArmRight(value);
                break;
            case "LEFT":
                crane.doArmLeft(value);
                break;
            case "CATCH":
                crane.doArmCatch();
                break;
            case "RELEASE":
            case "OPEN":
                crane.doArmRelease();
                break;
            case "STOP":
                crane.doStopMotors();
                break;
            case "UP":
                crane.doArmUp();
                break;
            case "DOWN":
                crane.doArmDown();
                break;
            default:
                console.log("can't do!");
        }
    }
}
