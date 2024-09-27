package offline.export.utils;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class IPUtil {

	public static String getAllLocalAddress() {
		List<String> address = getAllNonLoopbackAddress();
		return convert(address);
	}

	public static String getAllIpv6Address() {
		List<String> address = getAllINet6Address();
		return convert(address);
	}

	private static List<String> getAllINet6Address() {
		List<String> ips = new ArrayList<>();
		try {
			Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {

				NetworkInterface networkInterface = netInterfaces.nextElement();

				if (networkInterface.isLoopback() || !networkInterface.isUp()) {
					continue;
				}

				String netDisplayName = networkInterface.getDisplayName();
				if (netDisplayName != null && ("docker".equals(netDisplayName) || "virbr0".equals(netDisplayName)
						|| netDisplayName.startsWith("VirtualBox") || netDisplayName.startsWith("VMware")
						|| netDisplayName.startsWith("Hyper-V"))) {
					continue;
				}

				networkInterface.getInterfaceAddresses().stream().map(InterfaceAddress::getAddress).filter(a -> {
					return a instanceof Inet6Address;
				}).forEach(s -> ips.add(s.getHostAddress()));
			}
		} catch (SocketException e) {

		}
		return ips;
	}

	static List<String> getAllNonLoopbackAddress() {
		List<String> ips = new ArrayList<String>();
		try {
			final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				final NetworkInterface nic = interfaces.nextElement();
				final Enumeration<InetAddress> addresses = nic.getInetAddresses();
				while (addresses.hasMoreElements()) {
					final InetAddress address = addresses.nextElement();
					if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
						final String ip = address.getHostAddress();
						if (ip != null) {
							ips.add(ip);
						}
					}
				}
			}
		} catch (final SocketException se) {
			// swallow and ignore
		}

		return ips;
	}

	private static String convert(List<String> address) {
		return address.stream().collect(Collectors.joining(","));
	}

}
