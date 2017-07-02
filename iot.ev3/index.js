'use strict';

var ev3dev = require('ev3dev-lang');
var Consumer = require('sqs-consumer');
var AWS = require('aws-sdk');
var iot = require('./iot');
var AWSConfiguration = require('./aws_config.js');

var minAltitude  = 80;

var motorA = new ev3dev.Motor(ev3dev.OUTPUT_A);
var motorB = new ev3dev.Motor(ev3dev.OUTPUT_B);
var motorC = new ev3dev.Motor(ev3dev.OUTPUT_C);
var touchSensor1 = new ev3dev.TouchSensor(ev3dev.INPUT_1);
var touchSensor2 = new ev3dev.TouchSensor(ev3dev.INPUT_2);
var ultraSensor = new ev3dev.UltrasonicSensor(ev3dev.INPUT_3);
var gyroSensor = new ev3dev.GyroSensor(ev3dev.INPUT_4);

var cancellationMotorA;
var cancellationMotorC;
var angle;


AWS.config.update({
    region: AWSConfiguration.aws_region,
    accessKeyId: AWSConfiguration.aws_accessKeyId,
    secretAccessKey: AWSConfiguration.aws_secretAccessKey
});

function armSetup() {
    gyroReset();
    doArmUp();
    console.log("ev3 is ready");
}

function doArmRight() {
    if (!touchSensor1.isPressed) {
        console.log("arm is moving right ");
        cancellationMotorA = setInterval(function () {
            motorA.start(300, motorA.stopActionValues.brake);
        }, 100);
    } else {
        console.log("arm cannot move right");
    }
}


function doArmLeft(anAngle) {
    if (anAngle !== null) {
        console.log("arm is moving left " + anAngle + " grad");
        angle = anAngle;
        gyroReset();
        cancellationMotorA = setInterval(function () {
            motorA.start(-200, motorA.stopActionValues.brake);
        }, 100);
    }
    else {
        console.log("arm is moving left");
        motorA.runForDistance(-180, 200, motorA.stopActionValues.brake);
    }
}

function doArmUp() {
    if (!touchSensor2.isPressed) {
        console.log("arm is moving up");
        cancellationMotorC = setInterval(function () {
            motorC.start(-300, motorC.stopActionValues.brake);
        }, 100);
    } else {
        console.log("arm cannot move up");
    }
}

function doArmDown() {
    if (ultraSensor.getValue(0) >= minAltitude ) {
        console.log("arm is moving down");
        cancellationMotorC = setInterval(function () {
            motorC.start(300, motorC.stopActionValues.brake);
        }, 100);
    } else {
        console.log("arm cannot move down. Ultrasensor value: " + ultraSensor.getValue(0));
    }
}


function doArmCatch() {
    console.log("arm is catching");
    motorB.runForDistance(-90, 200, motorB.stopActionValues.brake);
}

function doArmRelease() {
    console.log("arm is releasing");
    motorB.runForDistance(90, 200, motorB.stopActionValues.brake);
}

function gyroReset(){
    angle = 0;
    gyroSensor = new ev3dev.GyroSensor(ev3dev.INPUT_4);
}

function stopMotor(motor) {
    if (motor.isRunning) {
        motor.sendCommand(motor.commandValues.stop);
        console.log("motor stopped");
    }
}

var app = Consumer.create({
    queueUrl: AWSConfiguration.aws_ev3_queue,
    handleMessage: function (message, done) {
        var msg = JSON.parse(JSON.parse(message.Body).Message);
        var action = msg.action;
        var value = msg.value;
        console.log("action " + action + ", value: " + value);

        switch (action) {
            case "RIGHT":
                doArmRight(value);
                break;
            case "LEFT":
                doArmLeft(value);
                break;
            case "CATCH":
                doArmCatch();
                break;
            case "RELEASE":
            case "OPEN":
                doArmRelease();
                break;
            case "STOP":
                stopMotor(motorA);
                break;
            case "UP":
                doArmUp();
                break;
            case "DOWN":
                doArmDown();
                break;
            default:
                console.log("can't do!");
        }
        done();
    },
    messageAttributeNames: [
        "All"
    ],
    VisibilityTimeout: 0,
    WaitTimeSeconds: 0,
    sqs: new AWS.SQS()
});


function initMotors() {
    if (!motorA.connected) {
        console.error("No motor was found on port A. Please connect a tacho motor to port A and try again.");
        process.exit(1);
    }
    if (!motorB.connected) {
        console.error("No motor was found on port B. Please connect a tacho motor to port B and try again.");
        process.exit(1);
    }
    if (!motorC.connected) {
        console.error("No motor was found on port C. Please connect a tacho motor to port C and try again.");
        process.exit(1);
    }
}

function initSensors() {
    if (!touchSensor1.connected) {
        console.error("No touch sensor could be found on port 1! Please verify that a touch sensor is plugged in on port 1 and try again.");
        process.exit(1);
    }
    if (!touchSensor2.connected) {
        console.error("No touch sensor could be found on port 2! Please verify that a touch sensor is plugged in on port 2 and try again.");
        process.exit(1);
    }

    touchSensor1.registerEventCallback(function (error, touchInfo) {
        if (error)
            throw error;
        console.log("Sensor 1 is " + (touchInfo.lastPressed ? "pressed" : "released"));
        if (touchInfo.lastPressed) {
            clearInterval(cancellationMotorA);
            stopMotor(motorA);
        }
    }, function (userData) {
        var isPressed = touchSensor1.isPressed;
        var changed = isPressed !== userData.lastPressed;
        userData.lastPressed = isPressed;
        return changed;
    }, false, {lastPressed: false});

    touchSensor2.registerEventCallback(function (error, touchInfo) {
        if (error)
            throw error;
        console.log("Sensor 2 is " + (touchInfo.lastPressed ? "pressed" : "released"));
        if (touchInfo.lastPressed) {
            clearInterval(cancellationMotorC);
            stopMotor(motorC);
        }
    }, function (userData) {
        var isPressed = touchSensor2.isPressed;
        var changed = isPressed !== userData.lastPressed;
        userData.lastPressed = isPressed;
        return changed;
    }, false, {lastPressed: false});

    ultraSensor.registerEventCallback(function (error, ultraInfo) {
        if (error)
            throw error;
        if (ultraInfo.lastValue < minAltitude ) {
            clearInterval(cancellationMotorC);
            stopMotor(motorC);
        }
    }, function (userData) {
        var curValue = ultraSensor.getValue(0);
        var changed = curValue < userData.lastValue;
        userData.lastValue = curValue;
        return changed;
    }, false, {lastValue: ultraSensor.getValue(0)});

    gyroSensor.registerEventCallback(function (error, ultraInfo) {
        if (error)
            throw error;
        if (ultraInfo.lastValue < - angle) {
            console.log("gyro:" + gyroSensor.getValue(0));
            clearInterval(cancellationMotorA);
            stopMotor(motorA);
            gyroReset();
        }
    }, function (userData) {
        var curValue = gyroSensor.getValue(0);
        var changed = curValue < userData.lastValue;
        userData.lastValue = curValue;
        return changed;
    }, false, {lastValue: 0});

}

function on(err) {
    console.log(err.message);
}


iot.setupThingShadow();

initMotors();
initSensors();
armSetup();
app.start();

