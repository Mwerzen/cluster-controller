import requests
import json
import sys

def main():
    command = {}
    command['applicationName'] = "test"
    command['applicationVersion'] = "4.0"
    command['applicationCommand'] = "/home/pi/apps/getAndExec.sh testDeployment.sh"
    command['replicationFactor'] = 1
    command['keepOldVersions'] = False;

    resp = requests.post('http://192.168.1.50:8080/deploy', json=command)
    
    if resp.status_code == 200:
        print("Deployed Successfully")
        sys.exit(0)
    elif resp.status_code == 503:
        print("Cluster Overwhelmed")
        sys.exit(1)
    else:
        print("Deployment Invalid")
        sys.exit(1)
    
    
    
    
main()
