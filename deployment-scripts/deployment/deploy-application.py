import requests
import json
import sys

def main():
    command = {}
    command['applicationName'] = "SleepAll"
    command['applicationVersion'] = "3.0"
    command['applicationCommand'] = "python sleepandprint.py"
    command['replicationFactor'] = 1
    command['keepOldVersions'] = False;

    resp = requests.post('http://localhost:8080/deploy', json=command)
    
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
