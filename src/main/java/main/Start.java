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
		sqlService.initTables();
		try (ZContext context = new ZContext()) {
			ZMQ.Socket socket = context.createSocket(SocketType.REP);
			socket.bind("tcp://*:" + port);
			while (!Thread.currentThread().isInterrupted()) {
				logger.info("Listening on Port " + port);
				ZMsg request = ZMsg.recvMsg(socket);
				Coordinate lastTraffic = null;
				logger.info("Got message with " + request.size() + " frames.");
				while (request.size() > 0) {
					ZFrame frame = request.removeFirst();
					byte[] messageBytes = frame.getData();
					Coordinate coordinate = Coordinate.fromBytes(messageBytes);
					sqlService.saveCoordinate(coordinate);
					if (coordinate.getType() == Type.TRAFFIC) {
						lastTraffic = coordinate;
					}
				}
				if (lastTraffic != null) {
					request.add(sqlService.getDangerLevel(lastTraffic, 300).toString());
				} else {
					request.add("noTraffic");
				}
				request.send(socket);
				logger.info("Send response with content: "+request.toString());
				lastTraffic = null;
			}
		}
	}

}
