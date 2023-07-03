package main;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.WriteResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import classes.Coordinate;
import classes.Coordinate.Type;
import services.FirebaseService;

public class Start {

	private static final Logger logger = LoggerFactory.getLogger(Start.class);

	private static final int port = 8080;

	public static void main(String[] args) {

		logger.info("Server gestartet auf Port " + port);

		FirebaseService.initialize();
		
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			logger.error("sleep wurde unterbrochen");
//			e.printStackTrace();
//		}
		
		logger.info("Firebase test...");
		
        Coordinate coordinateTest = new Coordinate(51.5074, -0.1278, Type.HIGH, new Date());
        
		ApiFuture<WriteResult> futureTest = FirebaseService.saveCoordinate(coordinateTest);
		
		try {
			
			WriteResult resultTest = futureTest.get();
//			ZMsg reply = new ZMsg();
//			reply.add("OK");
//			reply.send(socket);
			logger.info("Coordinate erfolgreich gespeichert.");
		} catch (InterruptedException e) {
			
			logger.error("Speichern der Coordinate unterbrochen: " + e.getMessage());
			e.printStackTrace();
			Thread.currentThread().interrupt();
//			ZMsg reply = new ZMsg();
//			reply.add("Error");
//			reply.send(socket);
		} catch (ExecutionException e) {
//			ZMsg reply = new ZMsg();
//			reply.add("Error");
//			reply.send(socket);
			logger.error("Fehler beim Speichern der Coordinate: " + e.getMessage());
			e.printStackTrace();

		}
        
        logger.info("Firebase test... END");


		// Erstelle einen ZMQ-Kontext und einen Socket vom Typ REP
		try (ZContext context = new ZContext()) {
			ZMQ.Socket socket = context.createSocket(SocketType.REP);
			socket.bind("tcp://*:" + port);

			while (!Thread.currentThread().isInterrupted()) {


				logger.info("Listening on Port " + port);

				// Warte auf eine Nachricht vom Client
				ZMsg request = ZMsg.recvMsg(socket);
				logger.info("Got message:" + request.toString());

				// Extrahiere die Coordinate aus der Nachricht
				byte[] coordinateBytes = request.getLast().getData();
				Coordinate coordinate = Coordinate.fromBytes(coordinateBytes);

				// Speichere die Coordinate in Firebase
				ApiFuture<WriteResult> future = FirebaseService.saveCoordinate(coordinate);
				try {
					
					WriteResult result = future.get();
//					ZMsg reply = new ZMsg();
//					reply.add("OK");
//					reply.send(socket);
					logger.info("Coordinate erfolgreich gespeichert.");
				} catch (InterruptedException e) {
					
					logger.error("Speichern der Coordinate unterbrochen: " + e.getMessage());
					e.printStackTrace();
					Thread.currentThread().interrupt();
//					ZMsg reply = new ZMsg();
//					reply.add("Error");
//					reply.send(socket);
				} catch (ExecutionException e) {
//					ZMsg reply = new ZMsg();
//					reply.add("Error");
//					reply.send(socket);
					logger.error("Fehler beim Speichern der Coordinate: " + e.getMessage());
					e.printStackTrace();

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
