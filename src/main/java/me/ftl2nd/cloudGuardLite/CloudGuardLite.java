package me.ftl2nd.cloudGuardLite;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class CloudGuardLite extends JavaPlugin {
   private ConfigLoader configLoader;
   private EventListener eventListener;
   private Checker checker;
   private boolean geyserInstalled;

   public void onEnable() {
      ConsoleCommandSender var10000 = this.getServer().getConsoleSender();
      String var10001 = String.valueOf(ChatColor.LIGHT_PURPLE);
      var10000.sendMessage(var10001 + "=============== INFO ===============");
      var10000 = this.getServer().getConsoleSender();
      var10001 = String.valueOf(ChatColor.LIGHT_PURPLE);
      var10000.sendMessage(var10001 + "Plugin: " + String.valueOf(ChatColor.GREEN) + "CloudGuardLite");
      var10000 = this.getServer().getConsoleSender();
      var10001 = String.valueOf(ChatColor.LIGHT_PURPLE);
      var10000.sendMessage(var10001 + "Version: " + String.valueOf(ChatColor.GREEN) + this.getDescription().getVersion());
      var10000 = this.getServer().getConsoleSender();
      var10001 = String.valueOf(ChatColor.LIGHT_PURPLE);
      var10000.sendMessage(var10001 + "Author: " + String.valueOf(ChatColor.GREEN) + (String)this.getDescription().getAuthors().get(0));
      var10000 = this.getServer().getConsoleSender();
      var10001 = String.valueOf(ChatColor.LIGHT_PURPLE);
      var10000.sendMessage(var10001 + "Bio: " + String.valueOf(ChatColor.GREEN) + this.getDescription().getWebsite());
      var10000 = this.getServer().getConsoleSender();
      var10001 = String.valueOf(ChatColor.LIGHT_PURPLE);
      var10000.sendMessage(var10001 + "====================================");
      this.geyserInstalled = this.getServer().getPluginManager().getPlugin("Geyser-Spigot") != null;
      this.configLoader = new ConfigLoader(this);
      this.eventListener = new EventListener(this, this.configLoader);
      this.getServer().getPluginManager().registerEvents(this.eventListener, this);
      this.checker = new Checker(this, this.configLoader);
      ((PluginCommand)Objects.requireNonNull(this.getCommand("cglreloadconfig"))).setExecutor(new ReloadConfigCommand(this, this.configLoader, this.checker, this.eventListener));
      (new UpdateChecker(this.getDescription().getVersion(), this.getLogger())).checkForUpdate();
      this.getServer().getConsoleSender().sendMessage(String.valueOf(ChatColor.GREEN) + "[CloudGuardLite] Plugin đã được kích hoạt thành công!");
   }

   public void onDisable() {
      this.checker.stop();
      if (this.configLoader != null && this.eventListener != null) {
         if (this.configLoader.isStopServer()) {
            if (this.eventListener.isLegitStop()) {
               this.getLogger().info("Plugin đã được dừng một cách hợp lệ!");
            } else {
               String var10000 = String.valueOf(ChatColor.RED);
               Bukkit.broadcastMessage(var10000 + String.valueOf(ChatColor.BOLD) + "Server đang bị tấn công Force OP, đang dừng server...");
               Bukkit.shutdown();
            }
         }
      }
   }

   public boolean isGeyserPresent() {
      return Bukkit.getPluginManager().getPlugin("Geyser-Spigot") != null;
   }

   public boolean isBungeePresent() {
      try {
         return Bukkit.spigot().getConfig().getBoolean("settings.bungeecord", false);
      } catch (Exception var2) {
         return false;
      }
   }
}