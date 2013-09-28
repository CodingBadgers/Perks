package uk.thecodingbadgers.bkits;

import java.util.logging.Level;

import uk.codingbadgers.bFundamentals.module.Module;
import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;
import uk.thecodingbadgers.bkits.commands.KitCommand;
import uk.thecodingbadgers.bkits.kit.KitHandler;

/**
 * The Class bKits.
 * Main entry point to the module.
 */
public class bKits extends Module {

	/** The instance. */
	private static bKits instance;
	
	/**
	 * Gets the single instance of bKits.
	 *
	 * @return single instance of bKits
	 */
	public static bKits getInstance() {
		return instance;
	}
	
	/**
	 * Gets the database associated with this module.
	 * 
	 * @return the database associated with this module.
	 */
	public static BukkitDatabase getDatabase() {
		return m_database;
	}
	
	@Override
	public void onLoad() {
		instance = this;
	}
	
	@Override
	public void onDisable() {
		log(Level.INFO,  getName() + " version " + getVersion() + " disabled.");
	}
	
	@Override
	public void onEnable() {
		registerCommand(new KitCommand());
		
		loadLanguageFile();
		KitHandler.getInstance().setup();
		
		log(Level.INFO,  getName() + " version " + getVersion() + " enabled.");	
	}

}
