package ua.hackhud.dreamLogger.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.jok1r.diverseRpgAttributes.AttributeManager;
import ru.jok1r.diverseRpgAttributes.EnumAttribute;
import ru.jok1r.diverseRpgAttributes.Main;

public class AttributeUtils {

    public static double getAttributeBonus(Player player, EnumAttribute attribute) {
        Main plugin = (Main) Bukkit.getPluginManager().getPlugin("DreamRpgAttributes");
        if (plugin == null || !plugin.isEnabled()) {
            return 0.0;
        }

        AttributeManager manager = plugin.manager;
        if (manager == null) {
            return 0.0;
        }

        return manager.getAllBonus(player, attribute);
    }
}
