package offline.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import offline.export.log.LogHandler;
import offline.export.utils.IOUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 文件下载工具类（单例模式）
 */

public class DownloadUtil {

	private static DownloadUtil downloadUtil;
	private final OkHttpClient okHttpClient;

	public static DownloadUtil get() {
		if (downloadUtil == null) {
			downloadUtil = new DownloadUtil();
		}
		return downloadUtil;
	}

	public DownloadUtil() {
		Dispatcher dispatcher = new Dispatcher();
		dispatcher.setMaxRequests(1);
		okHttpClient = new OkHttpClient.Builder()//
				.dispatcher(dispatcher)//
				.retryOnConnectionFailure(true)//
				.connectTimeout(620, TimeUnit.SECONDS) // 连接超时
				.readTimeout(620, TimeUnit.SECONDS) // 读取超时
				.writeTimeout(60, TimeUnit.SECONDS) // 写超时
				.writeTimeout(60, TimeUnit.SECONDS) // 写超时
//                .addInterceptor(new CommonHeaderInterceptor())
//                .addInterceptor(new CacheInterceptor())
//                .addInterceptor(new HttpLoggerInterceptor())
//                .addNetworkInterceptor(new EncryptInterceptor())
				.build();
	}

	public Response newCall(Request request) throws IOException {
		return okHttpClient.newCall(request).execute();
	}

	/**
	 * @param url          下载连接
	 * @param destFileDir  下载的文件储存目录
	 * @param destFileName 下载文件名称，后面记得拼接后缀，否则手机没法识别文件类型
	 * @param listener     下载监听
	 * @throws IOException
	 */

	public void download(final String url, String id, final String destFileDir, final OnDownloadListener listener)
			throws IOException {
		download(url, id, new File(destFileDir).getCanonicalFile(), listener);
	}

	/**
	 * @param url          下载连接
	 * @param destFileDir  下载的文件储存目录
	 * @param destFileName 下载文件名称，后面记得拼接后缀，否则手机没法识别文件类型
	 * @param listener     下载监听
	 * @throws IOException
	 */

	public void download(final String url, String id, final File toFile, final OnDownloadListener listener)
			throws IOException {
		Request request = new Request.Builder().url(url).build();
		Response response = okHttpClient.newCall(request).execute();
		processResponse(toFile, listener, response);
	}

	/**
	 * @param url          下载连接
	 * @param destFileDir  下载的文件储存目录
	 * @param destFileName 下载文件名称，后面记得拼接后缀，否则手机没法识别文件类型
	 * @param listener     下载监听
	 */

	public void asyncDownload(final String url, final String destFileDir, final String destFileName,
			final OnDownloadListener listener) {
		Request request = new Request.Builder().url(url).build();
		// 异步请求
		okHttpClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				// 下载失败监听回调
				listener.onDownloadFailed(e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				processResponse(new File(destFileDir, destFileName), listener, response);
			}
		});
	}

	protected void processResponse(File toFile, final OnDownloadListener listener, Response response) {
		if (response.code() == 404) {
			listener.onDownloadFailed(new FileNotFoundException("文件不存在!"));
			return;
		}
		InputStream is = null;
		byte[] buf = new byte[2048];
		int len = 0;
		FileOutputStream fos = null;

		// 储存下载文件的目录
		if (toFile.isDirectory()) {
			if (!toFile.exists()) {
				toFile.mkdirs();
			}
			toFile = new File(toFile, getHeaderFileName(response));
		}
		if (toFile.getParentFile() != null && !toFile.getParentFile().exists()) {
			toFile.getParentFile().mkdirs();
		}

		if (listener.isFileExists(toFile)) {
			listener.onFileExists(toFile);
			return;
		}

		try {
			File tmpFile = new File(toFile.getParentFile(), toFile.getName() + ".td");
			is = response.body().byteStream();
			long total = response.body().contentLength();
			fos = new FileOutputStream(tmpFile);
			long sum = 0;
			while ((len = is.read(buf)) != -1) {
				fos.write(buf, 0, len);
				sum += len;
				int progress = (int) (sum * 1.0f / total * 100);
				// 下载中更新进度条
				listener.onDownloading(progress);
			}
			fos.flush();
			IOUtils.closeQuietly(fos);
			// 下载完成
			tmpFile.renameTo(toFile);
			listener.onDownloadSuccess(toFile);
		} catch (Exception ex) {
			LogHandler.error(ex);
			listener.onDownloadFailed(ex);
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(fos);
			if (response != null && response.body() != null) {
				IOUtils.closeQuietly(response.body());
			}
		}
	}

	public interface OnDownloadListener {

		boolean isFileExists(File srcFile);

		/**
		 * 下载成功之后的文件
		 */
		void onDownloadSuccess(File file);

		/**
		 * 下载进度
		 */
		void onDownloading(int progress);

		/**
		 * 下载异常信息
		 */

		void onDownloadFailed(Exception e);

		/**
		 * 文件已存在
		 */
		void onFileExists(File file);
	}

	/**
	 * 解析文件头 Content-Disposition:attachment;filename=FileName.txt
	 * Content-Disposition: attachment;
	 * filename*="UTF-8''%E6%9B%BF%E6%8D%A2%E5%AE%9E%E9%AA%8C%E6%8A%A5%E5%91%8A.pdf"
	 */
	private static String getHeaderFileName(Response response) {
		String dispositionHeader = response.header("Content-Disposition");
		if (dispositionHeader != null && !dispositionHeader.isEmpty()) {
			dispositionHeader.replace("attachment;filename=", "");
			dispositionHeader.replace("filename*=utf-8", "");
			String[] strings = dispositionHeader.split(";");
			if (strings.length > 1) {
				dispositionHeader = strings[1].replace("filename=", "");
				dispositionHeader = dispositionHeader.replace("\"", "");
				if (dispositionHeader.length() > 128) {
					boolean endsWith = dispositionHeader.endsWith(".dll.zip");
					String newName = dispositionHeader.trim().substring(0, 128);
					if (endsWith && !newName.endsWith(".dll.zip"))
						return newName + ".dll.zip";
					return newName;
				}
				return dispositionHeader.trim();
			}
			return "";
		}
		return "";
	}
}