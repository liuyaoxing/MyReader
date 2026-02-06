package offline.export.module.httpserver;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liuyx.common.db.dao.Mr_FileServer;

public class FileServerPreferences {

	private static final String PREFS_NODE = "myreader.tools";

	private static final String KEY_FILESERVER = "fileServer";

	public static void addFileServer(Mr_FileServer fileServer) throws BackingStoreException {
		Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
		String fileServerJson = prefs.get(KEY_FILESERVER, "");
		Gson gson = new Gson();
		Type listMapType = new TypeToken<List<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> listMap = gson.fromJson(fileServerJson, listMapType);
		if (listMap == null)
			listMap = new ArrayList<>();
		listMap.add(fileServer.getAttributeMap());
		String json = gson.toJson(listMap);
		prefs.put(KEY_FILESERVER, json);
		prefs.flush();
	}

	public static List<Mr_FileServer> queryUploadFiles() {
		Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
		String fileServerJson = prefs.get(KEY_FILESERVER, "");
		Gson gson = new Gson();
		Type listMapType = new TypeToken<List<Map<String, String>>>() {
		}.getType();
		List<Map<String, String>> listMap = gson.fromJson(fileServerJson, listMapType);
		if (listMap == null || listMap.isEmpty())
			return Collections.emptyList();
		List<Mr_FileServer> fileServerList = new ArrayList<>(listMap.size());
		for (Map<String, String> map : listMap) {
			fileServerList.add(new Mr_FileServer(map));
		}
		return fileServerList;
	}

	public static List<Mr_FileServer> queryUploadFiles(Map<String, String> whereMap) {
		List<Mr_FileServer> listMap = queryUploadFiles();
		List<Mr_FileServer> result = listMap.stream().filter(map -> whereMap.entrySet().stream()//
				.allMatch(e -> Objects.equals(e.getValue(), map.get(e.getKey())))).collect(Collectors.toList());
		return result;
	}

	public static Mr_FileServer queryUploadFile(String md5) {
		Map<String, String> whereMap = new HashMap<>();
		whereMap.put(Mr_FileServer.MD5, md5);
		List<Mr_FileServer> files = queryUploadFiles(whereMap);
		if (files != null && files.size() > 0)
			return files.get(0);
		return null;
	}

	public static void clearFileServers() {
		Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
		prefs.remove(KEY_FILESERVER);
	}
}
