/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.codingbadgers.btransported.commands.home;

import org.bukkit.Location;

/**
 *
 * @author N3wton
 */
public class PlayerHome {
	
	public PlayerHome (String name, String owner, Location location) {
		this.name = name;
		this.owner = owner;
		this.location = location;
		this.hash = name.hashCode() + owner.hashCode() + location.hashCode();
	}
	
	public PlayerHome (String name, String owner, Location location, int hash) {
		this.name = name;
		this.owner = owner;
		this.location = location;
		this.hash = hash;
	}
    
    /**
     *
     */
    private String name;
	public String getName() {
		return this.name;
	}

    /**
     *
     */
    private String owner;
	public String getOwnerName() {
		return this.owner;
	}
	
    /**
     *
     */
    private Location location;
	public Location getLocation() {
		return this.location;
	}
	
	/**
     *
     */
    private int hash = 0;
	public int getHash() {
		return this.hash;
	}
}
