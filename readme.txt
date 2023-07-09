1. in die GCE VM per SSH einloggen

2. microsoft sql server starten: 

	sudo docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=notAdmin.23" -p 1433:1433 -v sqlvolume:/var/opt/mssql -d mcr.microsoft.com/mssql/server:2022-latest

Hier wird das sqlvolume erzeugt, damit die Daten auch beim löschen des Dockers erhalten bleiben

3. IP Adresse des sql dockers besorgen:

	docker inspect <container_id> 

4. die IP in die Variable sqlContainerIP der Klasse SqlService im Projekt Server eintragen

5. im Base Verzeichnis vom Server Java Projekt folgendes ausführen: 
	
	mvn compile jib:build 
	
6. Server Docker mit Starten: (bin mir manchmal nicht ganz sicher ob der wirklich das latest build nimmt was 3 sekunden vorher gebaut wurde, daher einfach das alte image löschen falls eins exisitert):
   
	docker run -p 8080:8080  gcr.io/fogcomputing-391406/server:latest

Ab hier läuft alles zur Interaktion mit der App, Schritte 7 und 8 sind zum testen mit dem Java Client der zum testen geschrieben wurde.

7. mit Java Projekt Client den Client starten (ip adresse anpassen)

8. jetzt sollte im log vom Server Container "Got message..." stehen


