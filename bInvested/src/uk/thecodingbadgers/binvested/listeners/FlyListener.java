package uk.thecodingbadgers.binvested.listeners;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;

public class FlyListener implements Listener {

	private BukkitDatabase database;
	private String tableName = "bInvested_flying";
	
	public FlyListener(BukkitDatabase db) {
		this.database = db;
		
		if (!db.TableExists("bInvested_flying")) {
			String query = "CREATE TABLE `" + tableName + "` (`name` VARCHAR(16));";
			
			database.Query(query, true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerLeave(PlayerKickEvent event) {
		if (event.getPlayer().isFlying()) {
			String query = "INSERT INTO `" + tableName + "` VALUES(" + event.getPlayer().getName() + ");";
			database.Query(query, true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerLeave(PlayerQuitEvent event) {
		if (event.getPlayer().isFlying()) {
			String query = "INSERT INTO `" + tableName + "` VALUES(" + event.getPlayer().getName() + ");";
			database.Query(query, true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String query = "SELECT `name` FROM `" + tableName + "` WHERE name='" + player.getName() + "'";
		ResultSet rs = database.QueryResult(query);
		
		try {
			if (rs != null && rs.next()) {
				player.setAllowFlight(true);
				player.setFlying(true);
				
				query = "DELETE FROM `" + tableName + "` WHERE name='" + player.getName() + "';";
				database.Query(query, true);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
