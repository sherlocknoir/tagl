TP OSGi Partie 2 : Composants OSGi
-----------------------------------
Vous manipulerez Apache iPOJO le modeles de composant orienté service Java pour OSGi
utilisé industrielement
* http://felix.apache.org/site/apache-felix-ipojo.html

0) Installation
Dezippez le framework 3 dans le répertoire Felix
Changez EVENTUELLEMENT les valeurs du proxy dans le osgi-part2\felix\run.bat
Installez EVENTUELLEMENT osgi-part2\settings.xml dans  C:\Documents and Settings\VotreUsername\.m2

Demarrez Felix :
	cd felix
	run.bat
Lancez les commandes du shell Gogo

help
lb
obr:list
obr:deploy -s "Apache Felix Log Service"
felix:log debug
obr:deploy -s "Apache Felix iPOJO"
obr:deploy -s "Apache Felix iPOJO Gogo Command"
obr:deploy -s "Apache Felix File Install"


inspect requirement service
inspect r service
inspect capability service
inspect c service 

---------------
Cron4J http://www.sauronsoftware.it/projects/cron4j/ est une bibliotheque java pour déclencher périodiquement des taches.
Elle est inspirée du cron Unix http://fr.wikipedia.org/wiki/Crontab.
Les taches doivent implémenter l'interface java.lang.Runnable.
Regardez les exemples fournis dans osgi-part2\cron4j\cron4j-orig\examples

Votre travail est d'utiliser cette bibliotheque pour déclencher périodiquement des services OSGi dont l'interface est java.lang.Runnable et la propriété cron4j.pattern contient le pattern cron
Voir le schéma d'architecture osgi-part2\architecture.ppt

1) Deployez les bundles Cron4J depuis le repository via FileInstall en les déposant dans le répertoire .\load de l'installation de Felix
cron4j-bundle-0.2.0-SNAPSHOT.jar
cron4j-bundle.example-0.1.0.jar

Que se passe t'il ?
lb
inspect c service
inspect r service


ipojo:factories
ipojo:factory
ipojo:handlers
ipojo:instance
ipojo:instances


2) completer le code du projet cron4j-bundle pour qu'il schedule des services java.lang.Runnable
Moins de 20 lignes de Java

3) ajouter au composant iPOJO un requires http://felix.apache.org/site/service-requirement-handler.html vers un LogService et completer le code du composant pour journaliser les enreisgrements et desenregistrement de services 
(ie utiliser le LogService)

inspect c service
inspect r service

4) transformez ce Requires en TemporalRequires
Observez ce qu'il se passe quand le LogService disparait !

inspect c service
inspect r service

5) Completez le composant iPOJO pour fournir une commande Gogo Shell listant les "schedules" en cours et leurs statitisques (nombre d'invocations, temps restant jusqu'a la prochaine invocation, nombre d'invocation restante, ...)
Ce complement est livrée dans la version cron4j-bundle-0.3.0-SNAPSHOT.jar

inspect c service
inspect r service

Inspirez vous du code de la commande Arch d'iPOJO (voir le repertoire cron4j-cmd)
http://svn.apache.org/viewvc/felix/trunk/ipojo/arch-gogo/
http://svn.apache.org/viewvc/felix/trunk/ipojo/arch-gogo/pom.xml?view=co
http://svn.apache.org/viewvc/felix/trunk/ipojo/arch-gogo/src/main/java/org/apache/felix/ipojo/arch/gogo/Arch.java?view=co 
http://felix.apache.org/site/rfc-147-overview.html


inspect c service
inspect r service

----------------
Extra 1
obr:deploy -s "Apache Felix iPOJO White Board Pattern Handler"
obr:deploy -s "Apache Felix iPOJO Temporal Service Dependency Handler"

obr:deploy -s "Apache Felix iPOJO WebConsole Plugins"
browse http://localhost:8080/system/console/bundles
Username=admin Password=admin

----------------
Extra 2
Ajouter les metadonnées du handler JMX au composant TransientCronDeamon
http://felix.apache.org/site/ipojo-jmx-handler.html

Redeployez le bundle
Lancez jconsole dans un shell/cmd
Changez les propriétés du composant via la JConsole 




Minimal Gogo Command with iPOJO


import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.service.command.Descriptor;

@Component(immediate = true)
@Instantiate
@Provides(specifications = ListComponentsCommand.class)
public class ListComponentsCommand {

    @ServiceProperty(name = "osgi.command.scope", value = "test")
    String scope;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    String[] function = new String[] { "test" };

    @Descriptor("test")
    public void test() {
        System.out.println("test!");
    }



