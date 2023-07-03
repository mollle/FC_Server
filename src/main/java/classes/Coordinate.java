package classes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Coordinate implements Serializable  {
	
    private static final long serialVersionUID = 1L;

	public enum Type {
		TRAFFIC, LOW, MEDIUM, HIGH
	}

	private double latitude;
	private double longitude;
	private Type type;
	private Date timestamp;

	public Coordinate() {
		// Leerer Konstruktor erforderlich für Firebase
	}

	public Coordinate(double latitude, double longitude, Type type, Date timestamp) {
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		this.setType(type);
		this.setTimestamp(timestamp);
	}
	
    public byte[] toBytes() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
			logger.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public static Coordinate fromBytes(byte[] bytes) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (Coordinate) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
			logger.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(Coordinate.class);

}
