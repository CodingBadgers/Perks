package uk.thecodingbadgers.binvested.listeners;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;

import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;
import uk.thecodingbadgers.binvested.bInvested;

public class FlyListener implements Listener {

	private BukkitDatabase database;
	private String tableName = "bInvested_flying";
	
	public FlyListener(BukkitDatabase db) {
		this.database = db;
		
		if (!db.tableExists("bInvested_flying")) {
			String query = "CREATE TABLE `" + tableName + "` (`name` VARCHAR(16));";
			
			database.query(query, true);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeave(PlayerKickEvent event) {
		if (event.getPlayer().getAllowFlight()) {
			String query = "INSERT INTO `" + tableName + "` VALUES('" + event.getPlayer().getName() + "');";
			database.query(query, true);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeave(PlayerQuitEvent event) {
		if (event.getPlayer().getAllowFlight()) {
			String query = "INSERT INTO `" + tableName + "` VALUES('" + event.getPlayer().getName() + "');";
			database.query(query, true);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String query = "SELECT `name` FROM `" + tableName + "` WHERE name='" + player.getName() + "'";
		ResultSet rs = database.queryResult(query);
		
		try {
			if (rs != null && rs.next()) {
				player.setAllowFlight(true);
				player.setFlying(true);
				
				query = "DELETE FROM `" + tableName + "` WHERE name='" + player.getName() + "';";
				database.query(query, true);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerAttack(EntityDamageEvent entityDamageEvent) {
		
		if (!(entityDamageEvent instanceof EntityDamageByEntityEvent)) {
			return;
		}
		
		EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) entityDamageEvent;
		
		Player attacker = null;
		
		if (event.getDamager() instanceof Player) {
			attacker = (Player) event.getDamager();
		} else if (event.getDamager() instanceof Projectile) {
			Projectile projectile = (Projectile) event.getDamager();
			ProjectileSource source = projectile.getShooter();
			
			if (!(source instanceof Player)) {
				return;
			}
			
			attacker = (Player) source;
		}

		if (attacker != null && attacker.getAllowFlight()) {
			outputMessage(attacker, bInvested.getInstance().getLanguageValue("fly-no-damage"));
			event.setCancelled(true);
		}
	}
	
	private void outputMessage(CommandSender sender, String message) {	
		message = ChatColor.translateAlternateColorCodes('&', message);
		sender.sendMessage(ChatColor.DARK_PURPLE + "[" + bInvested.getInstance().getName() + "] " + ChatColor.RESET + message);
	}
}
