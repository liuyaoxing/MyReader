package offline.export.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

/**
 * @author ex-liuraoxing135
 *
 */
public class ProcessUtils {

	public static ProcessStatus execute(final long timeout, final String... command)
			throws IOException, InterruptedException, TimeoutException {
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectErrorStream(true);
		Process process = pb.start();
		Worker worker = new Worker(process);
		worker.start();
		ProcessStatus ps = worker.getProcessStatus();
		try {
			worker.join(timeout);
			if (ps.exitCode == ProcessStatus.CODE_STARTED) {
				worker.interrupt();
				throw new TimeoutException();
			} else {
				return ps;
			}
		} catch (InterruptedException e) {
			worker.interrupt();
			throw e;
		} finally {
			process.destroy();
		}
	}

	private static class Worker extends Thread {
		private final Process process;
		private final ProcessStatus ps;

		private Worker(Process process) {
			this.process = process;
			this.ps = new ProcessStatus();
		}

		public void run() {
			try {
				InputStream is = process.getInputStream();
				try {
					ps.output = ProcessUtils.toString(is);
				} catch (IOException ignore) {
				}
				ps.exitCode = process.waitFor();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		public ProcessStatus getProcessStatus() {
			return this.ps;
		}
	}

	public static class ProcessStatus {
		public static final int CODE_STARTED = -257;
		public volatile int exitCode = CODE_STARTED;
		public volatile String output;
		
		@Override
		public String toString() {
			return "ProcessStatus [exitCode=" + exitCode + ", output=" + output + "]";
		}
	}

	private static String toString(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		return sb.toString();
	}
}
