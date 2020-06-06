/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package me.filoghost.chestcommands.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.filoghost.chestcommands.ChestCommands;
import me.filoghost.chestcommands.MenuManager;
import me.filoghost.chestcommands.api.Icon;
import me.filoghost.chestcommands.api.IconMenu;
import me.filoghost.chestcommands.task.ExecuteActionsTask;
import java.util.HashMap;
import java.util.Map;

public class InventoryListener implements Listener {
	
	private static Map<Player, Long> antiClickSpam = new HashMap<>();
	
	private MenuManager menuManager;
	
	public InventoryListener(MenuManager menuManager) {
		this.menuManager = menuManager;
	}
	

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onInteract(PlayerInteractEvent event) {
		if (event.hasItem() && event.getAction() != Action.PHYSICAL) {
			menuManager.openMenuByItem(event.getPlayer(), event.getItem(), event.getAction());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onInventoryClick(InventoryClickEvent event) {
		IconMenu menu = menuManager.getIconMenu(event.getInventory());
		if (menu == null) {
			return;
		}
		
		event.setCancelled(true); // First thing to do, if an exception is thrown at least the player doesn't take the item

		int slot = event.getRawSlot();
		if (slot < 0 || slot >= menu.getSize()) {
			return;
		}

		Icon icon = menu.getIconRaw(slot);
		if (icon == null || event.getInventory().getItem(slot) == null) {
			return;
		}

		Player clicker = (Player) event.getWhoClicked();

		Long cooldownUntil = antiClickSpam.get(clicker);
		long now = System.currentTimeMillis();
		int minDelay = ChestCommands.getSettings().anti_click_spam_delay;

		if (minDelay > 0) {
			if (cooldownUntil != null && cooldownUntil > now) {
				return;
			} else {
				antiClickSpam.put(clicker, now + minDelay);
			}
		}

		// Closes the inventory and executes actions AFTER the event
		Bukkit.getScheduler().runTask(ChestCommands.getInstance(), new ExecuteActionsTask(clicker, icon));
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		antiClickSpam.remove(event.getPlayer());
	}

}
