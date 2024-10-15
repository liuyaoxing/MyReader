package offline.export.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.liuyx.common.csv.CsvUtil;

public class BroadcastUtil {

	public static List<Map<String, String>> broadcast(Map<String, String> map, String boradcastIp, int port) {
		long t1 = System.currentTimeMillis();
		DatagramSocket dgSocket = null;
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		try {
			dgSocket = new DatagramSocket();
			byte msg[] = CsvUtil.mapToCsv(map).getBytes();
			DatagramPacket dgPacket = new DatagramPacket(msg, msg.length, InetAddress.getByName(boradcastIp), port);
			System.out.println("准备广播，广播地址：" + boradcastIp + ":" + port + "，消息：" + map);
//			System.out.println("获得的本机ip：" + Arrays.toString(IPUtil.getAllNonLoopbackAddress().toArray()));
			dgSocket.send(dgPacket);
			msg = new byte[256];
			DatagramPacket packet = new DatagramPacket(msg, msg.length);
			dgSocket.setSoTimeout(100);
			dgSocket.receive(packet);
			String str = new String(packet.getData(), 0, packet.getLength());
			long costTime = System.currentTimeMillis() - t1;
			// 如果没有，就不回
			System.out.println("收到广播查询结果：" + str + "，来自：" + packet.getAddress() + ":" + packet.getPort() + ",耗时:" + costTime);
			Map<String, String> ret = CsvUtil.csvToMap(str);
			list.add(ret);
			return list;
		} catch (SocketTimeoutException e) {
			System.out.println("广播超时，耗时:" + (System.currentTimeMillis() - t1));
			return list;
		} catch (IOException e) {
			e.printStackTrace();
			return list;
		} finally {
			if (dgSocket != null)
				dgSocket.close();
		}
	}
}
