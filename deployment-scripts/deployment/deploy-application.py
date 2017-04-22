import requests
import json
import sys

def main():
    command = {}
    command['applicationName'] = "PewPew"
    command['applicationVersion'] = "8.0"
    command['applicationCommand'] = "./java -jar danker"
    command['replicationFactor'] = 3
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
