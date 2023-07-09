package services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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

	// Checkt jeden Table in absteigenden Gefahrenstufen ob es Einträge im Radius
	// gibt, die höchste Gefahrenstufe wird returnt
	public Type getDangerLevel(Coordinate centerCoordinate, int radiusIn100m) {
		if (getCoordinatesWithinRadius(centerCoordinate, radiusIn100m, Type.HIGH)) {
			return Type.HIGH;
		} else if (getCoordinatesWithinRadius(centerCoordinate, radiusIn100m, Type.MEDIUM)) {
			return Type.MEDIUM;
		} else if (getCoordinatesWithinRadius(centerCoordinate, radiusIn100m, Type.LOW)) {
			return Type.LOW;
		} else {
			return Type.SMOOTH;
		}
	}

	private Boolean getCoordinatesWithinRadius(Coordinate coordinate, int radiusInM, Type type) {
		//erstmal alle einträge holen in einem umkreis von 1.0
		 try (PreparedStatement statement = connection.prepareStatement(
                 "SELECT * FROM [dbo].["+type.toString()+"] " +
                 "WHERE ABS(Latitude - ?) + ABS(Longitude - ?) <= 1")) {
        statement.setDouble(1, coordinate.getLatitude());
        statement.setDouble(2, coordinate.getLongitude());
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
            	//sobald eine Koordinate im radius liegt returnen
            			if(Coordinate.calculateDistance(resultSet.getDouble("Latitude"), resultSet.getDouble("Longitude"),coordinate.getLatitude(), coordinate.getLongitude())*1000<radiusInM){
            				return true;
            			};
				}
            return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

}
