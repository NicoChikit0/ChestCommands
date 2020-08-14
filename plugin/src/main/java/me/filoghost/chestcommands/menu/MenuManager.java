/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.menu;

import me.filoghost.chestcommands.inventory.DefaultMenuInventory;
import me.filoghost.chestcommands.inventory.MenuInventoryHolder;
import me.filoghost.chestcommands.logging.Errors;
import me.filoghost.chestcommands.parsing.menu.LoadedMenu;
import me.filoghost.chestcommands.parsing.menu.MenuOpenItem;
import me.filoghost.commons.collection.CaseInsensitiveMap;
import me.filoghost.commons.logging.ErrorCollector;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MenuManager {
	
	private static Map<String, InternalIconMenu> menusByFile;
	private static Map<String, InternalIconMenu> menusByOpenCommand;
	private static Map<MenuOpenItem, InternalIconMenu> menusByOpenItem;
	
	public MenuManager() {
		menusByFile = new CaseInsensitiveMap<>();
		menusByOpenCommand = new CaseInsensitiveMap<>();
		menusByOpenItem = new HashMap<>();
	}
	
	public void clear() {
		menusByFile.clear();
		menusByOpenCommand.clear();
		menusByOpenItem.clear();
	}

	public InternalIconMenu getMenuByFileName(String fileName) {
		return menusByFile.get(fileName);
	}

	public void registerMenu(LoadedMenu loadedMenu, ErrorCollector errorCollector) {
		InternalIconMenu menu = loadedMenu.getMenu();

		String fileName = loadedMenu.getSourceFile().getFileName().toString();
		InternalIconMenu sameNameMenu = menusByFile.get(fileName);
		if (sameNameMenu != null) {
			errorCollector.add(Errors.Menu.duplicateMenuName(sameNameMenu.getSourceFile(), loadedMenu.getSourceFile()));
		}
		menusByFile.put(fileName, menu);

		if (loadedMenu.getOpenCommands() != null) {
			for (String openCommand : loadedMenu.getOpenCommands()) {
				if (!openCommand.isEmpty()) {
					InternalIconMenu sameCommandMenu = menusByOpenCommand.get(openCommand);
					if (sameCommandMenu != null) {
						errorCollector.add(Errors.Menu.duplicateMenuCommand(sameCommandMenu.getSourceFile(), loadedMenu.getSourceFile(), openCommand));
					}
					menusByOpenCommand.put(openCommand, menu);
				}
			}
		}

		if (loadedMenu.getOpenItem() != null) {
			menusByOpenItem.put(loadedMenu.getOpenItem(), menu);
		}
	}

	public void openMenuByItem(Player player, ItemStack itemInHand, Action clickAction) {
		menusByOpenItem.forEach((openItem, menu) -> {
			if (openItem.matches(itemInHand, clickAction)) {
				menu.openCheckingPermission(player);
			}
		});
	}

	public InternalIconMenu getMenuByOpenCommand(String openCommand) {
		return menusByOpenCommand.get(openCommand);
	}

	public Collection<String> getMenuFileNames() {
		return Collections.unmodifiableCollection(menusByFile.keySet());
	}
	
	public static boolean isMenuInventory(Inventory inventory) {
		return getMenuInventoryHolder(inventory) != null;
	}

	public static DefaultMenuInventory getOpenMenuInventory(Player player) {
		InventoryView view = player.getOpenInventory();
		if (view == null) {
			return null;
		}

		DefaultMenuInventory menuInventory = getOpenMenuInventory(view.getTopInventory());
		if (menuInventory == null) {
			menuInventory = getOpenMenuInventory(view.getBottomInventory());
		}
		
		return menuInventory;
	}
	
	
	public static DefaultMenuInventory getOpenMenuInventory(Inventory inventory) {
		MenuInventoryHolder inventoryHolder = getMenuInventoryHolder(inventory);
		if (inventoryHolder != null) {
			return inventoryHolder.getMenuInventory();
		} else {
			return null;
		}
	}
	
	private static MenuInventoryHolder getMenuInventoryHolder(Inventory inventory) {
		if (inventory.getHolder() instanceof MenuInventoryHolder) {
			return (MenuInventoryHolder) inventory.getHolder();
		} else {
			return null;
		}
	}

}
