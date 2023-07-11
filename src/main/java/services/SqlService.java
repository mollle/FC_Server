package services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import classes.Coordinate;
import classes.Coordinate.Type;

public class SqlService {

	private static final String sqlContainerIP = "172.17.0.2";

	private static final String url = "jdbc:sqlserver://" + sqlContainerIP + ";databaseName=master";

	private static final String username = "sa";

	private static final String password = "notAdmin.23";

	private static final Logger logger = LoggerFactory.getLogger(SqlService.class);

	private Connection connection;

	public SqlService() {

		Properties properties = new Properties();
		properties.setProperty("user", username);
		properties.setProperty("password", password);
		properties.setProperty("ssl", "true");
		properties.setProperty("sslTrustServerCertificate", "true");

		logger.info("Connecting to SQL server...");
		try {
			this.connection = DriverManager.getConnection(url, username, password);
			logger.info("Connecting to SQL server... DONE");
		} catch (SQLException e) {
			logger.error("Can't connect to SQL server");
			e.printStackTrace();
		}
	}

	// erstellt 4 tables für traffic, low, medium, high mit den zeilen latidude,
	// longitude und time
	public void initTables() {
		logger.info("Init tables...");
//Code from ChatGPT...............................................
		try (Statement statement = connection.createStatement()) {

			// Tabelle für TRAFFIC
			String createTrafficTableQuery = "IF OBJECT_ID('[dbo].[traffic]', 'U') IS NULL\n" + "CREATE TABLE [dbo].["
					+ Type.TRAFFIC.toString() + "]\n" + "(\n" + "    [Id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,\n"
					+ "    [Timestamp] DATETIME NOT NULL,\n" + "    [Latitude] FLOAT NOT NULL,\n"
					+ "    [Longitude] FLOAT NOT NULL\n" + ")";
			statement.execute(createTrafficTableQuery);

			// Tabelle für LOW
			String createLowTableQuery = "IF OBJECT_ID('[dbo].[low]', 'U') IS NULL\n" + "CREATE TABLE [dbo].["
					+ Type.LOW.toString() + "]\n" + "(\n" + "    [Id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,\n"
					+ "    [Timestamp] DATETIME NOT NULL,\n" + "    [Latitude] FLOAT NOT NULL,\n"
					+ "    [Longitude] FLOAT NOT NULL\n" + ")";
			statement.execute(createLowTableQuery);

			// Tabelle für MEDIUM
			String createMediumTableQuery = "IF OBJECT_ID('[dbo].[medium]', 'U') IS NULL\n" + "CREATE TABLE [dbo].["
					+ Type.MEDIUM.toString() + "]\n" + "(\n" + "    [Id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,\n"
					+ "    [Timestamp] DATETIME NOT NULL,\n" + "    [Latitude] FLOAT NOT NULL,\n"
					+ "    [Longitude] FLOAT NOT NULL\n" + ")";
			statement.execute(createMediumTableQuery);

			// Tabelle für HIGH
			String createHighTableQuery = "IF OBJECT_ID('[dbo].[high]', 'U') IS NULL\n" + "CREATE TABLE [dbo].["
					+ Type.HIGH.toString() + "]\n" + "(\n" + "    [Id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,\n"
					+ "    [Timestamp] DATETIME NOT NULL,\n" + "    [Latitude] FLOAT NOT NULL,\n"
					+ "    [Longitude] FLOAT NOT NULL\n" + ")";
			statement.execute(createHighTableQuery);

//................................................................

			logger.info("Init tables... DONE");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Speichere das Coordinate-Objekt in der entsprechenden Tabelle
	public void saveCoordinate(Coordinate coordinate) {
		if (coordinate == null) {
			logger.error("Error creating Coordinate object from message");
			return;
		}
		String tableName = coordinate.getType().toString();
		if (tableName.equals(Type.SMOOTH.toString())) {
			logger.error("There is no table \"SMOOTH\"");
		}
//Code from ChatGPT...............................................
		String insertQuery = "INSERT INTO " + tableName + " (Timestamp, Latitude, Longitude) VALUES (?, ?, ?)";
		try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
			preparedStatement.setTimestamp(1, new Timestamp(coordinate.getTimestamp().getTime()));
			preparedStatement.setDouble(2, coordinate.getLatitude());
			preparedStatement.setDouble(3, coordinate.getLongitude());
			preparedStatement.executeUpdate();
//................................................................
			logger.info("Coordinate saved to table: " + tableName);
		} catch (SQLException e) {
			logger.error("Error saving coordinate to table: " + tableName);
			e.printStackTrace();
		}
	}

	// Holt aus jeder Table alle Einträge im radius
	public List<Coordinate> getCoordinatesWithinRadius(Coordinate centerCoordinate, int radiusInM) {
		List<Coordinate> coordinatesList = new ArrayList<>();
		coordinatesList.addAll(getCoordinatesWithinRadius(centerCoordinate, radiusInM, Type.HIGH));
		coordinatesList.addAll(getCoordinatesWithinRadius(centerCoordinate, radiusInM, Type.MEDIUM));
		coordinatesList.addAll(getCoordinatesWithinRadius(centerCoordinate, radiusInM, Type.LOW));
		return coordinatesList;
	}

	private List<Coordinate> getCoordinatesWithinRadius(Coordinate coordinate, int radiusInM, Type type) {
		List<Coordinate> coordinatesList = new ArrayList<>();
		// erstmal alle einträge holen in einem umkreis von 0.5, weil das Filtern direkt
		// in der SQL Abfrage nicht geklappt hat
		try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM [dbo].[" + type.toString() + "] "
				+ "WHERE ABS(Latitude - ?) + ABS(Longitude - ?) <= 0.5")) {
			statement.setDouble(1, coordinate.getLatitude());
			statement.setDouble(2, coordinate.getLongitude());
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					// jetzt nochmal genauer Filtern
					double distance = Coordinate.calculateDistance(resultSet.getDouble("Latitude"), resultSet.getDouble("Longitude"),
							coordinate.getLatitude(), coordinate.getLongitude()) * 1000;
					if (distance < radiusInM) {
						double latitude = resultSet.getDouble("Latitude");
						double longitude = resultSet.getDouble("Longitude");
						Date timestamp = resultSet.getDate("Timestamp");
						Coordinate newCoordinate = new Coordinate(latitude, longitude, type, timestamp);
						coordinatesList.add(newCoordinate);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return coordinatesList;
	}

}
