package uk.codingbadgers.btransported.commands;

import org.bukkit.Location;

public class NamedLocation {
	
	public String name;
	public Location location;
	
	public NamedLocation(String locName, Location loc) {
		this.name = locName;
		this.location = loc;
	}
}
