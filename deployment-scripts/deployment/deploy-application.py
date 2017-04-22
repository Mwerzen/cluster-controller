import requests
import json
import sys

def main():
    command = {}
    command['applicationName'] = "DankDankDocks"
    command['applicationVersion'] = "1.0"
    command['applicationCommand'] = "./java -jar danker"
    command['replicationFactor'] = 3
    command['keepOldVersions'] = True;

    resp = requests.post('http://localhost:8080/deploy', json=command)
    
    if resp.status_code == 0:
        print("Deployed Successfully")
        sys.exit(0)
    elif resp.status_code == 503:
        print("Cluster Overwhelmed")
        sys.exit(1)
    else:
        print("Deployment Invalid")
        sys.exit(1)
    
    
    
    
main()
