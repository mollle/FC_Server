package services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import classes.Coordinate;

public class SqlService {

	private static final String sqlContainerIP = "172.17.0.3";
	
	private static final String url = "jdbc:sqlserver://"+sqlContainerIP+";databaseName=master";

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
			logger.error("Can't connect to sql server");
			e.printStackTrace();
		}
	}

	// erstellt 4 tables für traffic, low, medium, high mit den zeilen latidude,
	// longitude und time
	public void initTables() {

		logger.info("Init tables...");

		try (Statement statement = connection.createStatement()) {

			// Tabelle für TRAFFIC
			String createTrafficTableQuery = "IF OBJECT_ID('[dbo].[traffic]', 'U') IS NULL\n"
					+ "CREATE TABLE [dbo].[traffic]\n" + "(\n" + "    [Id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,\n"
					+ "    [Timestamp] DATETIME NOT NULL,\n" + "    [Latitude] FLOAT NOT NULL,\n"
					+ "    [Longitude] FLOAT NOT NULL\n" + ")";
			statement.execute(createTrafficTableQuery);

			// Tabelle für LOW
			String createLowTableQuery = "IF OBJECT_ID('[dbo].[low]', 'U') IS NULL\n" + "CREATE TABLE [dbo].[low]\n"
					+ "(\n" + "    [Id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,\n"
					+ "    [Timestamp] DATETIME NOT NULL,\n" + "    [Latitude] FLOAT NOT NULL,\n"
					+ "    [Longitude] FLOAT NOT NULL\n" + ")";
			statement.execute(createLowTableQuery);

			// Tabelle für MEDIUM
			String createMediumTableQuery = "IF OBJECT_ID('[dbo].[medium]', 'U') IS NULL\n" + "CREATE TABLE [dbo].[medium]\n"
					+ "(\n" + "    [Id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,\n"
					+ "    [Timestamp] DATETIME NOT NULL,\n" + "    [Latitude] FLOAT NOT NULL,\n"
					+ "    [Longitude] FLOAT NOT NULL\n" + ")";
			statement.execute(createMediumTableQuery);

			// Tabelle für HIGH
			String createHighTableQuery = "IF OBJECT_ID('[dbo].[high]', 'U') IS NULL\n" + "CREATE TABLE [dbo].[high]\n"
					+ "(\n" + "    [Id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,\n"
					+ "    [Timestamp] DATETIME NOT NULL,\n" + "    [Latitude] FLOAT NOT NULL,\n"
					+ "    [Longitude] FLOAT NOT NULL\n" + ")";
			statement.execute(createHighTableQuery);

			logger.info("Init tables... DONE");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void saveCoordinate(Coordinate coordinate) {

	    // Überprüfe, ob das Coordinate-Objekt erfolgreich erstellt wurde
	    if (coordinate == null) {
	        logger.error("Error creating Coordinate object from message");
	        return;
	    }

	    // Speichere das Coordinate-Objekt in der entsprechenden Tabelle
	    String tableName;
	    switch (coordinate.getType()) {
	        case TRAFFIC:
	            tableName = "traffic";
	            break;
	        case LOW:
	            tableName = "low";
	            break;
	        case MEDIUM:
	            tableName = "medium";
	            break;
	        case HIGH:
	            tableName = "high";
	            break;
	        default:
	            logger.error("Invalid coordinate type");
	            return;
	    }

	    String insertQuery = "INSERT INTO " + tableName + " (Timestamp, Latitude, Longitude) VALUES (?, ?, ?)";
	    try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
	        preparedStatement.setTimestamp(1, new Timestamp(coordinate.getTimestamp().getTime()));
	        preparedStatement.setDouble(2, coordinate.getLatitude());
	        preparedStatement.setDouble(3, coordinate.getLongitude());
	        preparedStatement.executeUpdate();
	        logger.info("Coordinate saved to table: " + tableName);
	    } catch (SQLException e) {
	        logger.error("Error saving coordinate to table: " + tableName);
	        e.printStackTrace();
	    }
	}

}
