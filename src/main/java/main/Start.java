package main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import classes.Coordinate;
import services.SqlService;

public class Start {

	private static final int port = 8080;

	private static final Logger logger = LoggerFactory.getLogger(Start.class);

	public static void main(String[] args) {

		SqlService sqlService = new SqlService();

		sqlService.initTables();

		// Erstelle einen ZMQ-Kontext und einen Socket vom Typ REP
		try (ZContext context = new ZContext()) {
			ZMQ.Socket socket = context.createSocket(SocketType.REP);
			socket.bind("tcp://*:" + port);

			while (!Thread.currentThread().isInterrupted()) {

				logger.info("Listening on Port " + port);

				// Warte auf eine Nachricht vom Client
		           ZMsg request = ZMsg.recvMsg(socket);
		            logger.info("Got message: " + request.toString());

		            // Extrahiere den Inhalt der Nachricht als Byte-Array
		            ZFrame contentFrame = request.getLast();
		            byte[] messageBytes = contentFrame.getData();
		            
		            // Speichere die Coordinate
		            sqlService.saveCoordinate(Coordinate.fromBytes(messageBytes));

		            // Sende eine Bestätigung an den Client
		            request.send(socket);
		        }
		}
	}
}
