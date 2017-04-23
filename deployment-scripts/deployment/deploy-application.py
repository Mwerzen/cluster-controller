import requests
import json
import sys

def main():
    command = {}
    command['applicationName'] = "DummyService"
    command['applicationVersion'] = "6.0"
    command['applicationCommand'] = "/home/pi/apps/getAndExec.sh DummyService.sh"
    command['replicationFactor'] = 3
    command['keepOldVersions'] = False;

    resp = requests.post('http://192.168.1.50:8081/deploy', json=command)
    
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
