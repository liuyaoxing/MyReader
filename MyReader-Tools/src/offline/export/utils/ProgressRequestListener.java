package offline.export.utils;

public interface ProgressRequestListener {
	void onRequestProgress(long bytesWritten, long contentLength, boolean done);
}
