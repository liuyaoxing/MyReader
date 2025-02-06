package html;

import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;

public class GetStringFromUrl {

	public static void main(String[] args) throws Throwable {
		System.out.println(getStringFromUrl("https://cl.xgbrnb.org/htm_mob/2501/7/6663802.html"));
	}

	public static String getStringFromUrl(String url) throws Exception {
		if (url == null || url.length() == 0)
			return "";

		OkHttpClient client = new OkHttpClient.Builder()//
				.connectTimeout(10, TimeUnit.SECONDS)//
				.readTimeout(10, TimeUnit.SECONDS)//
				.followRedirects(true)//
				.followSslRedirects(true)//
				.cookieJar(MyReaderCookie.getInstance())//
				.build();

		okhttp3.Request.Builder reqBuilder = new Request.Builder();
		reqBuilder.url(url);
//	        reqBuilder.tag(PageSaver.HTTP_REQUEST_TAG);
		reqBuilder.cacheControl(new CacheControl.Builder().maxStale(365, TimeUnit.DAYS).build());

//	        String userAgent = url;//RefererMapping.getUserAgent(null);
//	        if ("chuansong.me".equals(MyReaderHelper.getHostDomain(url))) {
//	            reqBuilder.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//	            reqBuilder.addHeader("Accept-Encoding", "gzip, deflate, sdch");
//	            reqBuilder.addHeader("Accept-Language", "zh-CN,zh;q=0.8");
//	            reqBuilder.addHeader("Connection", "keep-alive");
//	        }
		// reqBuilder.addHeader("Host", MyReaderHelper.getHostDomain(url));
		String userAgent = "Mozilla/5.0 (Linux; Android 9; Redmi Note 7 Pro Build/PKQ1.181203.001; wv) AppleWebKit/537.36 (KHTML, like  Gecko) Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36";
		reqBuilder.addHeader("User-Agent", userAgent);
		reqBuilder.addHeader("Referer", url);
		reqBuilder.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
		reqBuilder.addHeader("Cookie", "ismob=1");
		
		try {
			Response response = client.newCall(reqBuilder.build()).execute();
			if (!response.isSuccessful()) {
				return getHtml(url);
			}
			String out = response.body().string();
			response.body().close();
			return out;
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return "";
	}

	public static String getHtml(String url) throws Exception {
//		OkHttpClient client = new OkHttpClient();
//		Request request = new Request.Builder()//
//				.url(url)//
//				.addHeader("User-Agent", RefererMapping.getUserAgent(null))//
//				.addHeader("Referer", RefererMapping.getReferer(url, url))//
//				.tag(PageSaver.HTTP_REQUEST_TAG)//
//				.build();
//		Response response = client.newCall(request).execute();
//		return response.body().string();
		return null;
	}
}
