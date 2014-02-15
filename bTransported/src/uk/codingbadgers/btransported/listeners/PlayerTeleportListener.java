package uk.codingbadgers.btransported.listeners;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.btransported.bTransported;

public class PlayerTeleportListener implements Listener {
	
	/**
	 * The bTransported module, used for access to configuration files and such
	 */
	private bTransported m_module = null;
	
	/**
	 * Class constructor
	 * @param module The bTransported module
	 */
	public PlayerTeleportListener(bTransported module) {
		m_module = module;
	}

	/**
	 * The player teleport event callback
	 * @param event The event to process
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		
		final Player player = event.getPlayer();
		
		if (player.getGameMode() == GameMode.CREATIVE) {
			return;
		}
		
		if (!m_module.getTeleportationConfig().getBoolean("teleport-protection-enabled")) {
			return;
		}
		
		final TeleportCause cause = event.getCause();
		final Location from = event.getFrom();
		final Location destination = event.getTo();
				
		// If the player is not in creative and the block below them is unsafe, cancel the teleport
		if (!isTeleportSafe(player, destination)) {
			event.setCancelled(true);
			return;
		}
		
		// If the player is not in creative and the teleport cause is blacklisted in that world cancel
		if (isBlacklistedTeleport(player, from, cause)) {
			event.setCancelled(true);
			return;
		}
	
	}
	
	/**
	 * 
	 * @param player
	 * @param location
	 * @param cause
	 * @return
	 */
	private boolean isBlacklistedTeleport(final Player player, final Location location, final TeleportCause cause) {
		
		FileConfiguration teleportConfiguration = m_module.getTeleportationConfig();
		
		String configNode = null;
		switch (cause)
		{
		case COMMAND:
			configNode = "disable-in-world.teleport-command";
			break;
		case ENDER_PEARL:
			configNode = "disable-in-world.teleport-enderpearl";
			break;
		case END_PORTAL:
			configNode = "disable-in-world.teleport-endportal";
			break;
		case NETHER_PORTAL:
			configNode = "disable-in-world.teleport-neatherportal";
			break;
		case PLUGIN:
			configNode = "disable-in-world.teleport-plugin";
			break;
		default:
			return false;
		}
		
		List<String> worlds = teleportConfiguration.getStringList(configNode);
		if (worlds.contains(location.getWorld().getName())) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("CANCEL-TELEPORT-TYPE-BLACKLISTED"));
			return true;
		}
		
		return false;
	}

	/**
	 * 
	 * @param player
	 * @param destination
	 * @return
	 */
	private boolean isTeleportSafe(final Player player, final Location destination) {

		// Always save in creative mode!
		if (player.getGameMode() == GameMode.CREATIVE)
			return true;
		
		final FileConfiguration teleportConfiguration = m_module.getTeleportationConfig();

		if (!player.isFlying()) {
			int maxFallDistance = teleportConfiguration.getInt("maximum-fall-distance");
			Location hightestBlock = new Location(destination.getWorld(), destination.getX(), destination.getY(), destination.getBlockZ());
			while (hightestBlock.getBlock().getType() == Material.AIR) {
				hightestBlock = hightestBlock.add(0.0, -1.0, 0.0);
				maxFallDistance--;
				if (maxFallDistance < 0) {
					Module.sendMessage("bTransported", player, m_module.getLanguageValue("CANCEL-TELEPORT-FALL-DISTANCE"));
					return false;
				}
			}
			
			List<String> blacklistedblocks = teleportConfiguration.getStringList("dangerous-blocks");
			if (blacklistedblocks.contains(hightestBlock.getBlock().getType().name())) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("CANCEL-TELEPORT-DANGEROUS"));
				return false;
			}
		}
		
		List<String> blacklistedblocks = teleportConfiguration.getStringList("dangerous-blocks");
		if (blacklistedblocks.contains(destination.getBlock().getType().name())) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("CANCEL-TELEPORT-DANGEROUS"));
			return false;
		}
		
		return true;
	}
	
}
