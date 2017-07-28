'use strict';

var ev3dev = require('ev3dev-lang');

var minAltitude = 80;

var motorA = new ev3dev.Motor(ev3dev.OUTPUT_A); //  motor for moving right or left
var motorB = new ev3dev.Motor(ev3dev.OUTPUT_B); // motor for touching
var motorC = new ev3dev.Motor(ev3dev.OUTPUT_C); // up, down motor
var motorD = new ev3dev.Motor(ev3dev.OUTPUT_D); // not using

var touchSensor1 = new ev3dev.TouchSensor(ev3dev.INPUT_1); // stop sensor for motor A
var touchSensor2 = new ev3dev.TouchSensor(ev3dev.INPUT_2); // stop sensor for motor C
var ultraSensor = new ev3dev.UltrasonicSensor(ev3dev.INPUT_3);
var gyroSensor = new ev3dev.GyroSensor(ev3dev.INPUT_4);

var battery = new ev3dev.PowerSupply();

var cancellationMotorA;
var cancellationMotorC;
var angle;


function initMotors() {
    if (!motorA.connected) {
        console.error("No motor was found on port A. Please connect a tacho motor to port A and try again.");

    }
    if (!motorB.connected) {
        console.error("No motor was found on port B. Please connect a tacho motor to port B and try again.");

    }
    if (!motorC.connected) {
        console.error("No motor was found on port C. Please connect a tacho motor to port C and try again.");

    }
}

function initSensors() {
    if (!touchSensor1.connected) {
        console.error("No touch sensor could be found on port 1! Please verify that a touch sensor is plugged in on port 1 and try again.");

    }
    if (!touchSensor2.connected) {
        console.error("No touch sensor could be found on port 2! Please verify that a touch sensor is plugged in on port 2 and try again.");

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
        if (ultraInfo.lastValue < minAltitude) {
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
        if (ultraInfo.lastValue < -angle) {
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

function getMotorsInfo() {
    return {
        "Motor A": getMotorInfo(motorA),
        "Motor B": getMotorInfo(motorB),
        "Motor C": getMotorInfo(motorC),
        "Motor D": getMotorInfo(motorD),
    };
}

function getSensorsInfo() {
    return {
        "Sensor 1": getSensorInfo(touchSensor1),
        "Sensor 2": getSensorInfo(touchSensor2),
        "Sensor 3": getSensorInfo(ultraSensor),
        "Sensor 4": getSensorInfo(gyroSensor),
    };
}

function getMotorInfo(motor) {
    if (motor.connected) {
        return {
            "driverName": motor.driverName,
            "status": "connected"
        };
    } else {
        return {
            "status": "disconnected"
        };
    }
}

function getSensorInfo(sensor) {
    if (sensor.connected) {
        return {
            "driverName": sensor.driverName,
            "status": "connected"
        };
    } else {
        return {
            "status": "disconnected"
        };
    }

}


function gyroReset() {
    angle = 0;
    gyroSensor = new ev3dev.GyroSensor(ev3dev.INPUT_4);
}

function stopMotor(motor) {
    if (motor.isRunning) {
        motor.sendCommand(motor.commandValues.stop);
        console.log("motor stopped");
    }
}

function getBattetyInfo() {
    return {
        "measuredCurrent": battery.measuredCurrent,
        "currentAmps": battery.currentAmps,
        "measuredVoltage": battery.measuredVoltage,
        "voltageVolts": battery.voltageVolts,
        "maxVoltage": battery.maxVoltage,
        "minVoltage": battery.minVoltage
    }
}


module.exports = {

    doSetup: function () {
        initMotors();
        initSensors();
        gyroReset();
        console.log("robot is ready!");
    },


    doArmRight: function () {
        if (!touchSensor1.isPressed) {
            console.log("turning right ");
            cancellationMotorA = setInterval(function () {
                motorA.start(motorA.maxSpeed, motorA.stopActionValues.brake);
            }, 100);
        } else {
            console.log("cannot turn right");
        }
    },


    doArmLeft: function (anAngle) {
        if (anAngle !== null) {
            console.log("turning left " + anAngle + " grad");
            angle = anAngle;
            gyroReset();
            cancellationMotorA = setInterval(function () {
                motorA.start(-motorA.maxSpeed, motorA.stopActionValues.brake);
            }, 100);
        }
        else {
            console.log("turning left");
            motorA.runForDistance(-180, motorA.maxSpeed, motorA.stopActionValues.brake);
        }
    },

    doArmUp: function () {
        if (!touchSensor2.isPressed) {
            console.log("moving up");
            cancellationMotorC = setInterval(function () {
                motorC.start(-motorC.maxSpeed, motorC.stopActionValues.brake);
            }, 100);
        } else {
            console.log("cannot move up");
        }
    },

    doArmDown: function () {
        if (ultraSensor.getValue(0) >= minAltitude) {
            console.log("moving down");
            cancellationMotorC = setInterval(function () {
                motorC.start(motorC.maxSpeed, motorC.stopActionValues.brake);
            }, 100);
        } else {
            console.log("cannot move down. Ultrasensor value: " + ultraSensor.getValue(0));
        }
    },


    doArmCatch: function () {
        console.log("catching");
        motorB.runForDistance(-90, motorB.maxSpeed, motorB.stopActionValues.brake);
    },

    doArmRelease: function () {
        console.log("releasing");
        motorB.runForDistance(90, motorB.maxSpeed, motorB.stopActionValues.brake);
    },

    doStopMotors: function () {
        stopMotor(motorA);
        stopMotor(motorB);
        stopMotor(motorC);
    },

    getMotorsState: function () {
        return getMotorsInfo();
    },

    getSensorsState: function () {
        return getSensorsInfo();
    },

    getBatteryState: function () {
        return getBattetyInfo();
    },

    getCommands: function () {
        return [
            {"RIGHT": "drehe rechts {arc} [Grad] , drehe rechts"},
            {"LEFT": "drehe links {arc} [Grad], drehe links"},
            {"DOWN": "fahre runter"},
            {"UP": "fahre hoch"},
            {"CATCH": "greife es"},
            {"RELEASE": "öffne es , lasse los"}
        ]
    }
};
