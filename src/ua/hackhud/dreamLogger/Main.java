package ua.hackhud.dreamLogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ua.hackhud.dreamLogger.listeners.NPCListener;
import ua.hackhud.dreamLogger.listeners.PlayerListener;
import ua.hackhud.dreamLogger.util.HexUtils;

public class Main extends JavaPlugin {
  public String takingItem;
  
  public String takingItemFromPlayer;
  
  public String droppingItem;
  
  public String killingItem;
  
  public String chestTaking;
  
  public String npcDrop;
  
  public String npcKilling;
  
  private FileWriter logWriter;
  
  static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);
  
  public void onEnable() {
    getServer().getPluginManager().registerEvents((Listener)new PlayerListener(this), (Plugin)this);
    getServer().getPluginManager().registerEvents((Listener)new NPCListener(this), (Plugin)this);
    getCommand("dlogger").setExecutor(new CommandDiverseLogger(this));
    register();
  }
  
  public void register() {
    File cfg = new File(getDataFolder(), "config.yml");
    if (!cfg.exists())
      saveResource("config.yml", false); 
    YamlConfiguration config = YamlConfiguration.loadConfiguration(cfg);
    this.takingItem = config.getString("messages.takingItem");
    this.takingItemFromPlayer = config.getString("messages.takingItemFromPlayer");
    this.droppingItem = config.getString("messages.droppingItem");
    this.killingItem = config.getString("messages.killingItem");
    this.chestTaking = config.getString("messages.chestTaking");
    this.npcDrop = config.getString("messages.npcDrop");
    this.npcKilling = config.getString("messages.npcKilling");
  }
  
  public String getTime() {
    LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
    return "[" + localDateTime.getDayOfMonth() + "-" + localDateTime.getMonth().getValue() + "-" + localDateTime.getYear() + "] [" + localDateTime.getHour() + ":" + localDateTime.getMinute() + ":" + localDateTime.getSecond() + "]";
  }
  
  public void saveKilledData(final String npc, final String player) {
    EXECUTOR_SERVICE.execute(new Runnable() {
          public void run() {
            File file = new File(Main.this.getDataFolder(), "statistics.yml");
            if (!file.exists())
              try {
                file.createNewFile();
              } catch (IOException e) {
                e.printStackTrace();
              }  
            YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
            String npc_conf = npc.replaceAll(" ", "_");
            if (conf.getConfigurationSection("npc") == null)
              conf.set("npc." + npc_conf, Integer.valueOf(0)); 
            int countKilled = conf.getInt("npc." + npc_conf);
            if (countKilled == 0) {
              conf.set("npc." + npc_conf, Integer.valueOf(1));
            } else {
              conf.set("npc." + npc_conf, Integer.valueOf(countKilled + 1));
            } 
            int countPlayer = conf.getInt("players." + player + "." + npc_conf);
            if (countPlayer == 0) {
              conf.set("players." + player + "." + npc_conf, Integer.valueOf(1));
            } else {
              conf.set("players." + player + "." + npc_conf, Integer.valueOf(countPlayer + 1));
            } 
            try {
              conf.save(file);
            } catch (IOException e) {
              e.printStackTrace();
            } 
          }
        });
  }
  
  public void logging(final String text) throws IOException {
    if (this.logWriter == null) {
      File file = new File(getDataFolder(), "log");
      if (!file.exists())
        file.mkdir(); 
      file = new File(file, getTime().replaceAll(" ", "_") + ".log");
      file.createNewFile();
      this.logWriter = new FileWriter(file);
    } 
    EXECUTOR_SERVICE.execute(new Runnable() {
          public void run() {
            try {
              Main.this.logWriter.write(HexUtils.stripHexColors(text));
              Main.this.logWriter.append('\n');
              Main.this.logWriter.flush();
            } catch (IOException e) {
              e.printStackTrace();
            } 
          }
        });
  }
  
  public void sendMessage(CommandSender sender, String message) {
    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
  }
}
