package offline.export.module.httpserver;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.liuyx.common.db.dao.Mr_FileServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import offline.export.FileUtils;
import offline.export.log.LogHandler;
import offline.export.utils.MD5Utils;
import offline.export.utils.StringUtils;

public class WebServerHandler implements HttpHandler {

	// 编译正则表达式以提高性能
	private static final Pattern PATTERN_IMAGES = Pattern.compile("/images/.*");
	private static final Pattern PATTERN_SCRIPTS = Pattern.compile("/scripts/.*");
	private static final Pattern PATTERN_CSS = Pattern.compile("/css/.*");

	private static final Pattern PATTERN_FILES = Pattern.compile("/files/.*");

	public static final String FILESERVER_NAME = "name";
	public static final String FILESERVER_SIZE = "size";
	public static final String FILESERVER_FILELIST = "fileList";
	public static final String FILESERVER_PATH = "path";
	public static final String FILESERVER_ABSPATH = "absPath";
	public static final String FILESERVER_LENGTH = "length";
	public static final String FILESERVER_MD5 = "md5";
	public static final String TDLL = ".tdll";

	public static final String TEXT_CONTENT_TYPE = "text/html;charset=utf-8";
	public static final String CSS_CONTENT_TYPE = "text/css;charset=utf-8";
	public static final String BINARY_CONTENT_TYPE = "application/octet-stream";
	public static final String ZIP_CONTENT_TYPE = "application/zip";
	public static final String JS_CONTENT_TYPE = "application/javascript";
	public static final String PNG_CONTENT_TYPE = "application/x-png";
	public static final String JPG_CONTENT_TYPE = "application/jpeg";
	public static final String SWF_CONTENT_TYPE = "application/x-shockwave-flash";
	public static final String WOFF_CONTENT_TYPE = "application/x-font-woff";
	public static final String TTF_CONTENT_TYPE = "application/x-font-truetype";
	public static final String SVG_CONTENT_TYPE = "image/svg+xml";
	public static final String EOT_CONTENT_TYPE = "image/vnd.ms-fontobject";
	public static final String MP3_CONTENT_TYPE = "audio/mp3";
	public static final String MP4_CONTENT_TYPE = "video/mpeg4";

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();
		String method = exchange.getRequestMethod();

