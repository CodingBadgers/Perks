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
public enum HomePermission {
	
	Gui ("perks.btransported.home.gui"),
	Set ("perks.btransported.home.set"),
	RemoveOther ("perks.btransported.home.other.remove"),
	HomeOther ("pers.btransported.home.other"),
	Home ("perks.btransported.home");

	public final String permission;
	HomePermission(String node) {
		this.permission = node;
	}
}
