package uk.thecodingbadgers.bkits.kit;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.codingbadgers.bFundamentals.player.FundamentalPlayer;
import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;
import uk.thecodingbadgers.bkits.bKits;

public class TimeoutHandler implements Listener {

	/* Events */
	
	@EventHandler(priority = EventPriority.HIGHEST) // Hacky work around the player getting created at NORMAL 
	public void playerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		FundamentalPlayer fPlayer = bFundamentals.Players.getPlayer(player);
		loadPlayerTimeouts(fPlayer);
	}

	@EventHandler(priority = EventPriority.LOWEST) // Hacky work around the player getting destroyed at NORMAL 
	public void playerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		FundamentalPlayer fPlayer = bFundamentals.Players.getPlayer(player);
		updatePlayerTimeouts(fPlayer);
	}

	@EventHandler(priority = EventPriority.LOWEST) // Hacky work around the player getting destroyed at NORMAL 
	public void playerKick(PlayerKickEvent event) {
		Player player = event.getPlayer();
		FundamentalPlayer fPlayer = bFundamentals.Players.getPlayer(player);
		updatePlayerTimeouts(fPlayer);
	}
	
	/* Logic */
	public void loadPlayerTimeouts(FundamentalPlayer player) {
		BukkitDatabase database = bKits.getDatabase();
		
		String query = "SELECT * FROM " + KitHandler.TABLE_NAME + " WHERE `player` LIKE '" + player.getPlayer().getName() + "'";
		ResultSet result = database.queryResult(query);
		
		try { 
			KitPlayerData data = new KitPlayerData(player.getPlayer().getName());
			
			while(result.next()) {
				String kitname = result.getString("kit");
				long endtime = result.getLong("timeout");
				data.addKitTimeout(kitname, endtime);
			}
			
			player.addPlayerData(data);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void updatePlayerTimeouts(FundamentalPlayer player) {
		BukkitDatabase database = bKits.getDatabase();
		KitPlayerData data = player.getPlayerData(KitPlayerData.class);
		
		for (String kit : data.getKits()) {
			
			String query = "SELECT * FROM " + KitHandler.TABLE_NAME + " WHERE `player` LIKE '" + player.getPlayer().getName() + "';";
			ResultSet result = database.queryResult(query);
			
			try {
				if (result == null || !result.next()) {
					query = data.generateInsertQuery(kit, KitHandler.TABLE_NAME);
				} else {
					query = data.geterateUpdateQuery(kit, KitHandler.TABLE_NAME);
				}
				
				database.query(query, true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