		try {
			// 1. 处理 OPTIONS 预检请求 (CORS)
			if ("OPTIONS".equalsIgnoreCase(method)) {
				addCorsHeaders(exchange);
				exchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
				exchange.sendResponseHeaders(200, 0);
				exchange.getResponseBody().close();
				return;
			}

			// 2. 处理静态资源: /images/, /scripts/, /css/
			if (method.equals("GET")) {
				if (PATTERN_IMAGES.matcher(path).matches() || PATTERN_SCRIPTS.matcher(path).matches() || PATTERN_CSS.matcher(path).matches()) {
					sendResources(exchange);
					return;
				}
			}

			// 处理文件下载
			if (method.equals("GET") && PATTERN_FILES.matcher(path).matches()) {
				doDownloadFiles(exchange);
				return;
			}

			// 3. 处理具体业务路由
			switch (path) {
				case "/" :
					handleIndexPage(exchange);
					break;
				case "/files" :
					doQueryFiles(exchange);
					break;
				case "/dll/export/list" :
				case "/file/list" :
					doListDllFiles(exchange);
					break;
				default :
					send404(exchange);
			}

		} catch (Exception e) {
			e.printStackTrace();
			// 错误处理
			exchange.sendResponseHeaders(500, -1);
		} finally {
			exchange.close();
		}
	}

	// ================= 辅助方法区 =================

	private static void addCorsHeaders(HttpExchange exchange) {
		exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
	}

	private void sendResources(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();
		BufferedInputStream bInputStream = null;
		try {
			bInputStream = new BufferedInputStream(getClass().getResource(path).openStream());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int len = 0;
			byte[] tmp = new byte[10240];
			while ((len = bInputStream.read(tmp)) > 0) {
				baos.write(tmp, 0, len);
			}
			byte[] bytes = baos.toByteArray();
			String contentType = getContentTypeByResourceName(path);
			if (contentType != null && !contentType.isEmpty()) {
				exchange.getResponseHeaders().set("Content-Type", contentType);
			} else {
				exchange.getResponseHeaders().set("Content-Type", "text/html;charset=utf-8");
			}
			exchange.sendResponseHeaders(200, bytes.length);
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(bytes);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (bInputStream != null) {
				try {
					bInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void doDownloadFiles(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();
		String md5 = path.replace("/files/", "");
		try {
			md5 = URLDecoder.decode(md5, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Mr_FileServer fileServer = FileServerPreferences.queryUploadFile(md5);
		if (fileServer == null || !new File(fileServer.getLocation()).exists()) {
			send404(exchange);
			return;
		}
		File file = new File(fileServer.getLocation());
		if (!file.exists()) {
			send404(exchange);
			return;
		}
		if (file.isFile()) {
			try {
				// 尝试根据文件后缀设置 Content-Type
				String mimeType = Files.probeContentType(file.toPath());
				if (mimeType == null)
					mimeType = "application/octet-stream";

				long fileSize = Files.size(file.toPath());
				exchange.getResponseHeaders().set("Content-Length", String.valueOf(fileSize));
				String encodedFileName = java.net.URLEncoder.encode(file.getName(), "UTF-8");
				exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
				exchange.getResponseHeaders().set("Content-Type", mimeType);
				exchange.sendResponseHeaders(200, fileSize);

				try (RandomAccessFile rafile = new RandomAccessFile(file, "r"); FileChannel fileChannel = rafile.getChannel()) {
					// 将 OutputStream 转换为 WritableByteChannel
					WritableByteChannel channel = Channels.newChannel(exchange.getResponseBody());
					fileChannel.transferTo(0, fileSize, channel);
				} catch (IOException e) {
					System.err.println("文件传输中断: " + e.getMessage());
				} finally {
					exchange.close();
				}
			} catch (IOException e) {
				sendResponse(exchange, 500, "Internal Server Error");
			}
			return;
		} else if (file.isDirectory()) {
			// String clientId = request.getHeaders().get("ClientId");
			// if (StringUtils.isNotEmpty(clientId)) {
			// doExportFolder(request, response);
			// } else {
			// String version = "_v." + new
			// SimpleDateFormat("yyyyMMdd").format(new Date());
			// String zipFileName =
			// DirectoryHelper.getAppCacheDir(getBaseContext(),
			// fileServer.getTitle() + version + ".zip").getCanonicalPath();
			// ZipUtils.zip(file.getPath(), zipFileName);
			// final File sendFile = new File(zipFileName);
			// response.getHeaders().add("Content-Disposition",
			// "attachment;filename=" + URLEncoder.encode(sendFile.getName(),
			// "utf-8"));
			// response.sendFile(sendFile);
			// sendFile.deleteOnExit();
			// return;
			// }
		}
	}

	private void send404(HttpExchange exchange) throws IOException {
		sendResponse(exchange, 404, "404 Not Found");
	}

	private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
		byte[] bytes = response.getBytes("UTF-8");
		exchange.sendResponseHeaders(statusCode, bytes.length);
		try (OutputStream os = exchange.getResponseBody()) {
			os.write(bytes);
		}
	}

	private String getContentTypeByResourceName(String resourceName) {
		if (resourceName.endsWith(".css")) {
			return CSS_CONTENT_TYPE;
		} else if (resourceName.endsWith(".js")) {
			return JS_CONTENT_TYPE;
		} else if (resourceName.endsWith(".swf")) {
			return SWF_CONTENT_TYPE;
		} else if (resourceName.endsWith(".png")) {
			return PNG_CONTENT_TYPE;
		} else if (resourceName.endsWith(".jpg") || resourceName.endsWith(".jpeg")) {
			return JPG_CONTENT_TYPE;
		} else if (resourceName.endsWith(".woff")) {
			return WOFF_CONTENT_TYPE;
		} else if (resourceName.endsWith(".ttf")) {
			return TTF_CONTENT_TYPE;
		} else if (resourceName.endsWith(".svg")) {
			return SVG_CONTENT_TYPE;
		} else if (resourceName.endsWith(".eot")) {
			return EOT_CONTENT_TYPE;
		} else if (resourceName.endsWith(".mp3")) {
			return MP3_CONTENT_TYPE;
		} else if (resourceName.endsWith(".mp4")) {
			return MP4_CONTENT_TYPE;
		}
		return "";
	}

	private void handleIndexPage(HttpExchange exchange) throws IOException {
		try {
			String content = getIndexContent(); // 你的获取首页内容的方法
			byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().set("Content-Type", "text/html;charset=utf-8");
			exchange.sendResponseHeaders(200, bytes.length);
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(bytes);
			}
		} catch (IOException e) {
			e.printStackTrace();
			exchange.sendResponseHeaders(500, -1);
		}
	}

	private void doQueryFiles(HttpExchange exchange) throws IOException {
		// 实现查询文件列表逻辑
		JsonArray array = new JsonArray();
		List<Mr_FileServer> files = FileServerPreferences.queryUploadFiles();
		for (int i = 0; i < files.size(); i++) {
			try {
				Mr_FileServer fileServer = files.get(i);
				if (StringUtils.isEmpty(fileServer.getTitle()) || StringUtils.isEmpty(fileServer.getLocation()))
					continue;
				File srcFile = new File(fileServer.getLocation());
				if (!srcFile.exists())
					continue;
				String fileMD5 = fileServer.getFileMD5();
				String md5 = StringUtils.isEmpty(fileMD5) ? MD5Utils.encryptFileFast(srcFile) : fileMD5;
				JsonObject jsonObject = jsonFileObject(fileServer, srcFile, md5);
				array.add(jsonObject);
			} catch (Exception ex) {
				LogHandler.error(ex);
			}
		}
		byte[] bytes = array.toString().getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
		exchange.sendResponseHeaders(200, bytes.length);
		try (OutputStream os = exchange.getResponseBody()) {
			os.write(bytes);
		}
	}

	private JsonObject jsonFileObject(Mr_FileServer fileServer, File srcFile, String md5) throws Exception {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(FILESERVER_NAME, srcFile.isDirectory() ? "[文件夹]" + srcFile.getName() : srcFile.getName());
		long fileLen = FileUtils.getDirSize(srcFile);
		DecimalFormat df = new DecimalFormat("0.00");
		if (fileLen > 1024 * 1024) {
			jsonObject.addProperty(FILESERVER_SIZE, df.format(fileLen * 1f / 1024 / 1024) + "MB");
		} else if (fileLen > 1024) {
			jsonObject.addProperty(FILESERVER_SIZE, df.format(fileLen * 1f / 1024) + "KB");
		} else {
			jsonObject.addProperty(FILESERVER_SIZE, fileLen + "B");
		}
		jsonObject.addProperty(FILESERVER_PATH, srcFile.getPath());
		jsonObject.addProperty(FILESERVER_MD5, md5);
		jsonObject.addProperty(FILESERVER_LENGTH, srcFile.length());
		String[] listFiles = srcFile.list();
		jsonObject.addProperty(FILESERVER_FILELIST, listFiles == null ? 0 : listFiles.length);
		return jsonObject;
	}

	private static void doListDllFiles(HttpExchange exchange) throws IOException {
		// 实现导出离线文件逻辑
		// 注意：原代码中有两个路径指向同一个方法，这里合并处理
		OutputStream os = exchange.getResponseBody();
		// ... 具体业务逻辑
		exchange.sendResponseHeaders(200, 0);
		os.close();
	}

	// 模拟方法
	private String getIndexContent() throws IOException {
		BufferedInputStream bInputStream = null;
		try {
			bInputStream = new BufferedInputStream(getClass().getResource("/index.html").openStream());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int len = 0;
			byte[] tmp = new byte[10240];
			while ((len = bInputStream.read(tmp)) > 0) {
				baos.write(tmp, 0, len);
			}
			return new String(baos.toByteArray(), "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (bInputStream != null) {
				try {
					bInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
