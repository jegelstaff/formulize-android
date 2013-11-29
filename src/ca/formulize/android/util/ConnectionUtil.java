package ca.formulize.android.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConnectionUtil {

	/**
	 * Helper function to convert an entire input stream into a String
	 * 
	 * @param in
	 * @return String representation of the input stream
	 */
	static public String readInputToString(InputStreamReader in) {
		BufferedReader reader = new BufferedReader(in);
		StringBuilder stringBuilder = new StringBuilder();

		try {
			// Read server response
			String line = null;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stringBuilder.toString();

	}
}
