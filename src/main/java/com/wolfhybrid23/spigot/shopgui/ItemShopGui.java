package com.wolfhybrid23.spigot.shopgui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

class ItemShopGui {
	enum State {
		HOME, CATEGORY, ITEM
	}
	
	State state;
	
	ShopGuiPlugin plugin;
	InventoryView view;
	Inventory inventory;
	Player player;
	
	ShopCategory selectedCategory;
	List<ShopItem> currentCategoryItems;
	
	ShopItem selectedItem;
	
	int pageIndex;
	boolean refreshing;
	
	ItemShopGui(ShopGuiPlugin plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
		
		state = State.HOME;
		homeLayout = new HashMap<>();
		categoryLayout = new HashMap<>();
		
		view = open();
	}
	
	ItemStack createButton(Material mat, String displayName, List<String> lore) {
		ItemStack stack = new ItemStack(mat);
		ItemMeta meta = stack.getItemMeta();
		
		meta.setDisplayName(displayName);
		meta.setLore(lore);
		
		stack.setItemMeta(meta);
		return stack;
	}
	
	ItemStack createBalanceIcon() {
		ItemStack stack = new ItemStack(plugin.config.econButtonMaterial);
		ItemMeta meta = stack.getItemMeta();
		
		meta.setDisplayName(plugin.config.econButtonText);
		List<String> lore = new ArrayList<>();
		
		lore.add("\u00A7a" + plugin.formatBalance(plugin.getBalance(player)));
		
		meta.setLore(lore);
		stack.setItemMeta(meta);
		
		return stack;
	}
	
	void populate() {
		inventory.clear();

		int bottom = inventory.getSize() - 9;
		inventory.setItem(bottom + plugin.config.econButtonPos, createBalanceIcon());
		
		switch(state) {
		default: break;
		case HOME:
			int size = plugin.config.homePageLength;
			int length = plugin.config.homePageLength / 9;
			
			if(homeLayout.isEmpty()) {
				List<ShopCategory> categories = plugin.config.categories;

				if(categories.size() > size) {
					// If there are more categories than there are slots in the inventory, log the issue.
					
					player.sendMessage(plugin.config.prefix + "\u00A7cAn error occured with the shop gui, please contact a mod or admin.");
					plugin.log.warning("Home menu contians too many categories...");
					break;
				}
				

				// Cramming area looks like this (X = no item, O = item allowed)
				// Example is on a 4 lined gui, but it scales.
				// X X X X X X X X X
				// X O O O O O O O X
				// X O O O O O O O X
				// X O O O O O O O X
				
				int startY = 1;
				
				int startX = 1;
				int endX = 7;
				
				int cramming = (length - 1) * 7;
				
				if(categories.size() > cramming) {
					// If cant fit all category icons in current cramming area, increase it to this:
					// X O O O O O O O X
					// X O O O O O O O X
					// X O O O O O O O X
					// X O O O O O O O X
					
					startY = 0;
					cramming += 7;
				}
				
				if(categories.size() > cramming) {
					// If still can't fit all categories, use the full size of the inventory as the layout.
					
					startX = 0;
					endX = 9;
				}
				
				int x = startX, y = startY;
				
				for(ShopCategory category : categories) {
					homeLayout.put((y * 9 + x), category);
					
					x++;
					if(x > endX) {
						y++;
						x = startX;
					}
				}
			}
			
			Set<Integer> iconSlots = homeLayout.keySet();
			for(int slot : iconSlots) {
				ShopCategory category = homeLayout.get(slot);
				inventory.setItem(slot, category.icon);
			}
			
			break;
			
		case CATEGORY:			
			// Back button
			inventory.setItem(bottom + plugin.config.backButtonPos, createButton(plugin.config.backButtonMaterial, plugin.config.backButtonText, null));
			
			int pageCount = selectedCategory.estimatePageCount();
			
			// Previous Page button
			if(pageIndex > 0) {
				inventory.setItem(bottom + plugin.config.prevButtonPos, createButton(plugin.config.prevButtonMaterial, plugin.config.prevButtonText, null));
			}
			
			// Next Page Button
			if(pageIndex < (pageCount - 1)) {
				inventory.setItem(bottom + plugin.config.nextButtonPos, createButton(plugin.config.nextButtonMaterial, plugin.config.nextButtonText, null));
			}
			if(currentCategoryItems == null) {
				currentCategoryItems = selectedCategory.getPageItems(pageIndex);
			}
			
			for(int i = 0; i < currentCategoryItems.size(); i++) {
				ShopItem currentItem = currentCategoryItems.get(i);
				ItemStack stack = currentItem.createSample();
				ItemMeta meta = stack.getItemMeta();
				List<String> lore = new ArrayList<>();
				
				if(currentItem.canBuy) lore.add("\u00A7cBuy Price: " + plugin.formatBalance(currentItem.buyPrice));
				if(currentItem.canSell) lore.add("\u00A7aSell Price: " + plugin.formatBalance(currentItem.sellPrice));
				
				meta.setLore(lore);
				stack.setItemMeta(meta);
				
				inventory.setItem(i, stack);
			}
			
			break;
			
		case ITEM:	
			int lines = inventory.getSize() / 9;
			
			int mid;
			switch(lines) {
			case 2:
				mid = 0;
				break;
			case 3:
				mid = 9;
				break;
			case 4:
				mid = 9;
				break;
			case 5:
				mid = 18;
				break;
			case 6:
				mid = 27;
				break;
			default: mid = 0; break;
			}
			
			// Back button
			inventory.setItem(bottom + plugin.config.backButtonPos, createButton(plugin.config.backButtonMaterial, plugin.config.backButtonText, null));
			
			// Preview Item
			inventory.setItem(mid + 4, selectedItem.createSample());
			
			// Buy Buttons
			if(selectedItem.canBuy) {
				int i = 0;
				for(int amount : plugin.config.bulkAmounts) {
					ItemStack buyButton = plugin.config.createBuyButton(player, selectedItem, amount);
					
					inventory.setItem(mid + 5 + i, buyButton);
					
					i++;
					
					if(i >= 4) break;
				}
			}
			else inventory.setItem(mid + 5, createButton(plugin.config.disabledButtonMaterial, plugin.config.disabledButtonText, null));
			
			if(selectedItem.canSell) {
				int i = 0;
				for(int amount : plugin.config.bulkAmounts) {
					ItemStack sellButton = plugin.config.createSellButton(player, selectedItem, amount);

					inventory.setItem(mid + 3 - i, sellButton);
					
					i++;
					
					if(i >= 4) break;
				}
			}
			else inventory.setItem(mid + 3, createButton(plugin.config.disabledButtonMaterial, plugin.config.disabledButtonText, null));
			
			break;
		}
	}
	
