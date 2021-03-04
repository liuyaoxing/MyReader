package offline.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * �ļ����ع����ࣨ����ģʽ��
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
				.connectTimeout(620, TimeUnit.SECONDS) // ���ӳ�ʱ
				.readTimeout(620, TimeUnit.SECONDS) // ��ȡ��ʱ
				.writeTimeout(60, TimeUnit.SECONDS) // д��ʱ
				.writeTimeout(60, TimeUnit.SECONDS) // д��ʱ
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
	 * @param url          ��������
	 * @param destFileDir  ���ص��ļ�����Ŀ¼
	 * @param destFileName �����ļ����ƣ�����ǵ�ƴ�Ӻ�׺�������ֻ�û��ʶ���ļ�����
	 * @param listener     ���ؼ���
	 * @throws IOException
	 */

	public void download(final String url, String id, final String destFileDir, final OnDownloadListener listener)
			throws IOException {
		Request request = new Request.Builder().url(url).build();
		Response response = okHttpClient.newCall(request).execute();
		processResponse(destFileDir, null, listener, response);
	}

	/**
	 * @param url          ��������
	 * @param destFileDir  ���ص��ļ�����Ŀ¼
	 * @param destFileName �����ļ����ƣ�����ǵ�ƴ�Ӻ�׺�������ֻ�û��ʶ���ļ�����
	 * @param listener     ���ؼ���
	 */

	public void asyncDownload(final String url, final String destFileDir, final String destFileName,
			final OnDownloadListener listener) {
		Request request = new Request.Builder().url(url).build();
		// �첽����
		okHttpClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				// ����ʧ�ܼ����ص�
				listener.onDownloadFailed(e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				processResponse(destFileDir, destFileName, listener, response);
			}
		});
	}

	protected void processResponse(final String destFileDir, final String destFileName,
			final OnDownloadListener listener, Response response) {
		InputStream is = null;
		byte[] buf = new byte[2048];
		int len = 0;
		FileOutputStream fos = null;

		// ���������ļ���Ŀ¼
		File dir = new File(destFileDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = null;
		if (destFileName == null || destFileName.isEmpty()) {
			file = new File(dir, getHeaderFileName(response));
		} else {
			file = new File(dir, destFileName);
		}
		try {
			is = response.body().byteStream();
			long total = response.body().contentLength();
			fos = new FileOutputStream(file);
			long sum = 0;
			while ((len = is.read(buf)) != -1) {
				fos.write(buf, 0, len);
				sum += len;
				int progress = (int) (sum * 1.0f / total * 100);
				// �����и��½�����
				listener.onDownloading(progress);
			}
			fos.flush();
			// �������
			listener.onDownloadSuccess(file);
		} catch (Exception e) {
			listener.onDownloadFailed(e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {

			}
		}
	}

	public interface OnDownloadListener {

		/**
		 * ���سɹ�֮����ļ�
		 */
		void onDownloadSuccess(File file);

		/**
		 * ���ؽ���
		 */
		void onDownloading(int progress);

		/**
		 * �����쳣��Ϣ
		 */

		void onDownloadFailed(Exception e);
	}

	/**
	 * �����ļ�ͷ Content-Disposition:attachment;filename=FileName.txt
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
				return dispositionHeader.trim();
			}
			return "";
		}
		return "";
	}
}