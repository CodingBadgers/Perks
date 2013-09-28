package uk.thecodingbadgers.binvested;

import java.util.logging.Level;

import uk.codingbadgers.bFundamentals.module.Module;
import uk.thecodingbadgers.binvested.commands.*;
import uk.thecodingbadgers.binvested.listeners.*;

public class bInvested extends Module {

	private static bInvested instance = null;

	public static bInvested getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;
		
		loadLanguageFile();
		
		registerCommand(new CommandFly());
		registerCommand(new CommandCraft());
		registerCommand(new CommandEnder());
		
		register(new FlyListener(m_database));
		register(new ScubaListener());
		register(new HungerListener(getConfig().getDouble("hunger.rate", 0.25f)));
		
		log(Level.INFO, getName() + " version " + getVersion() + " has been enabled successfully");
	}

	@Override
	public void onDisable() {
		log(Level.INFO, getName() + " version " + getVersion() + " has been disabled successfully");
	}
}
