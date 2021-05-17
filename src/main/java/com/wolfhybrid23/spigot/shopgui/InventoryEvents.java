package com.wolfhybrid23.spigot.shopgui;

import java.util.List;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

class InventoryEvents implements Listener {
	ShopGuiPlugin plugin;
	
	InventoryEvents(ShopGuiPlugin plugin) {
		this.plugin = plugin;
	}
	
	ItemShopGui findShopGui(InventoryView view) {
		if(view == null) return null;
		
		for(ItemShopGui gui : plugin.openGuis) {
			if(gui.view == view) {
				return gui;
			}
		}
		
		return null;
	}
	
	void removeDeadItems(Inventory inventory) {
		if(plugin.config.enableDeadItems)
		{
			ItemStack[] stacks = inventory.getContents();
			
			List<String> lore;
			ItemMeta meta;
			for(ItemStack stack : stacks) {
				if(stack == null) continue;
				
				meta = stack.getItemMeta();
				lore = meta.getLore();
				
				if(lore == null) continue;
				
				for(String line : lore) {
					if(line.equals(plugin.deadLoreString)) {
						inventory.remove(stack);
					}
				}
			}
		}
	}
	
	@EventHandler
	void onPlayerClick(InventoryClickEvent event) {
		ItemShopGui gui = findShopGui(event.getView());
		HumanEntity whoClicked = event.getWhoClicked();
		
		if(gui != null) {
			event.setCancelled(true);
			
			if(event.getClickedInventory() == gui.inventory) {
				plugin.stackSetDead(event.getCurrentItem());
			}
			
			gui.passClick(event);
			
			if(whoClicked instanceof Player) {
				((Player)whoClicked).updateInventory();	
			}
		}
		
		removeDeadItems(event.getView().getBottomInventory());
	}
	
	@EventHandler
	void onInventoryClose(InventoryCloseEvent event) {
		ItemShopGui gui = findShopGui(event.getView());
		
		if(gui != null) {
			if(!gui.refreshing) {
				plugin.openGuis.remove(gui);
			}
			else gui.refreshing = false;
		}
		
		removeDeadItems(event.getView().getBottomInventory());
	}
}
