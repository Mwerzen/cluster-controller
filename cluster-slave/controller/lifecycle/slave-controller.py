from kafka import KafkaConsumer, KafkaProducer
from random import randint
from enum import Enum
import socket
import json
from time import sleep
import subprocess
from subprocess import Popen
import shlex
import datetime
import os.path
from _datetime import date
from distutils.command.build import build

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

STATUS_MSG_SOURCE = 'source'
STATUS_MSG_LOAD = 'load'
STATUS_MSG_APPLICATIONS = 'applications'
STATUS_MSG_TIMESTAMP = 'timestamp'

COMMAND_MSG_SOURCE = 'source'
COMMAND_MSG_TARGET = 'target'
COMMAND_MSG_TYPE = 'commandType'
COMMAND_MSG_APPLICATION = 'application'
COMMAND_MSG_TIMESTAMP = 'timestamp'

APPLICATION_NAME = 'name'
APPLICATION_VERSION = 'version'
APPLICATION_COMMAND = 'command'

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
            body = json.loads(msg.value.decode(KAFKA_JSON_ENCODING))
            if(COMMAND_MSG_TARGET in body and body[COMMAND_MSG_TARGET] == NAME):
                debug(body)
                if (COMMAND_MSG_TYPE in body and body[COMMAND_MSG_TYPE] == SlaveCommand.DEPLOY.value and COMMAND_MSG_APPLICATION in body):
                    deployApplication(body[COMMAND_MSG_APPLICATION])
                elif(COMMAND_MSG_TYPE in body and body[COMMAND_MSG_TYPE] == SlaveCommand.UNDEPLOY.value and COMMAND_MSG_APPLICATION in body):
                    undeployApplication(body[COMMAND_MSG_APPLICATION])
                elif(COMMAND_MSG_TYPE in body and body[COMMAND_MSG_TYPE] == SlaveCommand.REBOOT.value):
                    reboot()     
        producer.send(KAFKA_TOPIC, buildStatusMessage())        

    producer.close()
    consumer.close()



## ---------------------
# #     Helpers
## ---------------------
def debug(msg):
    if(DEBUG):
        print(msg)
        
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
     
class SlaveCommand(Enum):
    DEPLOY = "DEPLOY"
    UNDEPLOY = "UNDEPLOY"
    REBOOT = "REBOOT"
    
def buildApplication(name, version, command):
    application = {}
    application[APPLICATION_NAME] = name
    application[APPLICATION_VERSION] = version
    application[APPLICATION_COMMAND] = command
    return application
    
def buildStatusMessage():
    global applications
    statusMessage = {}
    statusMessage[STATUS_MSG_SOURCE] = NAME
    statusMessage[STATUS_MSG_LOAD] = len(applications)
    statusMessage[STATUS_MSG_TIMESTAMP] = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    statusMessage[STATUS_MSG_APPLICATIONS] = applications
    
    debug("StatusMessage: " + json.dumps(statusMessage))
    return statusMessage

## ---------------------
# #     Deployment
## ---------------------

def isAppAlreadyDeployed(appToCheck):
    for app in applications:
        if(app[APPLICATION_NAME] == appToCheck[APPLICATION_NAME] and app[APPLICATION_VERSION] == appToCheck[APPLICATION_VERSION]):
            return True
    return False


def getKeyForApplication(app):
    return app[APPLICATION_NAME] + "-" + app[APPLICATION_VERSION]

def setPidForApp(app, pid):
    global appsToPids
    appsToPids[getKeyForApplication(app)] = pid
    
def getPidForApp(app):
    global appsToPids
    pid = appsToPids.get(getKeyForApplication(app), None)
    setPidForApp(app, None)
    return pid

def deployApplication(app):
    if isAppAlreadyDeployed(app):
        undeployApplication(app)
    commands = shlex.split("./" + app[APPLICATION_COMMAND]) 
    debug(commands)
    process = subprocess.Popen(commands, shell=True)
    pid = process.pid
    setPidForApp(app, pid)
    global applications
    applications.append(app)
    debug("Deployed: " + app[APPLICATION_NAME] + " pid " + str(pid))

def undeployApplication(app):    
    if isAppAlreadyDeployed(app):
        pid = getPidForApp(app)
        if pid is not None:
            global applications
            applications.remove(app)
            debug("Killing: " + app[APPLICATION_NAME] + " pid " + str(pid))
            subprocess.Popen(["kill", "-9", str(pid)], shell=True)
    else:
        debug("Could not undeploy app: " + app[APPLICATION_NAME])

def reboot():
    debug("Rebooting")
    # process = subprocess.Popen("reboot")

main()
