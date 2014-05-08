export PROXY_OPTIONS="-Dhttp.proxyHost=www-cache.ujf-grenoble.fr -Dhttp.proxyPort=3128"
export JMX_OPTION="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=16969 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
# set GC_OPTIONS=-XX:+UnlockExperimentalVMOptions  -XX:+UseG1GC
echo JMX Service URL is service:jmx:rmi:///jndi/rmi://localhost:16969/jmxrmi
java $JMX_OPTION $PROXY_OPTIONS -jar bin/felix.jar


java -Dhttp.proxyHost=www-cache.ujf-grenoble.fr -Dhttp.proxyPort=3128 -jar bin/felix.jar

