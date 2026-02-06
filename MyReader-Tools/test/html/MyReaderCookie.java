package html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * @author vwpolo
 */
public class MyReaderCookie implements CookieJar {

	private static MyReaderCookie instance = new MyReaderCookie();

	private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

	private final Map<String, String> cookieStoreMap = new HashMap<>();

	public static MyReaderCookie getInstance() {
		return instance;
	}

	@Override
	public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
		cookieStore.put(url, cookies);

		for (Cookie cookie : cookies) {
			String[] parts = cookie.value().split(";");
			for (String part : parts) {
				part = part.trim();
				if (part.startsWith("Set-Cookie: ")) {
					part = part.substring("Set-Cookie: ".length());
					int equalsIndex = part.indexOf('=');
					if (equalsIndex != -1) {
						String key = part.substring(0, equalsIndex);
						String value = part.substring(equalsIndex + 1);
						cookieStoreMap.put(key, value);
					}
				}
			}
		}
	}

	@Override
	public List<Cookie> loadForRequest(HttpUrl url) {

		List<Cookie> cookies = cookieStore.get(url);
		return cookies != null ? cookies : new ArrayList<Cookie>();
	}
}
