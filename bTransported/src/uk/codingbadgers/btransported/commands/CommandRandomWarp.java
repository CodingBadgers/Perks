/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.codingbadgers.btransported.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.btransported.permissions.WarpPermission;

/**
 *
 * @author n3wton
 */
public class CommandRandomWarp extends ModuleCommand {

    private final CommandWarp m_warpCommand;
    
    public CommandRandomWarp(CommandWarp warpCommand) {
        super("randomwarp", "/randomwarp");
        this.addAliase("rwarp");
        m_warpCommand = warpCommand;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        
        if (!(sender instanceof Player)) {
            return true;
        }
        
        Player player = (Player) sender;

        if (!Module.hasPermission(player, WarpPermission.Random.permission)) {
            Module.sendMessage("bTransported", player, m_module.getLanguageValue("COMMAND-WARP-NO-PERMISSION").replace("%permission%", WarpPermission.Random.permission));
            return true;
        }
        
        m_warpCommand.useRandomWarp(player);        
        return true;        
    }
    
}
