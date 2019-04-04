# Cluster Controller

A prototype cluster system built to teach me more about the key design details involved in modern Container and Cluster platforms. The idea was to run a master cluster controller + jenkins on a lightweight, low power Intel J3355 server and then connect a series of Raspberry Pi's running the cluster slave software hosting the apps.

## Functionality
- Cluster Master maintains a log of deployed applications.
- New jobs can be submitted to the cluster master via Webservice.
- Jobs can set a replication factor (number of instances to run on).
- Jobs can set whether to keep old instances or kill old instances.
- Jobs are load-balanced between available slaves.
- Slaves monitor application health, rebooting job if it fails.
- Master monitors slave health, retriggering a load balance if the number of slaves changes.
- Integration between master and slaves is maintained via Kafka.
