package com.wolfhybrid23.spigot.shopgui;

import org.bukkit.enchantments.Enchantment;

class EnchantData {
	Enchantment ench;
	int lvl;
	
	EnchantData(Enchantment ench, int level) {
		this.ench = ench;
		this.lvl = level;
	}
}
