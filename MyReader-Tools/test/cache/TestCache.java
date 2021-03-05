package cache;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import offline.export.bigcache.BigCache;
import offline.export.bigcache.CacheConfig;
import offline.export.bigcache.CacheConfig.StorageMode;
import offline.export.bigcache.storage.FileChannelStorage;

public class TestCache {

	private static final double STRESS_FACTOR = Double.parseDouble(System.getProperty("STRESS_FACTOR", "1.0"));

	private static final String TEST_DIR = "bigcache";

	public static void main(String[] args) throws IOException {
		CacheConfig config = new CacheConfig();
		config.setConcurrencyLevel(1);
		config.setInitialNumberOfBlocks(1);
		config.setPurgeInterval(1 * 60 * 1000);// 1∑÷÷”ª∫¥Ê“ª¥Œ
		config.setCapacityPerBlock(16 * 1024 * 1024);// 10M
		config.setStorageMode(StorageMode.MemoryMappedPlusFile);

		BigCache<String> cache = new BigCache<String>(TEST_DIR, config);

//		for (int i = 0; i < 10000; i++) {
//			cache.put(String.valueOf(i), String.valueOf(System.currentTimeMillis()).getBytes());
//		}

		for (int i = 0; i < 10000; i++) {
			System.out.println(cache.get(String.valueOf(i)));
		}

//		cache.close();
	}
}
