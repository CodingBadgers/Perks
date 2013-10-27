package uk.codingbadgers.btransported.listeners;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
		final TeleportCause cause = event.getCause();
		final Location from = event.getFrom();
		final Location destination = event.getTo();
		
		if (player.getGameMode() == GameMode.CREATIVE) {
			return;
		}
				
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

	private boolean isTeleportSafe(final Player player, final Location destination) {
		
		final Block block = destination.getBlock();

		FileConfiguration teleportConfiguration = m_module.getTeleportationConfig();
		
		if (teleportConfiguration.getBoolean("safe-blocks.enabled")) {
			List<Integer> whitelistedblocks = teleportConfiguration.getIntegerList("safe-blocks.blocks");
			if (!whitelistedblocks.contains(block.getTypeId())) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("CANCEL-TELEPORT-NONSAFE"));
				return false;
			}
		}

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
			
			List<Integer> blacklistedblocks = teleportConfiguration.getIntegerList("dangerous-blocks");
			if (blacklistedblocks.contains(hightestBlock.getBlock().getTypeId())) {
				Module.sendMessage("bTransported", player, m_module.getLanguageValue("CANCEL-TELEPORT-DANGEROUS"));
				return false;
			}
		}
		
		return true;
	}
	
}
