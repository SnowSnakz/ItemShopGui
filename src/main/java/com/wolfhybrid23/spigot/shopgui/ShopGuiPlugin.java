package com.wolfhybrid23.spigot.shopgui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class ShopGuiPlugin extends JavaPlugin {
	Config config;
	
	InventoryEvents inventoryEvents;
	ShopGuiCommand shopCommand;
	
	Economy econ = null;
	
	Logger log;

	String deadLoreString = "\u00a7d\u00a7e\u00a7a\u00a7d";
	List<ItemShopGui> openGuis;
	
	@Override
	public void onEnable() {
		log = getLogger();
		
		if(!getEconomy()) {
			getLogger().warning("Failed to find an economy, please make sure you have vault (and a vault compatible economy plugin) installed.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		openGuis = new ArrayList<>();
		
		registerCommands();
		registerEvents();
		
		try {
			reloadFromConfig();
		}
		catch (InvalidValueException | ValueUndefinedException e) {
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}

	void reloadFromConfig() throws InvalidValueException, ValueUndefinedException
	{
		File cfgFile = new File(getDataFolder() + "/config.yml");
		if(!cfgFile.exists()) saveDefaultConfig();
		
		reloadConfig();
		
		for(ItemShopGui gui : openGuis) {
			gui.view.close();
		}
		openGuis.clear();
		
		config = new Config(this, getConfig(), "config.yml");
	}
	
	void registerCommands() {
		shopCommand = new ShopGuiCommand(this);
		
		Bukkit.getPluginCommand("shop").setExecutor(shopCommand);
	}
	
	void registerEvents() {
		inventoryEvents = new InventoryEvents(this);
		
		Bukkit.getPluginManager().registerEvents(inventoryEvents, this);
	}
	
	void stackSetDead(ItemStack stack) {
		if(!config.enableDeadItems) return;
		if(stack == null) return;
		
		ItemMeta meta = stack.getItemMeta();
		
		List<String> lore = meta.getLore();
		if(lore == null) lore = new ArrayList<>();
		
		lore.add(deadLoreString);
		stack.setItemMeta(meta);
	}
	
	int getMaxPurchase(Player plr, ShopItem shopItem) {
		PlayerInventory inv = plr.getInventory();
		ItemStack[] invSlots = inv.getContents();
		
		ItemStack shopItemSample = shopItem.createSample();
		
		int maxStackSize = shopItemSample.getMaxStackSize();
		double unitPrice = shopItem.buyPrice;
		
		int maxAdd = 0;
		for(ItemStack slotStack : invSlots) {
			if(slotStack == null) {
				maxAdd += maxStackSize;
				continue;
			}
			if(!slotStack.isSimilar(shopItemSample)) continue;
			
			maxAdd += maxStackSize - slotStack.getAmount();
		}
		
		double bal = getBalance(plr);
		maxAdd = Math.min(maxAdd, (int)Math.floor(bal / unitPrice));
		
		if(maxAdd < 0) maxAdd = 0;
		return maxAdd;
	}
	
	int countMatchingItems(Player plr, ShopItem shopItem) {
		PlayerInventory inv = plr.getInventory();
		ItemStack[] invSlots = inv.getContents();

		ItemStack shopItemSample = shopItem.createSample();
		
		int count = 0;
		for(ItemStack slotStack : invSlots) {
			if(slotStack == null) continue;
			if(slotStack.isSimilar(shopItemSample)) 
			{
				count += slotStack.getAmount();
			}
		}
		
		return count;
	}
	
	void removeSoldItems(Player plr, ShopItem shopItem, int amount) {
		PlayerInventory inv = plr.getInventory();
		
		ItemStack shopItemSample = shopItem.createSample();
		
		ItemStack[] slots = inv.getContents();
		for(int i = 0; i < slots.length; i++) {
			ItemStack slot = slots[i];
			
			if(slot == null) continue;
			
			if(slot.isSimilar(shopItemSample)) {
				int num = slot.getAmount();
				int remove = Math.min(amount, num);
				
				if(remove >= num) {
					slots[i] = null;
				}
				else {
					slot.setAmount(num - remove);
				}
				
				amount -= remove;
			}
			
			if(amount <= 0) break;
		}
		
		inv.setContents(slots);
	}
	
	boolean getEconomy() {
		if(Bukkit.getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
		if(rsp == null) {
			return false;
		}
		
		econ = rsp.getProvider();
		return econ != null;
	}
	
	double getBalance(Player plr) {
		return econ.getBalance(plr);
	}
	
	String formatBalance(double balance) {
		return econ.format(balance);
	}
	
	boolean chargePlayer(Player plr, double amount) {
		return econ.withdrawPlayer(plr, amount).transactionSuccess();
	}
	
	boolean payPlayer(Player plr, double amount) {
		return econ.depositPlayer(plr, amount).transactionSuccess();
	}
	
	String colorize(String str) {
		return ChatColor.translateAlternateColorCodes('&', str);
	}
}
