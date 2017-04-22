from enum import Enum
import json
import shlex
import socket
import subprocess
import sys
import os
from time import sleep

from kafka import KafkaConsumer, KafkaProducer


DEBUG = True

SOCKET_HOST = '192.168.1.50'
SOCKET_PORT = 80

NAME = ""

KAFKA_HOST = '192.168.1.50:9092'
KAFKA_TOPIC = "test"
KAFKA_JSON_ENCODING = 'utf-8'
KAFKA_CLIENT_ID = NAME
KAFKA_GROUP_ID = NAME
KAFKA_CONSUMER_TIMEOUT = 10000
KAFKA_SEND_RETRIES = 10
KAFKA_RETRY_BACKOFF = 20000

MSG_SOURCE = 'source'
MSG_TARGET = 'target'
MSG_TYPE = 'type'
MSG_LOAD = 'load'
MSG_APPLICATION_NAME = 'applicationName'
MSG_APPLICATION_VERSION = 'applicationVersion'
MSG_APPLICATION_COMMANDS = 'deploymentCommands'

# STATUS_MSG_SOURCE = 'source'
# STATUS_MSG_LOAD = 'load'
# STATUS_MSG_APPLICATIONS = 'applications'
# STATUS_MSG_TIMESTAMP = 'timestamp'
# 
# COMMAND_MSG_SOURCE = 'source'
# COMMAND_MSG_TARGET = 'target'
# COMMAND_MSG_TYPE = 'commandType'
# COMMAND_MSG_APPLICATION = 'application'
# COMMAND_MSG_TIMESTAMP = 'timestamp'
# 
# APPLICATION_NAME = 'name'
# APPLICATION_VERSION = 'version'
# APPLICATION_COMMAND = 'command'

TARGET_PROCESS_ROOT_DIR = '/home/pi/'

applications = []
appsToPids = {}

## ---------------------
# #     Main
## ---------------------




def main():
    global NAME 
    NAME = getLastFourOfLocalIP()
    KAFKA_CLIENT_ID = NAME
    KAFKA_GROUP_ID = NAME
    
    global applications
    
    consumer = KafkaConsumer(KAFKA_TOPIC, bootstrap_servers=KAFKA_HOST, client_id=KAFKA_CLIENT_ID, group_id=KAFKA_GROUP_ID, consumer_timeout_ms=KAFKA_CONSUMER_TIMEOUT)
    producer = KafkaProducer(bootstrap_servers=KAFKA_HOST, value_serializer=lambda v: json.dumps(v).encode(KAFKA_JSON_ENCODING), retries=KAFKA_SEND_RETRIES, retry_backoff_ms=KAFKA_RETRY_BACKOFF)
    
    # Produce Awake Message
    producer.send(KAFKA_TOPIC, buildStatusMessage())
    print("Sent First Message to Topic: " + KAFKA_TOPIC)
    
    # Listen For Deployment Command
    while (True):
        for msg in consumer:
            handleInboundMessage(msg)
        monitorApplications(producer)
        producer.send(KAFKA_TOPIC, buildStatusMessage())        

    producer.close()
    consumer.close()

def handleInboundMessage(msg):
    try:
        body = json.loads(msg.value.decode(KAFKA_JSON_ENCODING))
        if (MSG_TARGET in body and body[MSG_TARGET] == NAME):
            debug(body)
            if (MSG_TYPE in body and body[MSG_TYPE] == MessageType.DEPLOY.value):
                deployApplication(buildApplicationFromBody(body))
            elif (MSG_TYPE in body and body[MSG_TYPE] == MessageType.UNDEPLOY.value):
                undeployApplication(buildApplicationFromBody(body))
            elif (MSG_TYPE in body and body[MSG_TYPE] == MessageType.REBOOT.value):
                reboot()
            elif (MSG_TYPE in body and body[MSG_TYPE] == MessageType.SHUTDOWN.value):
                reboot()
    except:
        print("Unexpected error:", sys.exc_info()[0])

def monitorApplications(producer):
    global applications
    for app in applications:
        if not app.isRunning():
            undeployApplication(app)
            producer.send(KAFKA_TOPIC, buildFailedMessage(app.name, app.version))

## ---------------------
# #    Logging
## ---------------------
def debug(msg):
    if(DEBUG):
        print(msg)

