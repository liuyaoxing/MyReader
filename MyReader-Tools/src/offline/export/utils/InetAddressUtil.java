package offline.export.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class InetAddressUtil {

	private static final String PLATFORMQUALIFIER = "Platform";

	private static final String NETIP = "netip";

	// 转换十进制掩码为IP地址格式掩码
	public static String getMask(short maskBit) {
		int fullbit = maskBit / 8;
		int tail = maskBit % 8;
		String mask = "";
		for (int i = 0; i < fullbit; i++) {
			mask += "255.";
		}
		if (fullbit < 4) {
			int tailNum = (255 << (8 - tail)) & 0xff;
			mask += String.valueOf(tailNum);
			for (int i = 0; i < 4 - fullbit - 1; i++) {
				mask += ".0";
			}
		} else {
			mask = mask.substring(0, mask.length() - 1);
		}
		return mask;
	}

	private static String mask = null;

	/**
	 * 该方法其实是获取网段地址
	 * 
	 * @param ip
	 * @return 网段地址
	 */

	public static String getMask(String ip) {
		if (mask != null)
			return mask;
		// 1. 从System.getProperty()获得
		String maskStr = System.getProperty(PLATFORMQUALIFIER + "." + NETIP, null);
		if (maskStr != null && maskStr.trim().length() > 0) {
			mask = maskStr.trim();
			return mask;
		}
		// 2. 从网络配置中读取
		try {
			return getMaskWith5Jdk(ip);
		} catch (Exception e) {
			return getDefaultMask();
		}
	}

	// 获取配置子网掩码
	public static String getDefaultMask() {
		return "255.255.255.0";
	}

	/**
	 * 1.6以下的jdk调用“ipconfig”得到子网掩码
	 * 
	 * @param ip
	 * @return 子网掩码
	 * @throws Exception
	 */
	private static String getMaskWith5Jdk(String ip) throws Exception {
		Process proc = Runtime.getRuntime().exec("ipconfig", null);
		LineNumberReader lin = new LineNumberReader(new InputStreamReader(proc.getInputStream(), "GBK"));
		String line = lin.readLine();
		boolean found = false;
		String ipStr;
		while (line != null) {
			if (line.indexOf("IP Address") != -1 || line.indexOf("IPv4 地址") != -1) {
				ipStr = line.substring(line.lastIndexOf(":") + 2);
				if (ipStr.equalsIgnoreCase(ip))
					found = true;
			}

			if (found & (line.indexOf("Subnet Mask") != -1 || line.indexOf("子网掩码") != -1)) {
				String maskStr = line.substring(line.lastIndexOf(":") + 2);
				return maskStr;
			}
			line = lin.readLine();
		}
		return getDefaultMask();
	}

	public static String getNetIP(String ipAddr, String maskAddr) {
		// 合法IP、子网掩码，开始计算。
		if (maskAddr.indexOf(".") == -1)
			maskAddr = getMask(Byte.parseByte(maskAddr));
		String[] ipSplit = ipAddr.split("\\.");
		String[] maskSplit = maskAddr.split("\\.");
		String ip = "";
		String mask = "";
		String netIP = "";
		for (int i = 0; i < 4; i++) {
			int ipTemp = Integer.parseInt(ipSplit[i]);
			int maskTemp = Integer.parseInt(maskSplit[i]);
			// 用户输入的IP
			ip = ip.concat(Integer.toString(ipTemp)).concat(".");
			// 用户输入的子网掩码
			mask = mask.concat(Integer.toString(maskTemp)).concat(".");
			// 网络地址
			if (i == 3)
				netIP = netIP.concat(Integer.toString(ipTemp & maskTemp));
			else
				netIP = netIP.concat(Integer.toString(ipTemp & maskTemp)).concat(".");
		}
		return netIP;
	}

	// 判断IP是否合法
	private static boolean isValidIP(String ip) {
		if (ip.indexOf(".") == -1)
			return false;
		String[] ipSplit = ip.split("\\.");
		int ipNum = 0;
		if (ipSplit.length != 4)
			return false;
		for (int i = 0; i < ipSplit.length; i++) {
			try {
				ipNum = Integer.parseInt(ipSplit[i]);
			} catch (Exception e) {
				return false;
			}
			if (ipNum < 0 || ipNum > 255)
				return false;
			if (i == 0)
				if (ipNum == 0 || ipNum == 255)
					return false;
		}
		return true;
	}

	// 判断子网掩码是否合法
	private static boolean isValidMask(String mask) {
		int maskNum = 0;
		int maskBit = 0;
		// 十进制掩码
		if (mask.indexOf(".") == -1) {
			try {
				maskBit = Byte.parseByte(mask);
			} catch (Exception e) {
				return false;
			}
			if (maskBit > 31 || maskBit < 1) {
				return false;
			}
			return true;
		}
		// IP格式掩码
		String[] maskSplit = mask.split("\\.");
		String maskBinString = "";
		if (maskSplit.length != 4)
			return false;
		// 将大于128的4个掩码段连成2进制字符串
		for (int i = 0; i < maskSplit.length; i++) {
			try {
				maskNum = Integer.parseInt(maskSplit[i]);
			} catch (Exception e) {
				return false;
			}
			// 首位为0，非法掩码
			if (i == 0 && Integer.numberOfLeadingZeros(maskNum) == 32)
				return false;
			// 非0或128～255之间，非法掩码
			if (Integer.numberOfLeadingZeros(maskNum) != 24)
				if (Integer.numberOfLeadingZeros(maskNum) != 32)
					return false;
			// 将大于128的掩码段连接成完整的二进制字符串
			maskBinString = maskBinString.concat(Integer.toBinaryString(maskNum));
		}
		// 二进制掩码字符串，包含非连续1时，非法掩码, 注:vpn连接时掩码为255.255.255.255,此时广播自己也不合法
		if (maskBinString.indexOf("0") < maskBinString.lastIndexOf("1"))
			return false;
		// 剩下的就是合法掩码
		return true;
	}

	public static List<String> getBroadcastIP() throws IOException {
		List<String> localIps = IPUtil.getAllNonLoopbackAddress();
		List<String> broadcastIps = new ArrayList<>();
		for (String clientIp : localIps) {
			broadcastIps.add(getBroadcastIP(clientIp, getMask(clientIp)));
		}
		return broadcastIps;
		// return getBroadcastIP(getClientIp(), getMask(getClientIp()));
	}

	/**
	 * 根据ip地址与子网掩码计算出广播地址用于UDP广播
	 * 
	 * @param ip
	 *            ip地址
	 * @param mask
	 *            子网掩码
	 * @return 广播地址
	 */
	private static String getBroadcastIP(String ip, String mask) {
		String broadcastIP = "非法IP或子网掩码地址。";
		// 非法IP或子网掩码地址，不进行计算。
		if (!isValidIP(ip) || !isValidMask(mask))
			return broadcastIP;
		broadcastIP = "";
		if (mask.indexOf(".") == -1)
			mask = getMask(Byte.parseByte(mask));
		// ip = "192.9.203.221";
		String[] ipSplit = ip.split("\\.");
		String[] maskSplit = mask.split("\\.");
		// 假如计算机的IP位址是192.15.156.205，子网掩码是255.255.255.224，
		// 先把子网掩码255.255.255.224做 NOT 运算﹐可以得出﹕
		// 00000000.00000000.00000000.00011111
		// 然后再和IP做一次 OR 运算﹐就可以得到 Broadcast Address:
		// 11000000.00001111.10011100.11001101 OR
		// 00000000.00000000.00000000.00011111
		// (192.15.156.205 OR 255.255.255.224)
		// 得出﹕ 11000000.00001111.10011100.11011111
		// (192.15.156.223)
		// 192.15.156.223就是那个子网的广播地址了
		for (int i = 0; i < 4; i++) {
			int ipTemp = Integer.parseInt(ipSplit[i]);
			int maskTemp = Integer.parseInt(maskSplit[i]);
			// 广播地址
			broadcastIP = broadcastIP.concat(Integer.toString(~maskTemp + 256 | ipTemp)).concat(".");
		}
		return broadcastIP.substring(0, broadcastIP.length() - 1);
	}

	public static boolean canBroadcast() {
		List<String> localIps = IPUtil.getAllNonLoopbackAddress();
		for (String clientIp : localIps) {
			if (isValidIP(clientIp) && isValidMask(getMask(clientIp)))
				return true;
		}
		return false;
	}

}
