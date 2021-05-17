package com.wolfhybrid23.spigot.shopgui;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class ShopGuiCommand implements CommandExecutor {
	String permission = "shopgui.command.use";
	//String openPermission = "shopgui.open."; For future update (maybe)
	String reloadPermission = "shopgui.command.reload";
	
	ShopGuiPlugin plugin;
	
	ShopGuiCommand(ShopGuiPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("shop")) {
			if(args.length > 0) {
				if(args[0].equalsIgnoreCase("reload")) {
					if(sender.hasPermission(reloadPermission)) {
						sender.sendMessage("\u00A7aReloading the plugin...");
						
						try {
							plugin.reloadFromConfig();
							
						} catch (InvalidValueException | ValueUndefinedException e) {
							e.printStackTrace();
							sender.sendMessage("\u00A7cReload Failed!");
							
							return true;
						}
						
						sender.sendMessage("\u00A7aDone!");
						return true;
					}
					else {
						sender.sendMessage(plugin.config.notEnoughPermissionError);
						return true;
					}
				}
			}
			
			if(sender.hasPermission(permission)) {
				if(sender instanceof Player) {
					Player plr = (Player)sender;
					plugin.openGuis.add(new ItemShopGui(plugin, plr));
					
					return true;
				}
				else {
					sender.sendMessage("Only players can execute this command.");
					return true;
				}
			}
			else
			{
				sender.sendMessage(plugin.config.notEnoughPermissionError);
				return true;
			}	
		}
		
		return false;
	}
}
