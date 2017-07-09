'use strict';


var mqtt = require('mqtt');
var mqttConfiguration= require('./mqtt_config');
var client  = mqtt.connect(mqttConfiguration.serverUrl);

module.exports = {

    setupMqttClient: function () {

        client.on('connect', function () {
            console.log("mqtt server connected");
            client.subscribe(mqttConfiguration.topicName);
        });

        client.on('message', function (topic, message) {
            console.log("command message arrived:"+ message.toString());
        });

        client.on('error', function (err) {
            console.log("error:" + err);
        });

        return client;
    },

    sendCommand: function(command){
        console.log("mqtt command published:" + comamnd);
        client.publish(mqttConfiguration.topicName , command );

    }

};

