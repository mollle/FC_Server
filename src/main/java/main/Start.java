package main;

import java.util.List;

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
	
	private static final int radiusThreshhold = 100;

	public static void main(String[] args) {

		SqlService sqlService = new SqlService();
		sqlService.initTables();
		try (ZContext context = new ZContext()) {
			ZMQ.Socket socket = context.createSocket(SocketType.REP);
			socket.bind("tcp://*:" + port);
			while (!Thread.currentThread().isInterrupted()) {
				logger.info("Listening on Port " + port);
				ZMsg request = ZMsg.recvMsg(socket);
				logger.info("Got message with " + request.size() + " frames.");
				Coordinate lastCoordinate = null;
				while (request.size() > 0) {
					ZFrame frame = request.removeFirst();
					byte[] messageBytes = frame.getData();
					Coordinate coordinate = Coordinate.fromBytes(messageBytes);
					logger.info("Got coordinate: " + coordinate.toString());
					sqlService.saveCoordinate(coordinate);
					if (request.size() == 0) {
						lastCoordinate = coordinate;
					}
				}
				if (lastCoordinate != null) {
					List<Coordinate> coordinatesInRadius = sqlService.getCoordinatesWithinRadius(lastCoordinate, radiusThreshhold);
					for (Coordinate coordinate : coordinatesInRadius) {
						request.add(coordinate.toBytes());
					}
					logger.info("Send response with " + coordinatesInRadius.size() + " coordinates");
				}else {
					logger.info("Send response without coordinates");
				}
				request.send(socket);
			}
		}

	}
}
