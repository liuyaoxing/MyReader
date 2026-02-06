package html;

import java.util.concurrent.TimeUnit;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetStringFromUrl {

	public static void main(String[] args) throws Throwable {
		// System.out.println(getStringFromUrl("https://cl.xgbrnb.org/htm_mob/2501/7/6663802.html"));

		System.out.println(getStringFromUrl(
				"https://mbd.baidu.com/newspage/data/landingsuper?id=1791474120816221227&third=baijiahao&baijiahao_id=1791474120816221227&wfr=&c_source=duedge&c_score=0.999100&p_tk=7631rRq9H4L%2B8qP0AQg%2FLJ%2BH1xbCL2XJupZjLHFgdPFuFgTaYsofNypU4qRxLbzZbnXBVjWsaBekq5g%2BeEaCiLHk6p%2BF%2BnwFbZrm1ecF%2FYI%2Bc3Cj%2Frecqq7%2BaNnB%2FXUrQLnZ%2FrqwuMw0%2BBJ85Af4phJNFk7566OcjxPr5h5BgOzKnNk%3D&p_timestamp=1739341545&p_sign=42ccfa1525294c5a1476f19d1e21e631&p_signature=2fd4dbe059e672b7ea20b0f348243511&__pc2ps_ab=7631rRq9H4L%2B8qP0AQg%2FLJ%2BH1xbCL2XJupZjLHFgdPFuFgTaYsofNypU4qRxLbzZbnXBVjWsaBekq5g%2BeEaCiLHk6p%2BF%2BnwFbZrm1ecF%2FYI%2Bc3Cj%2Frecqq7%2BaNnB%2FXUrQLnZ%2FrqwuMw0%2BBJ85Af4phJNFk7566OcjxPr5h5BgOzKnNk%3D|1739341545|2fd4dbe059e672b7ea20b0f348243511|42ccfa1525294c5a1476f19d1e21e631"));
		// System.out.println(getStringFromUrl(
		// "https://baijiahao.baidu.com/s?id=1820677285038563021&wfr=spider&for=pc&searchword=%E7%8E%8B%E5%B0%94%E5%BE%B7%E7%BB%8F%E5%85%B8%E5%8F%A5%E5%AD%90"));

		// System.out.println(getStringFromJsoup("https://baijiahao.baidu.com/s?id=1791474120816221227"));

		// System.out.println(getUrl("https://baijiahao.baidu.com/s?id=1820677285038563021&wfr=spider&for=pc"));
	}

	private static char[] getStringFromJsoup(String url) throws Throwable {
		// 如果报错，忽略url的https证书；http开头的应该可以不用处理

		// HttpsUrlValidator.retrieveResponseFromServer(url);

		// 加入url并编写请求头，打开浏览器控制台照着写

		Connection.Response response = Jsoup

				.connect(url)

				.header("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")

				.header("Accept-Encoding", "*/*")

				.header("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,ja-JP;q=0.6,ja;q=0.5,ko-KR;q=0.4,ko;q=0.3")

				.header("Connection", "keep-alive")

				.header("Content-Type", "application/json;charset=UTF-8")

				.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.135 Safari/537.36")

				.timeout(10000) // 设置超时时间

				.ignoreContentType(true)

				.execute();

		System.out.println(response.body()); // 获取到的html字符串
		return null;
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
		// reqBuilder.tag(PageSaver.HTTP_REQUEST_TAG);
		// reqBuilder.cacheControl(new CacheControl.Builder().maxStale(365,
		// TimeUnit.DAYS).build());
		String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.5845.97 Safari/537.36 Core/1.116.475.400 QQBrowser/13.5.6267.400";
		reqBuilder.addHeader("User-Agent", userAgent);
		// reqBuilder.addHeader("Referer", url);
		reqBuilder.addHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
		reqBuilder.addHeader("Accept-Encoding", "gzip, deflate, br");
		reqBuilder.addHeader("Cache-Control", "max-age=0");
		reqBuilder.addHeader("Connection", "keep-alive");

		reqBuilder.addHeader("Host", "baijiahao.baidu.com");

		reqBuilder.addHeader("Sec-Fetch-Dest", "document");
		reqBuilder.addHeader("Sec-Fetch-Mode", "navigate");
		reqBuilder.addHeader("Sec-Fetch-Site", "none");
		reqBuilder.addHeader("Sec-Fetch-User", "?1");

		reqBuilder.addHeader("Upgrade-Insecure-Requests", "1");

		reqBuilder.addHeader("Sec-Ch-Ua", "\"Not)A;Brand\";v=\"24\", \"Chromium\";v=\"116\"");
		reqBuilder.addHeader("Sec-Ch-Ua-Mobile", "?0");
		reqBuilder.addHeader("Sec-Ch-Ua-Platform", "\"Windows\"");

		reqBuilder.addHeader("Cookie",
				"PSTM=1650171937; BIDUPSID=C18EC05919A99D2AD8A1BAEC8CA7A782; BAIDUID=E9C0BB1668084050B9B9A7D1EF6D1A36:SL=0:NR=10:FG=1; MCITY=-340%3A; H_WISE_SIDS=60277_61027_60853_61671_61545_61781_61793_61783_61842_61889; BAIDUID_BFESS=E9C0BB1668084050B9B9A7D1EF6D1A36:SL=0:NR=10:FG=1; __bid_n=193660b0d8337282520f82; H_PS_PSSID=60277_61027_61671_61545_61781_61889_61987; BDUSS=k5OYUtlTTZVRTFteUxiYlFJMk5IUk5VcmxHaGM3RE4yY2hteEI5RU9DUXNoTDFuSVFBQUFBJCQAAAAAAAAAAAEAAACaQzwsbGl1eXhfZ21haWwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACz3lWcs95VnQ; BDUSS_BFESS=k5OYUtlTTZVRTFteUxiYlFJMk5IUk5VcmxHaGM3RE4yY2hteEI5RU9DUXNoTDFuSVFBQUFBJCQAAAAAAAAAAAEAAACaQzwsbGl1eXhfZ21haWwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACz3lWcs95VnQ; H_WISE_SIDS_BFESS=60277_61027_60853_61671_61545_61781_61793_61783_61842_61889; BAIDU_WISE_UID=wapp_1738053507963_829; ZFY=bN248FA1NFJYHiEpd28lE:BEl3Mx5sBuV:BBXY41Lecco:C");

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
		// OkHttpClient client = new OkHttpClient();
		// Request request = new Request.Builder()//
		// .url(url)//
		// .addHeader("User-Agent", RefererMapping.getUserAgent(null))//
		// .addHeader("Referer", RefererMapping.getReferer(url, url))//
		// .tag(PageSaver.HTTP_REQUEST_TAG)//
		// .build();
		// Response response = client.newCall(request).execute();
		// return response.body().string();
		return null;
	}
}