	void recreate() {
		refreshing = true;
		
		view.close();
		view = open();
	}
	
	String denormalize(String str) {
		return WordUtils.capitalizeFully(str.replace('_', ' '));
	}
	
	InventoryView open() {
		String displayName;
		int size;
		
		switch(state) {
		default: return null;
		
		case HOME:
			displayName = plugin.config.homePageTitle;
			size = plugin.config.homePageLength;
			
			break;
			
		case CATEGORY:
			displayName = selectedCategory.displayName;			
			size = plugin.config.categoryPageLength;
			
			break;
			
		case ITEM:
			displayName = plugin.config.buyPagePrefix + denormalize(selectedItem.mat.toString());
			size = plugin.config.buyPageLength;
			
			break;
		}
		
		inventory = Bukkit.createInventory(null, size, displayName);
		populate();
		
		return player.openInventory(inventory);
	}
	
	void failSound() {
		if(plugin.config.failSound != null) {
			player.playSound(player.getLocation(), plugin.config.failSound, 1f, 1f);
		}
	}
	void buySound() {
		if(plugin.config.buySound != null) {
			player.playSound(player.getLocation(), plugin.config.buySound, 1f, 1f);
		}
	}
	void sellSound() {
		if(plugin.config.sellSound != null) {
			player.playSound(player.getLocation(), plugin.config.sellSound, 1f, 1f);
		}
	}
	
