package uk.codingbadgers.btransported.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
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
public class CommandHome extends ModuleCommand implements Listener {

    /**
     * The bFundamentals module
     */
    private final bTransported m_module;

    /**
     * Map of player name to list of homes
     */
    private final Map<String, List<PlayerHome>> m_homes;
    
    /**
     * The name of the anvil inventory used for naming a home.
     */
    public static final String ANVIL_INVENTORY_NAME = "BF-HOME-ANVIL";

    /**
     * Class constructor
     *
     * @param module	The bFundamentals module
     */
    public CommandHome(bTransported module) {
        super("home", "home | home <home name> | home <player name> <home name> | home set <name> | home remove <home name> | home remove <player name> <home name>");
        m_module = module;
        m_homes = new HashMap<String, List<PlayerHome>>();
    }

    @Override
    /**
     * Called when a command is executed. Returning true indicates the command
     * was handled.
     *
     * @param sender	The thing that executed the command, player or console.
     * @param label The label of the command
     * @param args	The arguments passed with command
     */
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        // /Home cannot be used via the console currently.
        if (!(sender instanceof Player)) {
            return true;
        }

        // Cast the sender to a player
        final Player player = (Player) sender;

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
        else if (args.length == 1) {
            
            // handle /home <home name>
            handleHomeName(player, args[0]);
            return true;
            
        }
        else if (args.length == 2) {
            
            // handle /home <player name> <home name>
            if (handleHomePlayerName(player, args[0], args[1])) {
                return true;
            }
            
            final String command = args[0];
            if (command.equalsIgnoreCase("set")) {
                addNewHome(player, player.getLocation(), args[1]);
                return true;
            }
            else if (command.equalsIgnoreCase("remove")) {
                handleHomeRemove(player.getName(), args[1]);
                return true;
            }
            
        }
        else if (args.length == 3) {
            
            final String command = args[0];
            if (command.equalsIgnoreCase("remove")) {
                handleHomeRemove(args[1], args[2]);
                return true;
            }
        }

        return false;
    }
    
    private boolean handleHomeRemove(String playerName, String homeName) {
        
        PlayerHome home = getHomeFromName(playerName, homeName);
        if (home == null) {
            return false;
        }
        
        List<PlayerHome> homes = m_homes.get(playerName);
        homes.remove(home);
        m_homes.put(playerName, homes);
        
        return true;        
    }
    
    private boolean handleHomePlayerName(Player player, String playerName, String homeName) {
        
        OfflinePlayer homePlayer = Bukkit.getOfflinePlayer(playerName);
        if (!homePlayer.hasPlayedBefore()) {
            return false;
        }
        
        PlayerHome home = getHomeFromName(playerName, homeName);
        if (home == null) {
            return false;
        }

        return m_module.teleportOfflinePlayer(player, home.location);
    }
    
    private boolean handleHomeName(Player player, String homeName) {
        
        PlayerHome home = getHomeFromName(player.getName(), homeName);
        if (home == null) {
            return false;
        }

        return m_module.teleportOfflinePlayer(player, home.location);
    }

    private PlayerHome getHomeFromName(String playername, String homeName) {
        
        if (!m_homes.containsKey(playername)) {
            return null;
        }
        
        List<PlayerHome> homes = m_homes.get(playername);
        for (PlayerHome home : homes) {
            if (home.name.equalsIgnoreCase(homeName)) {
                return home;
            }
        }
        
        return null;
    }
    
    private void handleHomeGUI(Player player) {

        GuiInventory inventory = new GuiInventory(bFundamentals.getInstance());
        inventory.createInventory("Home Selection", 1);

        // If they already have homes, list them
        if (m_homes.containsKey(player.getName())) {
            List<PlayerHome> homes = m_homes.get(player.getName());

            for (PlayerHome home : homes) {
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
     * Format a home name into a non spaced camel case name
     * @param name The name to format
     * @return The formated version of the name
     */
    private String formatHomeName(String name) {
        
        name = name.toLowerCase();
        String[] nameParts = name.split(" ");
        String formattedName = "";
        for (String part : nameParts) {
            String camelPart = part.substring(0, 1).toUpperCase();
            if (part.length() > 1) {
                camelPart += part.substring(1);
            }
            formattedName += camelPart;
        }      
        
        return formattedName;
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
            Module.sendMessage("Home", player, m_module.getLanguageValue("COMMAND-HOME-REACHED-MAX"));
            return;
        }

        PlayerHome home = new PlayerHome();
        home.location = location;
        home.name = formatHomeName(name);
        home.owner = player.getName();

        homes.add(home);

        m_homes.put(player.getName(), homes);
        
        Module.sendMessage("Home", player, m_module.getLanguageValue("COMMAND-HOME-ADDED-NEW"));
    }

    /**
     * Handle click events within inventory's
     *
     * @param event The click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        
        Inventory invent = event.getInventory();
        if (invent.getType() != InventoryType.ANVIL) {
            return;
        }
        
        if (!invent.getName().equals(CommandHome.ANVIL_INVENTORY_NAME)) {
            return;
        }
        
        // we are in one of our anvil inventories
        event.setCancelled(true);
        
        // Are they clicking the result slot?
        final int ANVIL_RESULT_SLOT = 2;
        if (event.getRawSlot() != ANVIL_RESULT_SLOT) {
            return;
        }
        
        // Get the slot item
        ItemStack nameItem = invent.getItem(ANVIL_RESULT_SLOT);
        if (nameItem == null) {
            return;
        }

        // Setup the vars
        Player player = (Player) event.getWhoClicked();
        String name = nameItem.getItemMeta().hasDisplayName() ? nameItem.getItemMeta().getDisplayName() : player.getName() + "s Home";
        Location location = player.getLocation();
        
        // Add the new home
        addNewHome(player, location, name);
        
        // Close the inventory        
        player.closeInventory();
        player.updateInventory();
              
    }
    
    /**
     * Handle click events within inventory's
     *
     * @param event The click event
     */
    @EventHandler
    public void onInventoryClick(InventoryCloseEvent event) {
        
        Inventory invent = event.getInventory();
        if (invent.getType() != InventoryType.ANVIL) {
            return;
        }
        
        if (!invent.getName().equals(CommandHome.ANVIL_INVENTORY_NAME)) {
            return;
        }
        
        // Remove all items from our home anvil
        invent.setContents(new ItemStack[] {});        
    }
    
    /**
     * Handle tab completion
     * @return A list of matches
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        List<String> matches = new ArrayList<String>();
        if (args.length == 0)
            return matches;

        String playerName = sender.getName();
                
        //home remove <home name>
        //home remove <player name> <home name>
        if (args.length >= 2 && args[0].equalsIgnoreCase("remove")) {
            
            if (args.length == 3) {
                playerName = args[1];
            }
            
            if (!m_homes.containsKey(playerName)) {
                return matches;
            }
            
            List<PlayerHome> homes = m_homes.get(playerName);
            for (PlayerHome home : homes) {
                matches.add(home.name);
            }            
        }
        //home <home name>
        //home <player name> <home name>
        else if (args.length >= 1) {
            
            if (args.length == 2) {
                playerName = args[0];
            }
            
            if (!m_homes.containsKey(playerName)) {
                return matches;
            }
            
            List<PlayerHome> homes = m_homes.get(playerName);
            for (PlayerHome home : homes) {
                matches.add(home.name);
            }            
        }

        return matches;
    }
}
