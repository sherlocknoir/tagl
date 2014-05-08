TP OSGi Partie 1

1) installation et configuration

Telechargez la dernière distribution de Felix depuis
http://felix.apache.org/site/downloads.cgi

Dézippez la distribution dans le repertoire du TP

Configurez les variables JAVA_HOME et PATH

set FELIX_VER=4.0.2
set FELIX_HOME=felix-framework-%FELIX_VER%
set JAVA_HOME=c:\Progra~1\Java\jre6
set PATH=%JAVA_HOME%\bin;%PATH%
java -version



creez les repertoires dans %FELIX_HOME%
mkdir %FELIX_HOME%\load
mkdir %FELIX_HOME%\repository

copy repository\*.* %FELIX_HOME%\repository
copy run.bat %FELIX_HOME%

le fichier run.bat permet de lancer Felix avec le proxy HTTP configuré avec celui de l'UJF (www-cache.ujf-grenoble.fr:3128)

Positionnez 
cd %FELIX_HOME%

2) lancement de Felix
run.bat

3) Premières commandes
help
lb
headers
inspect capability osgi.wiring.package 0
inspect requirement osgi.wiring.package 0
inspect capability service 0
inspect requirement service 0

inspect c osgi.wiring.package 1
inspect r osgi.wiring.package 1
inspect c service 1
inspect r service 1


obr:deploy -start "Apache Felix Log Service"
help log
log debug
log info
log warn
log error

4) Deploiement de bundles
cd file:./repository/
install org.apache.felix.sandbox.mbean.shell.felix-0.3.0.jar
lb
start 5
inspect c osgi.wiring.package 5
inspect r osgi.wiring.package 5
inspect c service 5
inspect r service 5

update 5
log


install http://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.http.jetty/1.0.1/org.apache.felix.http.jetty-1.0.1.jar
log 1
lb
start 6
uninstall 6

que se passe t'il et pourquoi ?

5) Changement du niveau d'execution
lb
help bundlelevel
bundlelevel <OBR' bundle Id>
frameworklevel
bundlelevel -s 2 <OBR' bundle Id>
log
obr:list

que s'est il passé ?

frameworklevel 2
log
lb
obr:list


que s'est il passé ?

6) Deployez et lancez la Web console via l'OBR

obr:help
obr:list
obr:info "Apache Felix Web Management Console"
obr:deploy -s "Apache Felix Web Management Console"
lb

obr:deploy -s "Apache Felix Web Management Console" "Apache Felix Web Console Event Plugin" "Apache Felix Web Console Memory Usage Plugin" "Apache Felix Web Console Service Diagnostics Plugin" "Apache Felix Web Console UPnP Plugin"
lb


Combien de bundles ont été déployés (installés et démarrés) ?

naviguez http://localhost:8080/system/console depuis votre browser (username et password : admin et admin)

Deployez File Install soit depuis la web console onglet "OSGi repository"
soit avec la commande
obr:deploy -start "Apache Felix File Install"

7) FileInstall
Deployez des bundles via FileInstall en copiant les jarfiles des bundles du répertoires .\repertoire vers .\load depuis un cmd DOS
copy .\repository\org.apache.felix.examples.managedservice-0.1.0-SNAPSHOT.jar .\load
Que s'est il passé ? (faire lb puis log 4)

del .\load\org.apache.felix.examples.managedservice-0.1.0-SNAPSHOT.jar
Que s'est il passé ?  (faire lb puis log 4)

8) Configuration

obr:deploy -start "Apache Felix Configuration Admin Service"
obr:deploy -start "Apache Felix Metatype Service"
 
Utilisez la web console onglet "Configuration" pour reconfigurer
* la propriété "Poll interval" de FileInstall
log 1
* les propriétés "User Name" et "Password" de WebConsole
log 1

Arretez le framework avec shutdown
et redémarrez avec run.bat

Utilisez la web console onglet "Configuration" pour voir les 2 configurations courantes (elles sont persistantes a l'arret du Framework)

Deployez l'exemple
copy .\repository\org.apache.felix.examples.managedservice-0.1.0-SNAPSHOT.jar .\load\org.apache.felix.examples.managedservice.jar

Utilisez la web console onglet "Configuration" pour reconfigurer ce bundle
Observez les traces et le journal (log 1)

type .\repository\org.apache.felix.examples.managedservice.cfg
copy .\repository\org.apache.felix.examples.managedservice.cfg .\load
Observez les traces et le journal (log 1)

que s'est il passé ?
Utilisez la web console onglet "Configuration" pour voir le changement de configuration

Deployez la mise a jour de l'exemple
copy .\repository\org.apache.felix.examples.managedservice-0.2.0-SNAPSHOT.jar .\load\org.apache.felix.examples.managedservice.jar
help
test-managedservice
