package uk.thecodingbadgers.bkits.commands;

import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.gui.GuiInventory;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.thecodingbadgers.bkits.Kit;
import uk.thecodingbadgers.bkits.KitClaim;
import uk.thecodingbadgers.bkits.bKits;
import uk.thecodingbadgers.bkits.callbacks.KitGuiCallback;

public class KitCommand extends ModuleCommand {
	
	private static final String PERMISSION_KIT = "perks.bkits";
	private static final String PERMISSION_KIT_GUI = "perks.bkits.gui";

	private final bKits m_module;
	
	/**
	 * 
	 * @param module 
	 */
	public KitCommand(bKits module) {
		super("kit", "/kit <name>");
		m_module = module;
	}

	/**
	 * 
	 * @param sender
	 * @param labe
	 * @param args
	 * @return 
	 */
	@Override
	public boolean onCommand(CommandSender sender, String labe, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage(m_module.getLanguageValue("not-player"));
			return true;
		}

		Player player = (Player) sender;

		if (args.length == 0) {
			handleKitGUI(player);
			return true;
		}

		String kitname = args[0];
		// give kit no gui TODO
		
		return true;
	}
	
	/**
	 * 
	 * @param player 
	 */
	private void handleKitGUI(final Player player) {
		
		if (!Module.hasPermission(player, PERMISSION_KIT_GUI)) {
			Module.sendMessage("Kits", player, m_module.getLanguageValue("COMMAND-BKITS-GUI-NO-PERMISSION"));
			return;
		}
		
		Map<String, Kit> kits = m_module.getKits();
		final int noofKits = kits.size();
		final int ROW_COUNT = (int) Math.ceil(noofKits / 9.0f);

        GuiInventory inventory = new GuiInventory(bFundamentals.getInstance());
        inventory.createInventory("Kit Selection", ROW_COUNT);
		
		for (Kit kit : kits.values()) {
			
			if (!Module.hasPermission(player, PERMISSION_KIT + "." + (kit.getName().toLowerCase()))) {
				continue;
			}
			
			ItemStack item = new ItemStack(kit.getIcon());
			String[] details = new String[3];
			
			KitClaim claim = m_module.canPlayerClaimKit(player, kit);
			
			if (claim.canClaim) {
				details[0] = ChatColor.GREEN + "Available";
				details[1] = ChatColor.GOLD + "Left click to claim kit";
			} else {
				details[0] = ChatColor.RED + claim.timeLeft;
				details[1] = "";
			}
			
			details[2] = ChatColor.GOLD + "Right click to preview kit";
			
			inventory.addMenuItem(kit.getName(), item, details, new KitGuiCallback(m_module, player, kit, claim.canClaim));
		}
		
		inventory.open(player);
		
	}
}
