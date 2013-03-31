package uk.thecodingbadgers.btheboss;

import java.util.logging.Level;

import uk.codingbadgers.bFundamentals.module.Module;
import uk.thecodingbadgers.btheboss.commands.*;

public class bTheBoss extends Module {

	private static bTheBoss instance = null;

	public static bTheBoss getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;
		
		loadLanguageFile();
		
		registerCommand(new CommandVanish());
		registerCommand(new CommandClear());
		registerCommand(new CommandCollect());
		registerCommand(new CommandSpeed());
		
		log(Level.INFO, getName() + " version " + getVersion() + " has been enabled successfully");
	}

	@Override
	public void onDisable() {
		log(Level.INFO, getName() + " version " + getVersion() + " has been disabled successfully");
	}
}