	void buyButtonClicked(int index) {
		int[] bulkAmounts = plugin.config.bulkAmounts;
		
		int amount = bulkAmounts[index];
		if(amount == -1) {
			amount = plugin.getMaxPurchase(player, selectedItem);
		}
		
		double price = amount * selectedItem.buyPrice;
		
		if((plugin.getBalance(player) < price) || (amount == 0)) {
			player.sendMessage(plugin.config.prefix + plugin.config.notEnoughCurrencyError);
			player.closeInventory();
			failSound();
		}
		else {
			if(plugin.getMaxPurchase(player, selectedItem) < amount) {
				player.sendMessage(plugin.config.prefix + plugin.config.notEnoughSpaceError);
				player.closeInventory();
				failSound();
				
				return;
			}
			
			if(plugin.chargePlayer(player, price)) {
				ItemStack purchasedItems = selectedItem.createSample();
				purchasedItems.setAmount(amount);
				
				player.getInventory().addItem(purchasedItems);
				buySound();
			}
			else {
				player.closeInventory();
				failSound();
			}
		}
	}
	void sellButtonClicked(int index) {
		int[] bulkAmounts = plugin.config.bulkAmounts;

		int amount = bulkAmounts[index];
		if(amount == -1) {
			amount = plugin.countMatchingItems(player, selectedItem);
		}
		
		if((plugin.countMatchingItems(player, selectedItem) < amount) || (amount == 0)) {
			player.sendMessage(plugin.config.prefix + plugin.config.notEnoughItemsError);
			player.closeInventory();
			failSound();
			
			return;
		}
		
		double price = amount * selectedItem.sellPrice;
		
		if(plugin.payPlayer(player, price)) {
			plugin.removeSoldItems(player, selectedItem, amount);
			sellSound();
		}
		else {
			player.closeInventory();
			failSound();
		}
	}
	
	Map<Integer, ShopCategory> homeLayout;
	Map<Integer, ShopItem> categoryLayout;
	
	void passClick(InventoryClickEvent event) {		
		if(event.getClickedInventory() == inventory) {			
			State prevState = state;

			int bottom = inventory.getSize() - 9;
			int lines = inventory.getSize() / 9;
			
			int mid;
			switch(lines) {
			case 2:
				mid = 0;
				break;
			case 3:
				mid = 9;
				break;
			case 4:
				mid = 9;
				break;
			case 5:
				mid = 18;
				break;
			case 6:
				mid = 27;
				break;
			default: mid = 0; break;
			}
			
			int slot = event.getSlot();
			
			switch(state) {
			default: break;
			case HOME:
				
				ShopCategory category = homeLayout.get(slot);
				if(category != null) {
					selectedCategory = category;
					pageIndex = 0;
					
					categoryLayout.clear();
					currentCategoryItems = null;
					
					state = State.CATEGORY;
				}
				
				break;
				
			case CATEGORY:

				if(slot < currentCategoryItems.size()) {
					ShopItem item = currentCategoryItems.get(slot);
					if(item != null) {
						selectedItem = item;
						state = State.ITEM;
						
						break;
					}
				}
				
				if(slot == (bottom + plugin.config.backButtonPos)) {
					prevState = null;
					state = State.HOME;
					break;
				}
				
				if(slot == (bottom + plugin.config.nextButtonPos)) {
					currentCategoryItems = null;
					if(pageIndex < (selectedCategory.estimatePageCount() - 1)) {
						pageIndex++;
					}
					
					break;
				}
				if(slot == (bottom + plugin.config.prevButtonPos)) {
					currentCategoryItems = null;
					if(pageIndex > 0) {
						pageIndex--;
					}
					
					break;
				}
				
				break;
				
			case ITEM:
				int fnSlot = slot - mid;
				switch(fnSlot) {
				case 0:
				case 1:
				case 2:
				case 3:
					if(selectedItem.canSell) {
						sellButtonClicked(3 - fnSlot);
					}
					break;
					
				// 4th slot is the item preview
					
				case 5:
				case 6:
				case 7:
				case 8:
					if(selectedItem.canBuy) {
						buyButtonClicked(fnSlot - 5);
					}
					break;
				}
				
				if(slot == (bottom + plugin.config.backButtonPos)) {
					state = State.CATEGORY;
				}
				
				break;
			}
			
			if(state != prevState) {
				recreate();
			}
			else populate(); // Aka, refresh.
		}
	}
}
