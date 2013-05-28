package uk.codingbadgers.btransported.commands.tp;

import java.util.ArrayList;
import java.util.Calendar;

import org.bukkit.entity.Player;

import uk.codingbadgers.bFundamentals.player.PlayerData;

public class PlayerTPRData implements PlayerData {
	
	private class TPRequest {
		public Player from;
		public Player to;
		public Long time;
	}
	
	private static Long TimeOutMS = 30000L;
	
	private ArrayList<TPRequest> m_requests = new ArrayList<TPRequest>();

	public boolean addTPRequest(Player from, Player to) {
	
		if (requestExists(from, to)) {
			return false;			
		}
		
		TPRequest newRequest = new TPRequest();
		newRequest.from = from;
		newRequest.to = to;
		newRequest.time = Calendar.getInstance().getTimeInMillis();
		
		m_requests.add(newRequest);
		
		return true;
	}
	
	public boolean requestExists(Player from, Player to) {
		
		for (TPRequest request : m_requests) {
			if (request.from.getName().equalsIgnoreCase(from.getName()) && request.to.getName().equalsIgnoreCase(to.getName())) {
				Long currentTime = Calendar.getInstance().getTimeInMillis();
				if (currentTime - request.time > TimeOutMS) { // should be configurable
					m_requests.remove(request);
					return false;
				}
				
				// request is still valid
				return true;
			}
		}
		
		return false;		
	}
	
	public Player getLastRequest() {
		removeTimedOutRequests();
		if (m_requests.size() == 0) {
			return null;
		}
		return m_requests.get(m_requests.size() - 1).from;
	}
	
	private void removeTimedOutRequests() {
		for (TPRequest request : m_requests) {
			Long currentTime = Calendar.getInstance().getTimeInMillis();
			if (currentTime - request.time > TimeOutMS) { // should be configurable
				m_requests.remove(request);
			}
		}
	}

	public void removeRequest(Player from, Player to) {
		for (TPRequest request : m_requests) {
			if (request.from.getName().equalsIgnoreCase(from.getName()) && request.to.getName().equalsIgnoreCase(to.getName())) {
				m_requests.remove(request);
				return;
			}
		}
	}

}
