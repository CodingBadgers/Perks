package uk.thecodingbadgers.bkits.kit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import uk.thecodingbadgers.bkits.bKits;

/**
 * The Class KitHandler.
 */
public class KitHandler {

	/** The Constant instance. */
	private static final KitHandler instance = new KitHandler();

	/** The kits. */
	private List<Kit> kits = new ArrayList<Kit>();
	
	/**
	 * Gets the single instance of KitHandler.
	 *
	 * @return single instance of KitHandler
	 */
	public static KitHandler getInstance() {
		return instance;
	}
	
	/**
	 * Adds a kit.
	 *
	 * @param kit the kit
	 */
	public void addKit(Kit kit) {
		this.kits.add(kit);
	}
	
	/**
	 * Handle kit.
	 *
	 * @param player the player
	 * @param kitName the kit name
	 * @return true if handled successfully, false otherwise
	 */
	public boolean handleKit(Player player, String kitName) {
		for (Kit kit : kits) {
			if (!kit.getName().equalsIgnoreCase(kitName)) {
				continue;
			}
			
			kit.give(player);
			return true;
		}
		return false;
	}

	/**
	 * Load kits.
	 */
	public void loadKits() {
		File kitConfig = new File(bKits.getInstance().getDataFolder(), "kits.yml");
		
		if (!kitConfig.exists()) {
			try {
				kitConfig.createNewFile();
			} catch (IOException e) {
				bKits.getInstance().getLogger().severe(bKits.getInstance().getLanguageValue("kits-create-error"));
				e.printStackTrace();
				return;
			}
		}

		try {
			KitParser parser = new KitParser(kitConfig);
			parser.parseKits();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The amount of kits registed.
	 *
	 * @return the amount of kits
	 */
	public static int size() {
		return getInstance().kits.size();
	}

	/**
	 * Output kits to a player.
	 *
	 * @param player the player
	 */
	public void outputKits(Player player) {
		String kitsText = bKits.getInstance().getLanguageValue("list");
		
		for (Kit kit : kits) {
			kitsText += kit.getName() + " ";
		}
		
		bKits.sendMessage(bKits.getInstance().getName(), player, kitsText);
	}
}
