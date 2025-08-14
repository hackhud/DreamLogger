package ua.hackhud.dreamLogger.listeners;

import noppes.npcs.side.server.bukkit.listeners.NPCDropEvent;
import noppes.npcs.side.server.bukkit.listeners.NPCKillingEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.jok1r.diverseRpgAttributes.EnumAttribute;
import ua.hackhud.dreamLogger.Main;
import ua.hackhud.dreamLogger.util.AttributeUtils;
import ua.hackhud.dreamLogger.util.HexUtils;

import java.io.IOException;

public class NPCListener implements Listener {

  private final Main plugin;

  public NPCListener(Main plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onNpcDrop(NPCDropEvent.Post event) {
    if (!event.getDrop().hasItemMeta()) {
      return;
    }

    String message = plugin.npcDrop.replaceAll("%d", plugin.getTime())
            .replaceAll("%p", event.getPlayer());

    String itemName = event.getDrop().getItemMeta().getDisplayName();
      double npcDropChanceValue = AttributeUtils.getAttributeBonus(
              Bukkit.getPlayer(event.getPlayer()),
              EnumAttribute.NPC_DROP_CHANCE
      );

      String dangerous = "";
      if (npcDropChanceValue >= 10) {
          dangerous = (npcDropChanceValue <= 15)
                  ? "[NEEDSTOBECHECKED]"
                  : "[DANGEROUS]";
      }

      String chanceMessage = String.format("%s %s", npcDropChanceValue, dangerous).trim();

      message = message
              .replace("%n", itemName)
              .replace("%i", String.valueOf(event.getDrop().getAmount()))
              .replace("%c", chanceMessage);

    try {
      plugin.logging(message);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @EventHandler
  public void onNpcKill(NPCKillingEvent event) {
    String message = plugin.npcKilling
            .replaceAll("%d", plugin.getTime())
            .replaceAll("%p", event.getPlayer().getName())
            .replaceAll("%npc", event.getNpc());

    try {
      plugin.logging(message);
      plugin.saveKilledData(event.getNpc(), event.getPlayer().getName());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
