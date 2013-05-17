package uk.thecodingbadgers.binvested.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import uk.thecodingbadgers.binvested.bInvested;

public class ScubaListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDamage(EntityDamageEvent event) {
		
		if (!(event.getEntity() instanceof Player) || event.getCause() != DamageCause.DROWNING) {
			return;
		}
		
		Player player = (Player) event.getEntity();
		
		if (!bInvested.hasPermission(player, "perks.binvested.scuba")) {
			return;
		}
		
		if (!(player.getEquipment().getHelmet().getType() == Material.GOLD_HELMET 
				|| bInvested.hasPermission(player, "perks.binvested.scuba.plus")))  {
			return;
		}
		
		event.setCancelled(true);
		event.setDamage(0);
		player.setRemainingAir(20);
	}
}
