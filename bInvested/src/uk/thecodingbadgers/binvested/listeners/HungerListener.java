package uk.thecodingbadgers.binvested.listeners;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.thecodingbadgers.binvested.bInvested;

public class HungerListener implements Listener {

	public double hungerRate = 0f;
	
	public HungerListener(double d) {
		hungerRate = d;
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		
		Player player = (Player) event.getEntity();
		
		if (!bInvested.hasPermission(player, "perks.binvested.hunger")) {
			return;
		}
		
		List<MetadataValue> meta = player.getMetadata("bInvested-hungerCounter");
		
		if (meta.size() < 1) {
			player.setMetadata("bInvested-hungerCounter", new FixedMetadataValue(bFundamentals.getInstance(), 0.0f));
			event.setCancelled(true);
			return;
		}
		
		float hungerCounter = meta.get(0).asFloat();
		
		hungerCounter += hungerRate;
		
		if (hungerCounter >= 1f) {
			hungerCounter = 0f;
			event.setCancelled(false);
		} else {
			event.setCancelled(true);
		}
		
		player.removeMetadata("bInvested-hungerCounter", bFundamentals.getInstance());
		player.setMetadata("bInvested-hungerCounter", new FixedMetadataValue(bFundamentals.getInstance(), hungerCounter));
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		
		Player player = (Player) event.getEntity();

		if (!bInvested.hasPermission(player, "perks.binvested.hunger")) {
			return;
		}
		
		if (!player.hasMetadata("bInvested-hungerCounter")) {
			return;
		}
		
		player.removeMetadata("bInvested-hungerCounter", bFundamentals.getInstance());
		player.setMetadata("bInvested-hungerCounter", new FixedMetadataValue(bFundamentals.getInstance(), 0.0f));
	}

}
