/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.codingbadgers.btransported.permissions;

/**
 *
 * @author Sam
 */
public enum SpawnPermission {
	
	Spawn ("perks.btransported.spawn"),
	Other ("perks.btransported.spawn.other"),
	OtherWorld ("perks.btransported.spawn.other.world"),
	Remove ("perks.btransported.spawn.remove"),
	Set ("perks.btransported.spawn.set"),
	Gui ("perks.btransported.spawn.gui");

	public final String permission;
	SpawnPermission(String node) {
		this.permission = node;
	}
}