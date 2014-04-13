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
public enum WarpPermission {
	
	Warp ("perks.btransported.warp.to"),
	WarpOther ("perks.btransported.warp.to.other"),
	Remove ("perks.btransported.warp.remove"),
	Create ("perks.btransported.warp.create"),
	List ("perks.btransported.warp.list"),
	Gui ("perks.btransported.warp.gui"),
	All ("perks.btransported.warp.all");

	public final String permission;
	WarpPermission(String node) {
		this.permission = node;
	}
}
