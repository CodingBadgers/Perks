package uk.codingbadgers.btransported.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import uk.codingbadgers.bFundamentals.bFundamentals;

import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.gui.GuiInventory;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.btransported.bTransported;
import uk.codingbadgers.btransported.commands.home.HomeGuiCallback;
import uk.codingbadgers.btransported.commands.home.NewHomeGuiCallback;
import uk.codingbadgers.btransported.commands.home.PlayerHome;

/**
 * @author N3wton
 */
public class CommandHome extends ModuleCommand {
	
	/** The bFundamentals module */
	private final bTransported m_module;
    
    /** Map of player name to list of homes */
    private final Map<String, List<PlayerHome>> m_homes;
	
	/**
	 * Class constructor
	 * @param module	The bFundamentals module
	 */
	public CommandHome(bTransported module) {
		super("home", "home | home <home name> | home <player name> <home name> | home set <name> | home delete <home name> | home delete <player name> <home name>");
		m_module = module;
        m_homes = new HashMap<String, List<PlayerHome>>();
	}

	@Override
	/**
	 * Called when a command is executed. Returning true indicates the command was handled.
	 * @param sender	The thing that executed the command, player or console.
	 * @param label 	The label of the command
	 * @param args		The arguments passed with command
	 */
	public boolean onCommand(CommandSender sender, String label, String[] args)
	{
		// /Home cannot be used via the console currently.
		if (!(sender instanceof Player)) {
			return true;
		}
		
		// Cast the sender to a player
		final Player player = (Player)sender;
		
		// Make sure the sending player has the spawn permission
		if (!Module.hasPermission(player, "perks.btransported.home")) {
			Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-HOME-NO-PERMISSION").replace("%permission%", "perks.btransported.home"));
			return true;
		}
		
        if (args.length == 0) {
            
            // handle /home
            handleHomeGUI(player);
            
            return true;
        }
		
		return false;
	}
    
    private void handleHomeGUI(Player player) {
        
        GuiInventory inventory = new GuiInventory(bFundamentals.getInstance());
        inventory.createInventory("Home Selection", 1);
        
        // If they already have homes, list them
        if (m_homes.containsKey(player.getName()))
        {
            List<PlayerHome> homes = m_homes.get(player.getName());
            
            for (PlayerHome home : homes)
            {
                ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
                String[] details = new String[2];
                details[0] = home.location.getBlockX() + ", " + home.location.getBlockY() + ", " + home.location.getBlockZ();
                details[1] = home.location.getWorld().getName();
                inventory.addMenuItem(home.name, item, details, new HomeGuiCallback(m_module, player, home));
            }
        }       
        
        Location playerLocation = player.getLocation();
        
        // Add the writen book home creator
        ItemStack item = new ItemStack(Material.BOOK_AND_QUILL);
        String[] details = new String[3];
        details[0] = "Create a new home at";
        details[1] = playerLocation.getBlockX() + ", " + playerLocation.getBlockY() + ", " + playerLocation.getBlockZ();
        details[2] = "in " + playerLocation.getWorld().getName();
        
        final int LAST_SLOT = 8;
        inventory.addMenuItem("New Home", item, details, LAST_SLOT, new NewHomeGuiCallback(m_module, player, this));
        
        inventory.open(player);
        
    }
    
    /**
     * 
     * @param player
     * @param location
     * @param name 
     */
    public void addNewHome(Player player, Location location, String name) {
        
        List<PlayerHome> homes;
        if (m_homes.containsKey(player.getName())) {
            homes = m_homes.get(player.getName());
        } else {
            homes = new ArrayList<PlayerHome>();
        }
        
        if (homes.size() >= 8) {
            Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-HOME-REACHED-MAX"));
            return;
        }
        
        PlayerHome home = new PlayerHome();
        home.location = location;
        home.name = name;
        home.owner = player.getName();
        
        homes.add(home); 
        
        m_homes.put(player.getName(), homes);
    }
    
}
