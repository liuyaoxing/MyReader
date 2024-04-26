package offline.export.utils;

import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtils {

	public static boolean isNetworkAvailable(String urlStr) {
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(2000);
			conn.connect();
			int responseCode = conn.getResponseCode();
			return responseCode == 200 || responseCode == 404;
		} catch (Exception ex) {
		}
		return false;
	}
}
