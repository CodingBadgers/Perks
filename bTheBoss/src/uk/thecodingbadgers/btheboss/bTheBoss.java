package uk.thecodingbadgers.btheboss;

import java.io.IOException;
import java.util.logging.Level;

import uk.codingbadgers.bFundamentals.module.Module;
import uk.thecodingbadgers.btheboss.commands.*;
import uk.thecodingbadgers.btheboss.inventory.InventoryAliases;

public class bTheBoss extends Module {

	private static bTheBoss instance = null;

	public static bTheBoss getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;
		
		try {
			loadLanguageFile();
			InventoryAliases.loadAliases();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		registerCommand(new CommandVanish());
		registerCommand(new CommandClear());
		registerCommand(new CommandCollect());
		registerCommand(new CommandSpeed());
		registerCommand(new CommandInventory());
		
		log(Level.INFO, getName() + " version " + getVersion() + " has been enabled successfully");
	}

	@Override
	public void onDisable() {
		log(Level.INFO, getName() + " version " + getVersion() + " has been disabled successfully");
	}
}
