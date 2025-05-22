package html;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class UrlParameterReplacer {
	public static String replaceUrlParameter(String url, String parameterName, String parameterValue) {
		try {
			URI uri = new URI(url);
			String query = uri.getQuery();
			Map<String, String> parameters = new HashMap<>();
			if (query != null) {
				String[] pairs = query.split("&");
				for (String pair : pairs) {
					String[] parts = pair.split("=");
					parameters.put(parts[0], parts.length > 1 ? parts[1] : "");
				}
			}
			parameters.put(parameterName, parameterValue);
			StringBuilder newQuery = new StringBuilder();
			boolean first = true;
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				if (first) {
					first = false;
				} else {
					newQuery.append("&");
				}
				newQuery.append(entry.getKey()).append("=").append(entry.getValue());
			}
			return uri.getScheme() + "://" + uri.getAuthority() + uri.getPath() + (newQuery.length() > 0 ? "?" + newQuery.toString() : "");
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid URL", e);
		}
	}
	
	public static void main(String[] args) {
		String url = "https://baijiahao.baidu.com/s?id=1820677285038563021&wfr=spider&for=pc&searchword=%E7%8E%8B%E5%B0%94%E5%BE%B7%E7%BB%8F%E5%85%B8%E5%8F%A5%E5%AD%90";
		
		System.out.println(replaceUrlParameter(url, "for", "mobile"));
	}
}