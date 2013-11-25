package uk.thecodingbadgers.bkits.kit;

import java.util.HashMap;
import java.util.Map;

import uk.codingbadgers.bFundamentals.player.PlayerData;

public class KitPlayerData implements PlayerData {

	private String player;
	private Map<String, Long> kits = new HashMap<String, Long>();
	
	public KitPlayerData(String player) {
		this.player = player;
	}
	
	public void addKitTimeout(Kit kit) {
		if (kits.containsKey(kit.getName())) {
			return;
		}
		
		kits.put(kit.getName(), System.currentTimeMillis() + kit.getTimeout());
	}
	
	public String getFormmatedTime(String kit) {
		long elapsedTime = (kits.get(kit) - System.currentTimeMillis()) / 1000;  
	    String format = String.format("%%0%dd", 2);
	    String seconds = String.format(format, elapsedTime % 60);  
	    String minutes = String.format(format, (elapsedTime % 3600) / 60);  
	    String hours = String.format(format, elapsedTime / 3600);  
	    String time = "";
	    
	    boolean showHours = !hours.equalsIgnoreCase("00");
	    boolean showMinutes = !minutes.equalsIgnoreCase("00");
	    boolean showSeconds = !seconds.equalsIgnoreCase("00");
	    
	    time += showHours ? hours + " hours, " : "";
	    time += showMinutes ? minutes + (showSeconds ? " minutes and " : " minutes") : "";
	    time += showSeconds ? seconds + " seconds" : "";  
	    return time;
	}

	public void addKitTimeout(String kitname, long endtime) {
		if (kits.containsKey(kitname)) {
			return;
		}
		
		kits.put(kitname, endtime);
	}
	
	public boolean canUse(Kit kit) {
		boolean canUse = !kits.containsKey(kit.getName()) || kits.get(kit.getName()) - System.currentTimeMillis() <= 0;
		
		if (canUse) {
			kits.remove(kit.getName());
		}
		
		return canUse;
	}
	
	public String generateInsertQuery(String kitname, String tablename) {
		if (!kits.containsKey(kitname)) {
			throw new IllegalArgumentException(player + " has not used kit " + kitname);
		}
		
		String query = "INSERT INTO " + tablename + " VALUES('" + player + "', '" + kitname + "', '" + kits.get(kitname)  + "');";
		return query;
	}
	
	public String[] generateInsertQueries(String tablename) {
		String[] queries = new String[kits.size()];
		int i = 0;
		
		for (Map.Entry<String, Long> entry : kits.entrySet()) {
			queries[i] = "INSERT INTO " + tablename + " VALUES('" + player + "', '" + entry.getKey() + "', '" + entry.getValue()  + "');";
			i++;
		}
		
		return queries;
	}
	
	public String[] generateUpdateQueries(String tablename) {
		String[] queries = new String[kits.size()];
		int i = 0;
		long curTime = System.currentTimeMillis();
		
		for (Map.Entry<String, Long> entry : kits.entrySet()) {
			if (entry.getValue() - curTime <= 0) {
				
			}
			
			queries[i] = "UPDATE " + tablename + " SET `timeout`='" + entry.getValue() + "' WHERE `kit`='" + entry.getKey() + "' AND `player` LIKE '" + player + "';";
			i++;
		}
		
		return queries;
	}
}
