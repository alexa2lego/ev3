/*
 * configuration parameters for using AWS services
 */
var awsConfiguration = {

    aws_ev3_queue: 'ADD HIER YOUR QUEUE URI such as https://sqs.eu-west-1.amazonaws.com/XXXXXXXXX/EV3Queue',
    aws_region: 'ADD YOUR AWS REGION',
    aws_accessKeyId: 'ADD HIER YOUR ACCESS KEY ID',
    aws_secretAccessKey: 'ADD HUER YOUR SECRET ACCESS KEY',
    aws_iot_host: 'ADD HIER YOUR IOT ENDPOINT such as YYYYYYYYYY.iot.eu-west-1.amazonaws.com'

};
module.exports = awsConfiguration;