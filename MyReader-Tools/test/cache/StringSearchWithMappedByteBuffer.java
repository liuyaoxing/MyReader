package cache;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class StringSearchWithMappedByteBuffer {
	public static void main(String[] args) {
		String filePath = "D:\\Developer\\DeveloperWorks\\Android\\MyReader\\zz.format\\largeFile.txt";
		String targetString = "刘兴兴";
		try (RandomAccessFile raf = new RandomAccessFile(new File(filePath), "r"); FileChannel fc = raf.getChannel()) {
			MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			byte[] targetBytes = targetString.getBytes();
			for (int i = 0; i <= mbb.limit() - targetBytes.length; i++) {
				boolean found = true;
				for (int j = 0; j < targetBytes.length; j++) {
					if (mbb.get(i + j) != targetBytes[j]) {
						found = false;
						break;
					}
				}
				if (found) {
					System.out.println("Found '" + targetString + "' at position " + i);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
