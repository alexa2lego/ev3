'use strict';

var awsIot = require('aws-iot-device-sdk');
var path = require('path');
var ev3ThingName = 'EV3Arm';

var aws_iot_connection_debug = false;
var awsConfig = require('./aws_config.js');

var device = awsIot.device({
    keyPath: path.join(__dirname, './certs/private.pem.key'),
    certPath: path.join(__dirname, './certs/certificate.pem.crt'),
    caPath: path.join(__dirname, './certs/root-CA.crt'),
    region: awsConfig.aws_region,
    host: awsConfig.aws_iot_host,
    clientId: ev3ThingName,
    debug: aws_iot_connection_debug
});


module.exports = {

    setupThingDevice: function () {

        device.on('connect', function () {
            console.log('device connected');
            device.subscribe(awsConfig.aws_ev3_topic);
        });
        device.on('timeout', function () {
            console.log('device has timeout');
        });

        device.on('close', function () {

            console.error('device connection closed');
        });

        device.on('error', function (err) {
            console.error('device error:' + err);
        });


        return device;
    }

};

