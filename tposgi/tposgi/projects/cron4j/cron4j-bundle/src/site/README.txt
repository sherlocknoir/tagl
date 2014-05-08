cron4j deamon bundle
====================

Author: Didier Donsez
This bundle schedules services implementing java.lang.Runnable according to the cron pattern described in the service property "cron4j.pattern"

The demonstration
=================
download and launch Apache Felix framework with java -jar bin\felix.jar
then run the following commands in the Felix shell
 
obr list
obr start "Apache Felix Log Service"
obr start "Apache Felix iPOJO"
obr start "Apache Felix iPOJO White Board Pattern Handler"
obr start "Apache Felix iPOJO Composite"
obr start "Apache Felix iPOJO Arch Command"

start file:./bundles/cron4j-bundle-0.2.0-SNAPSHOT.jar
start file:./bundles/cron4j-bundle.example-0.1.0.jar

arch
inspect service capability
log

TODOLIST
========
* Persistent scheduling (using service.pid)
* Felix shell commands to list current scheduled services
* ...
