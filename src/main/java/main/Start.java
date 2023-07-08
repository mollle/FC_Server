package main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import classes.Coordinate;
import classes.Coordinate.Type;
import services.SqlService;

public class Start {

	private static final int port = 8080;

	private static final Logger logger = LoggerFactory.getLogger(Start.class);

	public static void main(String[] args) {

		SqlService sqlService = new SqlService();

//		sqlService.initTables();

//		sqlService.saveCoordinate(new Coordinate(10.5, 10.5, Type.HIGH, new Date()));

//		logger.info("Danger level: "+ sqlService.getDangerLevel(new Coordinate(10.6, 10.6, Type.LOW, new Date()), 15600).toString());

//		logger.info(""+Coordinate.calculateDistance(10.5, 10.5, 10.6, 10.6));

//		// Erstelle einen ZMQ-Kontext und einen Socket vom Typ REP
		try (ZContext context = new ZContext()) {
			ZMQ.Socket socket = context.createSocket(SocketType.REP);
			socket.bind("tcp://*:" + port);

			while (!Thread.currentThread().isInterrupted()) {
				logger.info("Listening on Port " + port);

				ZMsg request = ZMsg.recvMsg(socket);
				Coordinate lastTraffic = null;
				logger.info("Got message: " + request.toString());

				while (request.size() > 0) {
					ZFrame frame = request.removeFirst();
					byte[] messageBytes = frame.getData();
					Coordinate coordinate = Coordinate.fromBytes(messageBytes);
					// Speichere die Koordinate
					sqlService.saveCoordinate(coordinate);
					if (coordinate.getType() == Type.TRAFFIC) {
						lastTraffic = coordinate;
					}
				}

			    try {
			        Thread.sleep(1000); 
			    } catch (InterruptedException e) {
			        e.printStackTrace();
			    }

				if (lastTraffic != null) {
					request.add(sqlService.getDangerLevel(lastTraffic, 300).toString());
//			    }
					// TODO: wenn kein traffic ist request inhalt vielleicht leer? weil immer alle
					// frames removed werden

					request.send(socket);
					logger.info("send response");
					lastTraffic = null;
				}

			}
		}

	}
}