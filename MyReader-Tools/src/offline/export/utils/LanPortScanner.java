package offline.export.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LanPortScanner {

	private static final int START_PORT = 61666;
	private static final int END_PORT = 61667;
	private static final int NUM_THREADS = 100;

	public static void main(String[] args) throws SocketException {
		long t1 = System.currentTimeMillis();
		System.out.println(Arrays.toString(scan(new ScannerResultCallback() {
			@Override
			public void onSuccess(String ip) {
				System.err.println("发现ip:" + ip + "，耗时:" + (System.currentTimeMillis() - t1));
			}

		}).toArray()));
		System.out.println(System.currentTimeMillis() - t1);
	}

	public static List<String> scan(ScannerResultCallback callback) throws SocketException {
		ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
		List<String> openPorts = new ArrayList<>();

		// 获取本机 IP 地址的网段部分
		List<String> localIps = IPUtil.getAllNonLoopbackAddress();
		Collections.sort(localIps);
		for (String localIp : localIps) {
			String ipPrefix = localIp.substring(0, localIp.lastIndexOf('.') + 1);
			for (int port = START_PORT; port <= END_PORT; port++) {
				final int finalPort = port;
				executorService.execute(() -> {
					try {
						for (int i = 1; i <= 255; i++) {
							String ip = ipPrefix + i;
							System.out.println("扫描ip和端口:" + ip + ":" + finalPort);
							if (isPortOpen(ip, finalPort)) {
								if (callback != null)
									callback.onSuccess(ip + ":" + finalPort);
								openPorts.add(ip + ":" + finalPort);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
		}

		executorService.shutdown();
		try {
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (String openPort : openPorts) {
			System.out.println("开放端口：" + openPort);
		}
		return openPorts;
	}

	private static boolean isPortOpen(String ip, int port) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(ip, port), 100);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public interface ScannerResultCallback {
		public void onSuccess(String ip);
	}
}
