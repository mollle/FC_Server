package services;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import classes.Coordinate;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.WriteResult;

public class FirebaseService {

	private static final Logger logger = LoggerFactory.getLogger(FirebaseService.class);

	private static final String TRAFFIC_COL_NAME = "traffic";

	private static Firestore firestore;

	public static void initialize() {
		try {
			logger.info("Initializing Firebase...");
//			FileInputStream serviceAccount = new FileInputStream("/app/resources/firebaseAuthKey.json");
			FileInputStream serviceAccount = new FileInputStream("src/main/resources/firebaseAuthKey.json");

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.setDatabaseUrl("https://fogcomputing-391406.firebaseio.com").build();
			FirebaseApp.initializeApp(options);
			firestore = FirestoreOptions.getDefaultInstance().getService();
			logger.info("Initializing Firebase... Done");
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public static Firestore getFirestore() {
		if (firestore == null) {
			throw new IllegalStateException("Firebase has not been initialized. Call initialize() first.");
		}
		return firestore;
	}

	public static ApiFuture<WriteResult> saveCoordinate(Coordinate coordinate) {
		logger.info("Saving Coordinate...");
		Firestore firestore = getFirestore();

		// Erstelle einen Namen für das Dokument basierend auf dem Timestamp
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		String documentName = dateFormat.format(coordinate.getTimestamp());

		// Speichere die Coordinate in Firebase
		 DocumentReference newCoordinateDocRef = firestore.collection(TRAFFIC_COL_NAME).document(documentName);
		
		// Erstelle eine Map mit den Feldern und Werten der Coordinate
		Map<String, Object> coordinateData = new HashMap<>();
		coordinateData.put("latitude", coordinate.getLatitude());
		coordinateData.put("longitude", coordinate.getLongitude());
		coordinateData.put("type", coordinate.getType().toString());
		coordinateData.put("timestamp", coordinate.getTimestamp());

		return newCoordinateDocRef.set(coordinateData);
	}
}
