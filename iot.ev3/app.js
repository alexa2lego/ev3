'use strict';

var awsIot = require('aws-iot-device-sdk');
var path = require('path');
var ev3ThingName = 'EV3Arm';

var awsConfig = require('./aws_config.js');
var devicePin;
var robot = require('./robot');

var shadow = awsIot.thingShadow({
    keyPath: path.join(__dirname, './certs/private.pem.key'),
    certPath: path.join(__dirname, './certs/certificate.pem.crt'),
    caPath: path.join(__dirname, './certs/root-CA.crt'),
    region: awsConfig.aws_region,
    host: awsConfig.aws_iot_host,
    clientId: ev3ThingName,
    maximumReconnectTimeMs: 10000,
    protocol: 'mqtts',
    debug: false,
    accessKeyId: '',
    secretKey: '',
    sessionToken: ''
});


function generatePin() {
    var pinNumber = Math.floor(10000 + Math.random() * 90000);
    console.log("PIN: " + pinNumber);
    return pinNumber;
}

shadow.on('connect', function () {
    registerThing();
    console.log("shadow initialized");
});

shadow.on('status', function(thingName, stat, clientToken, stateObject) {
    handleStatus(thingName, stat, clientToken, stateObject);
});

shadow.on('delta', function(thingName, stateObject) {
    handleDelta(thingName, stateObject);
});


shadow.on('timeout', function () {
    console.log('shadow has timeout');
});

shadow.on('close', function () {
    shadow.unregister(ev3ThingName);
    console.error('shadow connection closed');
});

shadow.on('error', function (err) {
    console.error('shadow error:' + err);
});

shadow.on('status', function (thingName, stat, clientToken, stateObject) {
    console.log("shadow stat: " + stat + "\n stateObject: " + JSON.stringify(stateObject));
});

function generateState() {
    return {
        state: {
            reported: {
                "pin": devicePin
            }
        }
    };
}


function updateState() {

    var clientTokenUpdate = shadow.update(ev3ThingName, generateState());

    if (clientTokenUpdate === null) {
        console.log('update shadow failed, operation still in progress');
    } else {
        console.log('update shadow successfully: ' + clientTokenUpdate);
    }

}

function registerThing() {
    devicePin = generatePin();
    shadow.register(ev3ThingName, {
        ignoreDeltas: false
    }, function () {
        updateState();
    })
}


function handleDelta(thingName, stateObject) {

    handleMessage(JSON.stringify(stateObject.state.command));
}

function handleStatus(thingName, stat, clientToken, stateObject) {
    console.log('got \'' + stat + '\' status on: ' + thingName);
}

function handleMessage  (message) {
    var msg = JSON.parse(message);
    var action = msg.action;
    var value = msg.value;
    console.log("action " + action + ", value: " + value);

    switch (action) {
        case "RIGHT":
            robot.doArmRight(value);
            break;
        case "LEFT":
            robot.doArmLeft(value);
            break;
        case "CATCH":
            robot.doArmCatch();
            break;
        case "RELEASE":
        case "OPEN":
            robot.doArmRelease();
            break;
        case "STOP":
            robot.doStopMotors();
            break;
        case "UP":
            robot.doArmUp();
            break;
        case "DOWN":
            robot.doArmDown();
            break;
        case "FORWARDS":
        case "BACKWARDS":

            break;
        default:
            console.log("can't do!");
    }
}

robot.setup();