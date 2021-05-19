package com.wolfhybrid23.spigot.shopgui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Config {
	ShopGuiPlugin plugin;
	FileConfiguration cfg;
	String path;
	
	Config(ShopGuiPlugin plugin, FileConfiguration cfg, String path) throws InvalidValueException, ValueUndefinedException {
		this.plugin = plugin;
		this.cfg = cfg;
		this.path = path;
		
		Load();
	}
	
	Sound buySound;
	Sound sellSound;
	Sound failSound;
	
	Material buyButtonMaterial;
	Material sellButtonMaterial;
	Material backButtonMaterial;
	Material nextButtonMaterial;
	Material prevButtonMaterial;
	Material econButtonMaterial;
	Material disabledButtonMaterial;
	
	int[] bulkAmounts;
	
	String prefix;
	String homePageTitle;
	
	String buyButtonText;
	String sellButtonText;
	String backButtonText;
	String nextButtonText;
	String prevButtonText;
	String econButtonText;
	String disabledButtonText;
	
	String notEnoughCurrencyError;
	String notEnoughItemsError;
	String notEnoughSpaceError;
	String notEnoughPermissionError;
	String noCategoryPermissionError;
	
	String buyPagePrefix;
	
	int backButtonPos;
	int nextButtonPos;
	int prevButtonPos;
	int econButtonPos;
	
	int homePageLength;
	int buyPageLength;
	int categoryPageLength;
	
	int itemsPerPage;
	boolean enableDeadItems;
	
	List<ShopCategory> categories;
	
	String normalize(String str) {
		return str.trim().replace('-', '_').replace(' ', '_').toUpperCase();
	}
	
	int clampWithWarning(int val, int min, int max, String message) {
		if((val > max) || (val < min)) {
			plugin.log.warning(message);
		}
		return Math.min(max, Math.max(val, min));
	}
	
	void Load() throws ValueUndefinedException, InvalidValueException {
		enableDeadItems = cfg.getBoolean("enable-dead-items", false);
		
		buySound = getSound(cfg.getString("buy-sound"));
		sellSound = getSound(cfg.getString("sell-sound"));
		failSound = getSound(cfg.getString("fail-sound"));
		
		buyButtonMaterial = getMaterial(cfg, "buy-button.material", path);
		sellButtonMaterial = getMaterial(cfg, "sell-button.material", path);
		backButtonMaterial = getMaterial(cfg, "back-button.material", path);
		nextButtonMaterial = getMaterial(cfg, "next-page-button.material", path);
		prevButtonMaterial = getMaterial(cfg, "previous-page-button.material", path);
		disabledButtonMaterial = getMaterial(cfg, "disabled-button.material", path);
		econButtonMaterial = getMaterial(cfg, "balance-icon.material", path);
		
		prefix = plugin.colorize(cfg.getString("prefix", "&8[&7Item Shop&8] &r"));
		homePageTitle = plugin.colorize(require(cfg, "home-page-title", path));
		
		buyButtonText = plugin.colorize(require(cfg, "buy-button.display-name", path));
		sellButtonText = plugin.colorize(require(cfg, "sell-button.display-name", path));
		backButtonText = plugin.colorize(require(cfg, "back-button.display-name", path));
		nextButtonText = plugin.colorize(require(cfg, "next-page-button.display-name", path));
		prevButtonText = plugin.colorize(require(cfg, "previous-page-button.display-name", path));
		disabledButtonText = plugin.colorize(require(cfg, "disabled-button.display-name", path));
		econButtonText = plugin.colorize(require(cfg, "balance-icon.display-name", path));
		
		notEnoughCurrencyError = plugin.colorize(require(cfg, "not-enough-currency", path));
		notEnoughItemsError = plugin.colorize(require(cfg, "not-enough-items", path));
		notEnoughSpaceError = plugin.colorize(require(cfg, "not-enough-space", path));
		notEnoughPermissionError = plugin.colorize(require(cfg, "no-command-permission", path));
		noCategoryPermissionError = plugin.colorize(require(cfg, "no-category-permission", path));
		
		backButtonPos = cfg.getInt("back-button-x", 5) - 1;
		nextButtonPos = cfg.getInt("next-page-button-x", 8) - 1;
		prevButtonPos = cfg.getInt("previous-page-button-x", 2) - 1;
		econButtonPos = cfg.getInt("balance-icon-x", 9) - 1;
		
		backButtonPos = clampWithWarning(backButtonPos, 0, 8, "Buttons can only use the bottom 9 slots.");
		nextButtonPos = clampWithWarning(backButtonPos, 0, 8, "Buttons can only use the bottom 9 slots.");
		prevButtonPos = clampWithWarning(backButtonPos, 0, 8, "Buttons can only use the bottom 9 slots.");
		econButtonPos = clampWithWarning(backButtonPos, 0, 8, "Buttons can only use the bottom 9 slots.");
		
		homePageLength = cfg.getInt("home-page-lines", 4);
		buyPageLength = cfg.getInt("buy-page-lines", 3);
		categoryPageLength = cfg.getInt("category-page-lines", 4);
		
		homePageLength = clampWithWarning(homePageLength, 1, 6, "The value of home-page-lines must be between 1 and 6");
		buyPageLength = clampWithWarning(buyPageLength, 2, 6, "The value of buy-page-lines must be between 2 and 6");
		categoryPageLength = clampWithWarning(categoryPageLength, 2, 6, "The value of category-page-lines must be between 2 and 6");
		
		homePageLength *= 9;
		buyPageLength *= 9;
		categoryPageLength *= 9;
		
		buyPagePrefix = plugin.colorize(cfg.getString("buy-page-prefix", "&l"));
		
		itemsPerPage = cfg.getInt("items-per-page");
		itemsPerPage = clampWithWarning(itemsPerPage, 1, categoryPageLength - 9, "items-per-page is greator than the inventory size. (the bottom 9 slots are reserved)");
		
		List<Integer> bulkAmounts = cfg.getIntegerList("bulk-amounts");
		
		if(bulkAmounts == null) {
			if(!cfg.isInt("bulk-amounts")) {
				throw new ValueUndefinedException("bulk-amounts", path);
			}

			bulkAmounts = new ArrayList<Integer>();
			bulkAmounts.add(cfg.getInt("bulk-amounts"));
		}
		
		if(bulkAmounts.size() < 1) {
			throw new InvalidValueException("bulk-amounts", path, "Must contain at least one item.");
		}
		
		this.bulkAmounts = new int[bulkAmounts.size()];
		for(int i = 0; i < bulkAmounts.size(); i++) {
			this.bulkAmounts[i] = bulkAmounts.get(i);
		}
		
		this.categories = new ArrayList<>();
		ConfigurationSection categoriesSection = cfg.getConfigurationSection("categories");
		Set<String> categories = categoriesSection.getKeys(false);
		
		for(String category : categories) {
			ConfigurationSection categorySection = categoriesSection.getConfigurationSection(category);
			this.categories.add(new ShopCategory(this, categorySection));
		}
	}
	
	String require(ConfigurationSection cfg, String prop, String path) throws ValueUndefinedException {
		String val = cfg.getString(prop);
		if(val == null) throw new ValueUndefinedException(prop, path);
		return val;
	}
	
	Sound getSound(String name) {
		Sound result;
		
		try {
			result = Enum.valueOf(Sound.class, normalize(name));
		}
		catch(IllegalArgumentException ex) {
			result = null;
		}
		
		return result;
	}
	
	String materialHelpLink = "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html";
	Material getMaterial(ConfigurationSection cfg, String prop, String path) throws ValueUndefinedException, InvalidValueException {
		String name = cfg.getString(prop);
		if(name == null) throw new ValueUndefinedException(prop, path);
		
		String transformed = normalize(name);
		Material m = Material.getMaterial(transformed);
		
		if(m == null) throw new InvalidValueException(prop, path, name + " (transformed: " + transformed + ")", "Material", materialHelpLink);
		if(!m.isItem()) throw new InvalidValueException(prop, path, name + " (transformed: " + transformed + ") cannot be used as an item.");
		
		return m;
	}
	
	// For a future update...
	/*
	Map<String, EnchantData> enchantTranslation;
	void setupEnchantTranslation() {
		putAllLevels("protection", Enchantment.PROTECTION_ENVIRONMENTAL);
		putAllLevels("projectile_protection", Enchantment.PROTECTION_PROJECTILE);
		putAllLevels("blast_protection", Enchantment.PROTECTION_EXPLOSIONS);
		putAllLevels("fire_protection", Enchantment.PROTECTION_FIRE);
		putAllLevels("feather_falling", Enchantment.PROTECTION_FALL);
		
		putAllLevels("unbreaking", Enchantment.DURABILITY);
		
		putLevel("mending", Enchantment.MENDING);
		putLevel("infinity", Enchantment.ARROW_INFINITE);
		putLevel("silk_touch", Enchantment.SILK_TOUCH);
		
		putAllLevels("effeciency", Enchantment.DIG_SPEED);
		putAllLevels("sharpness", Enchantment.DAMAGE_ALL);
	}
	
	void putLevel(String name, Enchantment enchant) {
		enchantTranslation.put(name, new EnchantData(enchant, enchant.getStartLevel()));
	}
	
	void putAllLevels(String name, Enchantment enchant) {
		int lvl = enchant.getStartLevel();
		int max = enchant.getMaxLevel();
		
		while(lvl <= max) 
		{
			enchantTranslation.put(name + "-" + lvl, new EnchantData(enchant, lvl));
		}
	}
	
	EnchantData getEnchantData(Material mat, String toTranslate) {
		String transformed = toTranslate.trim().toLowerCase();
		
		EnchantData data = null;
		data = enchantTranslation.get(transformed);
		
		return data;
	}
	*/
	
	ItemStack createBuyButton(Player plr, ShopItem item, int amount) {
		ItemStack result = new ItemStack(buyButtonMaterial);
		String displayName = buyButtonText;
		
		if(amount <= -1) {
			amount = Math.min(plugin.getMaxPurchase(plr, item), (int)Math.ceil(plugin.getBalance(plr) / item.buyPrice));
			displayName += amount + " (Fill Inventory)";
		}
		else {
			result.setAmount(amount);
			displayName += amount;
		}
		
		double price = amount * item.buyPrice;
		boolean canAfford = plugin.getBalance(plr) >= price;
		
		ItemMeta meta = result.getItemMeta();
		meta.setDisplayName(displayName);
		
		List<String> lore = new ArrayList<>();
		lore.add((canAfford ? "\u00A7a" : "\u00A7c") + plugin.formatBalance(price));
		
		meta.setLore(lore);
		result.setItemMeta(meta);
		
		return result;
	}
	ItemStack createSellButton(Player plr, ShopItem item, int amount) {
		ItemStack result = new ItemStack(sellButtonMaterial);
		String displayName = sellButtonText;
		
		if(amount == -1) {
			amount = plugin.countMatchingItems(plr, item);
			displayName += amount + " (Sell Inventory)";
		}
		else {
			result.setAmount(amount);
			displayName += amount;
		}
		
		double price = amount * item.sellPrice;
		boolean canAfford = plugin.countMatchingItems(plr, item) >= amount;
		
		ItemMeta meta = result.getItemMeta();
		meta.setDisplayName(displayName);
		
		List<String> lore = new ArrayList<>();
		lore.add((canAfford ? "\u00A7a" : "\u00A7c") + plugin.formatBalance(price));
		
		meta.setLore(lore);
		result.setItemMeta(meta);
		
		return result;
	}
}
