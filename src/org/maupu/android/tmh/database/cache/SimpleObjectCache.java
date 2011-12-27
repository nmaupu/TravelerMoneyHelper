package org.maupu.android.tmh.database.cache;

import java.util.HashMap;
import java.util.Map;

import org.maupu.android.tmh.database.object.BaseObject;

/**
 * Class providing a simple DTO cache
 * @author nmaupu
 */
public class SimpleObjectCache {
	private Map<String, BaseObject> contents = new HashMap<String, BaseObject>();
	
	public void addDTO(BaseObject dto) {
		contents.put(constructKey(dto.getTableName(), dto.getId()), dto);
	}
	
	public void removeDTO(BaseObject dto) {
		removeDTO(constructKey(dto.getTableName(), dto.getId()));
	}
	
	public void removeDTO(String key) {
		contents.remove(key);
	}
	
	public void clearCache() {
		contents.clear();
	}
	
	public BaseObject getBaseDTO(String key) {
		return contents.get(key);
	}
	
	public static String constructKey(String tableName, Integer id) {
		return tableName+id;
	}
}
