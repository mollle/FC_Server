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
		TRAFFIC, LOW, MEDIUM, HIGH, SMOOTH
	}

	private double latitude;
	private double longitude;
	private Type type;
	private Date timestamp;

	public Coordinate(double latitude, double longitude, Type type, Date timestamp) {
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		this.setType(type);
		this.setTimestamp(timestamp);
	}
	
//Code from ChatGPT...............................................
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
//................................................................
    
//Code adapted from https://gist.github.com/vananth22/888ed9a22105670e7a4092bdcf0d72e4...
        public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
            int earthRadiusKm = 6371; // Radius der Erde
            Double latDistance = Math.toRadians(lat2-lat1);
            Double lonDistance = Math.toRadians(lon2-lon1);
            Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + 
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * 
            Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            Double distance = earthRadiusKm * c;
            logger.info("distance between "+lat1+","+lon1+" and "+lat2+","+lon2+" is "+distance);
            return distance;
        }
//................................................................

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
