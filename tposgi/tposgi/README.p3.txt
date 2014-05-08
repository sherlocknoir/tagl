==Shell JMX==

Deployez les 2 bundles
start file:.\repository\org.apache.felix.sandbox.mbean.shell.gogo-0.4.0.jar
start file:.\repository\org.apache.felix.sandbox.mbean.shell.rui-0.3.0.jar

Dans un autre cmd.exe

jvisualvm
Connectez vous avec service:jmx:rmi:///jndi/rmi://localhost:16969/jmxrmi


cd tposgi\projects\mshell
run.bat service:jmx:rmi:///jndi/rmi://localhost:16969/jmxrmi

