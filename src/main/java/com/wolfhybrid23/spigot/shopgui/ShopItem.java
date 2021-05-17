package com.wolfhybrid23.spigot.shopgui;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

class ShopItem {
	Material mat;
	
	boolean canBuy;
	boolean canSell;
	
	double buyPrice;
	double sellPrice;
	
	ShopItem(Config config, ConfigurationSection itemSection) throws ValueUndefinedException, InvalidValueException {
		String matName = itemSection.getName();
		String transformed = matName.replace('-', '_').toUpperCase();
		
		mat = Material.getMaterial(transformed);
		if(mat == null) throw new InvalidValueException(matName, itemSection.getParent().getName(), matName + " (transformed: " + transformed + ")", "Material", config.materialHelpLink);
		
		canBuy = itemSection.getBoolean("can-buy");
		canSell = itemSection.getBoolean("can-sell");
		
		buyPrice = itemSection.getDouble("buy-price");
		sellPrice = itemSection.getDouble("sell-price");
		
		/* For a future update.
		List<String> enchantment = itemSection.getStringList("enchantment");
		List<String> attributes = itemSection.getStringList("attributes");
		List<String> potionEffects = itemSection.getStringList("potion-effects");
		List<String> flags = itemSection.getStringList("item-flags");
		
		TODO: Continue the above.
		*/
	}
	
	ItemStack createSample() {
		ItemStack stack = new ItemStack(mat);
		
		// For a future update:
		// TODO: Apply enchantment, attributes, potion effects and item flags. 
		
		return stack;
	}
}
