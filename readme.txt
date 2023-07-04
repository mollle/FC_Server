1. microsoft sql server starten (muss noch erweitert werden, damit datenbank nicht weg ist wenn der docker geschlossen wird)

	docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=notAdmin.23" -p 1433:1433 -d mcr.microsoft.com/mssql/server:2022-latest

2. IP Adresse des sql dockers besorgen:

	docker inspect <container_id> 

3. die IP in die variable sqlContainerIP der Klasse SqlService eintragen

4. im base verzeichnis vom Server java projekt 
	mvn compile jib:build 
	ausführen

5. Server docker mit starten: (bin mir manchmal nicht ganz sicher ob der wirklcih das latest nimmt was 3sekunden vorher gebaut wurde
   mit der ui lösche ich das image einfach immer, auf der vm habe ich es noch nicht getestet)
	docker run -p 8080:8080  gcr.io/fogcomputing-391406/server:latest

6. mit java projekt client starten (ip adresse anpassen)

7. jetzt sollte im log vom server container "Got message..." stehen

