package html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * @author vwpolo
 */
public class MyReaderCookie implements CookieJar {

	private static MyReaderCookie instance = new MyReaderCookie();

	private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

	public static MyReaderCookie getInstance() {
		return instance;
	}

	@Override
	public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
		cookieStore.put(url, cookies);
	}

	@Override
	public List<Cookie> loadForRequest(HttpUrl url) {
		List<Cookie> cookies = cookieStore.get(url);
		return cookies != null ? cookies : new ArrayList<Cookie>();
	}
}
