package org.braekpo1nt.mctmanager.commands;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DebugClickEvent implements Listener {
    
    
    public static final Component TITLE = Component.text("Choose your Class");
    
    public DebugClickEvent(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void clickEvent(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) {
            return;
        }
        if (!event.getView().title().equals(TITLE)) {
            return;
        }
        Player player = ((Player) event.getWhoClicked());
        if (event.getCurrentItem() == null) {
            return;
        }
        Material clickedItem = event.getCurrentItem().getType();
        switch (clickedItem) {
            case STONE_SWORD:
                player.getInventory().clear();
                player.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                player.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
                player.sendMessage("Selected Knight");
                break;
            case BOW:
                player.getInventory().clear();
                player.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                player.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                player.getInventory().addItem(new ItemStack(Material.BOW));
                player.getInventory().addItem(new ItemStack(Material.ARROW, 16));
                player.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
                player.sendMessage("Selected Archer");
                break;
            case IRON_SWORD:
                player.getInventory().clear();
                player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
                player.sendMessage("Selected Assassin");
                break;
            case LEATHER_CHESTPLATE:
                player.getInventory().clear();
                player.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
                player.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
                player.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
                player.sendMessage("Selected Tank");
                break;
            default:
                return;
        }
        player.closeInventory();
        
        event.setCancelled(true);
    }
    
}
