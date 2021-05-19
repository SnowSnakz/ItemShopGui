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
	int length;
	
	String name;
	String displayName;
	List<ShopItem> items;
	
	String permission;
	
	ShopCategory(Config config, ConfigurationSection cfg) throws ValueUndefinedException, InvalidValueException {
		if(cfg.isInt("lines")) length = cfg.getInt("lines") * 9;
		else length = config.categoryPageLength;
		
		itemsPerPage = cfg.getInt("items-per-page", config.itemsPerPage);
		itemsPerPage = config.clampWithWarning(itemsPerPage, 1, length - 9, "items-per-page is less than 1 or greator than the max allowed (the bottom 9 rows are reserved)");
		
		
		name = cfg.getName();
		displayName = config.plugin.colorize(config.require(cfg, "display-name", config.path));

		permission = cfg.getString("permission", name.replace('-', '_'));
		
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
