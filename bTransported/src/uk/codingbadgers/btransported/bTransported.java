package uk.codingbadgers.btransported;

import java.util.logging.Level;
import uk.codingbadgers.bFundamentals.module.Module;

/**
 * The Class bTransported.
 * Main entry point to the module
 */
public class bTransported extends Module {
	
	/**
	 * This is called when the module is unloaded
	 */
	@Override
	public void onDisable() {
		log(Level.INFO,  getName() + " version " + getVersion() + " disabled.");
	}

	/**
	 * Called when the module is loaded.
	 * Allowing us to register the player and block listeners
	 */
	@Override
	public void onEnable() {	
		log(Level.INFO,  getName() + " version " + getVersion() + " disabled.");
	}
}
