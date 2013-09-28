package uk.thecodingbadgers.bkits.kit;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
	
	@EventHandler
	public void playerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		FundamentalPlayer fPlayer = bFundamentals.Players.getPlayer(player);
		loadPlayerTimeouts(fPlayer);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		FundamentalPlayer fPlayer = bFundamentals.Players.getPlayer(player);
		updatePlayerTimeouts(fPlayer);
	}

	@EventHandler
	public void playerKick(PlayerKickEvent event) {
		Player player = event.getPlayer();
		FundamentalPlayer fPlayer = bFundamentals.Players.getPlayer(player);
		updatePlayerTimeouts(fPlayer);
	}
	
	/* Logic */
	public void loadPlayerTimeouts(FundamentalPlayer player) {
		BukkitDatabase database = bKits.getDatabase();
		
		String query = "SELECT * FROM " + KitHandler.TABLE_NAME + " WHERE `player`=" + player.getPlayer().getName();
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
		
	}
}
