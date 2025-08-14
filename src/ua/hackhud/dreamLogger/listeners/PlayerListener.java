package ua.hackhud.dreamLogger.listeners;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import ua.hackhud.dreamLogger.Main;
import ua.hackhud.dreamLogger.dream.DroppedData;

public class PlayerListener implements Listener {
  private final Main plugin;
  private final Map<UUID, DroppedData> dataMap = new HashMap<>();
  private final Map<UUID, DroppedData> killingData = new HashMap<>();

  public PlayerListener(Main plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onDeath(PlayerDeathEvent event) {
    if (!event.getDrops().isEmpty()) {
      final String player = event.getEntity().getName();
      (new BukkitRunnable() {
        public void run() {
          long time = System.currentTimeMillis();
          if (PlayerListener.this.plugin.getServer().getPlayer(player) != null) {
            List<Entity> data = PlayerListener.this.plugin.getServer().getPlayer(player).getNearbyEntities(5.0D, 5.0D, 5.0D);
            for (Entity entity : data) {
              if (entity.getType() == EntityType.DROPPED_ITEM) {
                Item item = (Item)entity;
                if (item.getTicksLived() < 10L)
                  PlayerListener.this.killingData.put(item.getUniqueId(), new DroppedData(item.getUniqueId(), Long.valueOf(time), player));
              }
            }
          }
        }
      }).runTaskLater((Plugin)this.plugin, 8L);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onTakeItem(PlayerPickupItemEvent event) {
    if (!event.isCancelled()) {
      ItemStack stack = event.getItem().getItemStack();
      UUID uuid = event.getItem().getUniqueId();
      String message;

      if (this.dataMap.containsKey(uuid)) {
        message = this.plugin.takingItemFromPlayer.replaceAll("%p1", this.dataMap.get(uuid).getPlayer());
        double time = (System.currentTimeMillis() - this.dataMap.get(uuid).getDroppedTime().longValue()) / 1000.0D;
        message = message.replaceAll("%t", String.valueOf(time));
        this.dataMap.remove(uuid);
      } else if (this.killingData.containsKey(uuid)) {
        message = this.plugin.killingItem.replaceAll("%p1", this.killingData.get(uuid).getPlayer());
        this.killingData.remove(uuid);
      } else {
        message = this.plugin.takingItem;
      }

      if (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()) {
        message = message.replaceAll("%n", stack.getItemMeta().getDisplayName());
      } else {
        message = message.replaceAll("%n", String.valueOf(stack.getTypeId()));
      }

      message = message.replaceAll("%u", uuid.toString())
              .replaceAll("%i", String.valueOf(stack.getAmount()))
              .replaceAll("%p", event.getPlayer().getName())
              .replaceAll("%d", this.plugin.getTime())
              .replaceAll("%x", String.valueOf((int)event.getPlayer().getLocation().getX()))
              .replaceAll("%y", String.valueOf((int)event.getPlayer().getLocation().getY()))
              .replaceAll("%z", String.valueOf((int)event.getPlayer().getLocation().getZ()));

      try {
        this.plugin.logging(message);
      } catch (IOException exception) {
        exception.printStackTrace();
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onDropItem(PlayerDropItemEvent event) {
    if (!event.isCancelled()) {
      this.dataMap.put(event.getItemDrop().getUniqueId(),
              new DroppedData(event.getItemDrop().getUniqueId(),
                      Long.valueOf(System.currentTimeMillis()),
                      event.getPlayer().getName()));

      String message = this.plugin.droppingItem.replaceAll("%p", event.getPlayer().getName());
      ItemStack stack = event.getItemDrop().getItemStack();

      if (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()) {
        message = message.replaceAll("%n", stack.getItemMeta().getDisplayName());
      } else {
        message = message.replaceAll("%n", String.valueOf(stack.getTypeId()));
      }

      message = message.replaceAll("%u", event.getItemDrop().getUniqueId().toString())
              .replaceAll("%i", String.valueOf(stack.getAmount()))
              .replaceAll("%d", this.plugin.getTime())
              .replaceAll("%x", String.valueOf((int)event.getPlayer().getLocation().getX()))
              .replaceAll("%y", String.valueOf((int)event.getPlayer().getLocation().getY()))
              .replaceAll("%z", String.valueOf((int)event.getPlayer().getLocation().getZ()));

      try {
        this.plugin.logging(message);
      } catch (IOException exception) {
        exception.printStackTrace();
      }
    }
  }
}