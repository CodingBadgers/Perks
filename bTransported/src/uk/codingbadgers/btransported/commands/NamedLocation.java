package uk.codingbadgers.btransported.commands;

import org.bukkit.Location;

/**
 *
 * @author Sam
 */
public class NamedLocation {

    /**
     *
     */
    public String name;

    /**
     *
     */
    public Location location;

    /**
     *
     * @param locName
     * @param loc
     */
    public NamedLocation(String locName, Location loc) {
		this.name = locName;
		this.location = loc;
	}
}
