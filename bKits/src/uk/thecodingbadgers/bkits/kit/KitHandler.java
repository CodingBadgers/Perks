package uk.thecodingbadgers.bkits.kit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.codingbadgers.bFundamentals.player.FundamentalPlayer;
import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;
import uk.thecodingbadgers.bkits.bKits;
import uk.thecodingbadgers.bkits.commands.KitCommand.CommandResult;
import uk.thecodingbadgers.bkits.kit.parser.ParserType;

/**
 * The Class KitHandler.
 */
public class KitHandler {

	public static final String TABLE_NAME = bFundamentals.getConfigurationManager().getDatabaseSettings().prefix + "kits";
	private static final KitHandler instance = new KitHandler();
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
	public CommandResult handleKit(Player player, String kitName) {
		CommandResult result = CommandResult.NOT_FOUND;
		
		for (Kit kit : kits) {
			if (!kit.getName().equalsIgnoreCase(kitName)) {
				continue;
			}
			
			FundamentalPlayer fPlayer = bFundamentals.Players.getPlayer(player);
			KitPlayerData data = (KitPlayerData) fPlayer.getPlayerData(KitPlayerData.class);
			
			if (data == null) {
				data = new KitPlayerData(player.getName());
			}
			 
			
			if (data.canUse(kit)) {
				kit.give(player);
				data.addKitTimeout(kit);
				result = CommandResult.GIVEN;
			} else {
				result = CommandResult.TIMEOUT;
			}
			
			fPlayer.addPlayerData(data);
		}
		
		return result;
	}

	public void setup() {
		loadKits();
		loadPlayerTimeouts();
	}

	/**
	 * Load kits.
	 */
	public void loadKits() {
		File[] files = bKits.getInstance().getDataFolder().listFiles(new KitFilenameFilter());
		
		for (File kitConfig : files) {
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
				kits.addAll(ParserType.parse(kitConfig));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Load the current timeouts for the players on the server.
	 */
	public void loadPlayerTimeouts() {
		BukkitDatabase database = bKits.getDatabase();
		
		if (!database.tableExists(TABLE_NAME)) {
			bKits.getInstance().log(Level.INFO, "Creating table " + TABLE_NAME);
			String query = "CREATE TABLE " + TABLE_NAME + "(" +
							"`player` VARCHAR (16) NOT NULL," +
							"`kit` VARCHAR (16) NOT NULL," +
							"`timeout` LONG);";
			database.query(query, true);
		}
		
		
	}

	/**
	 * The amount of kits registered.
	 *
	 * @return the amount of kits registered.
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
		StringBuilder kitsText = new StringBuilder();
		kitsText.append(bKits.getInstance().getLanguageValue("list"));
		
		for (Kit kit : kits) {
			if (bKits.hasPermission(player, "perks.bkits.kits." + kit.getName())) {
				kitsText.append(kit.getName() + " ");
			}
		}
		
		bKits.sendMessage(bKits.getInstance().getName(), player, kitsText.toString());
	}
}
