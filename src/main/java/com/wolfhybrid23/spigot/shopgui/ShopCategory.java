package com.wolfhybrid23.spigot.shopgui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

class ShopCategory {
	ItemStack icon;
	
	int itemsPerPage;
	
	String name;
	String displayName;
	List<ShopItem> items;
	
	ShopCategory(Config config, ConfigurationSection cfg) throws ValueUndefinedException, InvalidValueException {
		itemsPerPage = config.itemsPerPage;
		
		name = cfg.getName();
		displayName = config.plugin.colorize(config.require(cfg, "display-name", config.path));

		Material iconMaterial = config.getMaterial(cfg, "material", config.path);
		icon = new ItemStack(iconMaterial);
		
		ItemMeta iconMeta = icon.getItemMeta();
		iconMeta.setDisplayName("\u00A7f" + displayName);
		// TODO: Make icon more configurable (glow, enchantment, etc.)
		
		icon.setItemMeta(iconMeta);
		
		items = new ArrayList<ShopItem>();
		
		ConfigurationSection itemSection = cfg.getConfigurationSection("items");
		if(itemSection == null) return;
		
		Set<String> itemNames = itemSection.getKeys(false);
		for(String itemName : itemNames) {
			ShopItem shopItem = new ShopItem(config, itemSection.getConfigurationSection(itemName));
			items.add(shopItem);
		}
	}
	
	List<ShopItem> getPageItems(int page) {
		return items.subList(page * itemsPerPage, Math.min((page + 1) * itemsPerPage, items.size()));
	}
	
	int estimatePageCount() {
		if(items.size() == 0) return 0;
		
		int res = 0;
		int c = items.size();
		
		while(c > 0) {
			c -= itemsPerPage;
			res++;
		}
		
		return res;
	}
}
