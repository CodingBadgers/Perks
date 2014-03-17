package uk.thecodingbadgers.bkits;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class InventoryAliases {

	public static Map<Material, List<String>> aliases = new HashMap<Material, List<String>>();
	
	private InventoryAliases() {		
	}
	
	public static Material getFromAlias(String alias) {
		Validate.notNull(alias, "Alias cannot be null");
		
		alias = alias.toLowerCase();
		alias = alias.replace(' ', '_');
		
		for (Map.Entry<Material, List<String>> entry : aliases.entrySet()) {
			for (String string : entry.getValue()) {
				if (string.equalsIgnoreCase(alias)) {
					return entry.getKey();
				}
			}
		}
		
		return Material.getMaterial(alias);
	}
	
	public static void loadAliases() throws IOException {
		File configFile = new File(bKits.getInstance().getDataFolder(), "alias.yml");
		
		if (!configFile.exists()) { 
			
			if (!configFile.createNewFile()) {
				throw new IOException("Could not crease aliases config file");
			}
			
			// copy defaults across from jar file
			IOUtils.copy(InventoryAliases.class.getResourceAsStream("/alias.yml"), new FileOutputStream(configFile));
		}
		
		FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		
		for (String key : config.getKeys(false)) {			
			Material mat = Material.matchMaterial(key);
			
			List<String> aliases = config.getStringList(key);
			
			for (String alias : aliases){ 
				alias = alias.toLowerCase();
			}
			
			InventoryAliases.aliases.put(mat, aliases);
			bKits.getInstance().debugConsole("Added " + aliases.size() + " aliases for " + mat.name().toLowerCase());
		}
		
		bKits.getInstance().log(Level.INFO, "Added aliases for " + aliases.size() + " items");
	}
}