## ---------------------
# #    IP Config
## ---------------------   
def getLocalIPLastValue():
    ipLastDigits = socket.gethostbyname(socket.gethostname())
    ipLastDigits = ipLastDigits.split('.')[3]
    return ipLastDigits


def getLocalIPLastValueViaSocketConnection():
    soc = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    soc.connect((SOCKET_HOST, SOCKET_PORT))
    ipLastDigits = soc.getsockname()[0]
    ipLastDigits = ipLastDigits.split('.')[3]
    return ipLastDigits

def getLastFourOfLocalIP():
    group = getLocalIPLastValue()
    debug("Group from Local: " + group)
    if not group or group == '1':
        group = getLocalIPLastValueViaSocketConnection()
        debug("Group from Socket: " + group)
    if not group or group == '1':
        raise ValueError("Couldn't find LAN Ip")
    return group


## ---------------------
# #     Messaging
## ---------------------
class MessageType(Enum):
    STATUS = "STATUS"
    DEPLOY = "DEPLOY"
    UNDEPLOY = "UNDEPLOY"
    FINISHED = "FINISHED"
    FAILED = "FAILED"
    REBOOT = "REBOOT"
    SHUTDOWN = "SHUTDOWN"
    
def buildStatusMessage():
    statusMessage = {}
    statusMessage[MSG_SOURCE] = NAME
    statusMessage[MSG_LOAD] = len(applications)
    
    debug("StatusMessage: " + json.dumps(statusMessage))
    return statusMessage

def buildFinishedMessage(appName, appVersion):
    finishedMessage = {}
    finishedMessage[MSG_SOURCE] = NAME
    finishedMessage[MSG_APPLICATION_NAME] = appName
    finishedMessage[MSG_APPLICATION_VERSION] = appVersion
    
    debug("FinishedMessage: " + json.dumps(finishedMessage))
    return finishedMessage

def buildFailedMessage(appName, appVersion):
    failedMessage = {}
    failedMessage[MSG_SOURCE] = NAME
    failedMessage[MSG_APPLICATION_NAME] = appName
    failedMessage[MSG_APPLICATION_VERSION] = appVersion
    
    debug("FailedMessage: " + json.dumps(failedMessage))
    return failedMessage


## ---------------------
# #     Application 
## ---------------------
class Application(object):
    def __init__(self, name, version, command):
        self.name = name
        self.version = version
        self.command = command
        self.pid = None
        
    def equals(self, other):
        if self.name == other.name:
            return True
        return False
    
    def getKey(self):
        return self.name
    
    def toString(self):
        return "" + self.name + ":" + self.version + ":" + self.command + ":" + str(self.pid)
    
    def isRunning(self):
        return isProcessRunning(self)

def buildApplicationFromBody(body):
    return Application(body[MSG_APPLICATION_NAME], body[MSG_APPLICATION_VERSION], body[MSG_APPLICATION_COMMANDS])

## ---------------------
# #     Deployment
## ---------------------
def isAppAlreadyDeployed(app):
    for deployedApp in applications:
        if(deployedApp.equals(app)):
            return True
    return False

def deployApplication(app):
    if isAppAlreadyDeployed(app):
        undeployApplication(app)
        
    launchApplication(app)
    
    global applications
    applications.append(app)
    
    debug("Deployed: " + app.toString())

def undeployApplication(app):    
    if isAppAlreadyDeployed(app):
        if app.pid is not None:
            killApplication(app)
            global applications
            applications.remove(app)
            debug("Killing: " + app.toString())
        else:
            debug("Something is wrong with app's pid: " + app.toString())
    else:
        debug("Could not undeploy app: " + app.toString())

def launchApplication(app):
    commands = shlex.split(app.command)
    return executeProcessForPid(commands)

def killApplication(app):
    commands = shlex.split("kill -9 " + str(app.pid))
    return executeProcessForPid(commands)

def reboot():
    commands = shlex.split("reboot")
    return executeProcessForPid(commands)

def shutdown():
    commands = shlex.split("shutdown now")
    return executeProcessForPid(commands)
    
    
## ---------------------
# #     Unix
## --------------------- 
# def executeProcessForPid(commands):
#     return subprocess.Popen(commands, shell=True).pid;    
#
# def isProcessRunning(app):
#     try:
#         os.kill(app.pid, 0)
#         return True
#     except OSError:
#         return False

## ---------------------
# #     Test
## ---------------------
def executeProcessForPid(commands):
    debug(commands)
    return 8888

def isProcessRunning(app):
    return True

main()
