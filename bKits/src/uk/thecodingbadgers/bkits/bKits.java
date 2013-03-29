package uk.thecodingbadgers.bkits;

import java.util.logging.Level;

import uk.codingbadgers.bFundamentals.module.Module;
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
	
	/* 
	 * @see uk.codingbadgers.bFundamentals.module.Module#onLoad()
	 */
	@Override
	public void onLoad() {
		instance = this;
	}
	
	/* 
	 * @see uk.codingbadgers.bFundamentals.module.Module#onDisable()
	 */
	@Override
	public void onDisable() {
		log(Level.INFO,  getName() + " version " + getVersion() + " disabled.");
	}

	/* 
	 * @see uk.codingbadgers.bFundamentals.module.Module#onEnable()
	 */
	@Override
	public void onEnable() {
		registerCommand(new KitCommand());
		
		loadLanguageFile();
		KitHandler.getInstance().loadKits();
		
		log(Level.INFO,  getName() + " version " + getVersion() + " enabled.");	
	}

}
