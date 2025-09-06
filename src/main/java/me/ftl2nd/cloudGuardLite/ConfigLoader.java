package me.ftl2nd.cloudGuardLite;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigLoader {
   private final CloudGuardLite plugin;
   private FileConfiguration config;
   private final List<String> opList = new CopyOnWriteArrayList();
   private final List<String> sensitivePermissions = new CopyOnWriteArrayList();
   private final List<String> punishCommands = new CopyOnWriteArrayList();
   private final List<String> logoutActions = new CopyOnWriteArrayList();
   private boolean disableOpCommand;
   private boolean blockCreativeMode;
   private boolean stopServer;
   private boolean isLogoutActionsEnabled;
   private boolean isAntiUUIDSpoof;
   private int checkInterval;
   private String configVersion = "unknown";
   private String defaultConfigVersion = "unknown";

   public ConfigLoader(CloudGuardLite plugin) {
      this.plugin = plugin;
      this.reloadConfig();
   }

   public void reloadConfig() {
      File configFile = new File(this.plugin.getDataFolder(), "config.yml");
      if (!configFile.exists()) {
         this.plugin.saveDefaultConfig();
      }

      this.config = YamlConfiguration.loadConfiguration(configFile);
      YamlConfiguration defaultConfig = new YamlConfiguration();

      try {
         label78: {
            InputStream defConfigStream = this.plugin.getResource("config.yml");

            label79: {
               try {
                  if (defConfigStream == null) {
                     this.plugin.getLogger().severe("Không thể tìm thấy config mặc định trong resources!");
                     break label79;
                  }

                  defaultConfig.load(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
               } catch (Throwable var8) {
                  if (defConfigStream != null) {
                     try {
                        defConfigStream.close();
                     } catch (Throwable var6) {
                        var8.addSuppressed(var6);
                     }
                  }

                  throw var8;
               }

               if (defConfigStream != null) {
                  defConfigStream.close();
               }
               break label78;
            }

            if (defConfigStream != null) {
               defConfigStream.close();
            }

            return;
         }
      } catch (InvalidConfigurationException | IOException var9) {
         this.plugin.getLogger().severe("Không thể load config mặc định!");
         var9.printStackTrace();
         return;
      }

      boolean modified = false;
      Iterator var4 = defaultConfig.getKeys(true).iterator();

      while(var4.hasNext()) {
         String key = (String)var4.next();
         if (!this.config.contains(key)) {
            this.config.set(key, defaultConfig.get(key));
            modified = true;
         }
      }

      this.configVersion = this.config.getString("config_version", "unknown");
      this.defaultConfigVersion = defaultConfig.getString("config_version", "unknown");
      if (!this.configVersion.equals(this.defaultConfigVersion)) {
         this.plugin.getLogger().warning("Config của bạn đang dùng phiên bản " + this.configVersion + ", phiên bản mới là " + this.defaultConfigVersion + ".");
         this.config.set("config_version", this.defaultConfigVersion);
         modified = true;
      }

      if (modified) {
         try {
            this.config.save(configFile);
            this.plugin.getLogger().warning("Đã cập nhật file config.yml!");
         } catch (IOException var7) {
            this.plugin.getLogger().severe("Không thể ghi lại file config.yml!");
            var7.printStackTrace();
         }
      }

      this.opList.clear();
      this.sensitivePermissions.clear();
      this.punishCommands.clear();
      this.logoutActions.clear();
      this.opList.addAll(this.config.getStringList("op_list"));
      this.sensitivePermissions.addAll(this.config.getStringList("sensitive_permissions"));
      this.punishCommands.addAll(this.config.getStringList("punish_command"));
      this.disableOpCommand = this.config.getBoolean("disable_op_command");
      this.blockCreativeMode = this.config.getBoolean("block_gamemode_creative");
      this.stopServer = this.config.getBoolean("stop_server");
      this.isLogoutActionsEnabled = this.config.getBoolean("logout_actions.enable");
      this.logoutActions.addAll(this.config.getStringList("logout_actions.commands"));
      this.isAntiUUIDSpoof = this.config.getBoolean("anti_uuid_spoof", true);
      this.checkInterval = this.config.getInt("check_interval", 150);
   }

   public List<String> getOpList() {
      return this.opList;
   }

   public List<String> getSensitivePermissions() {
      return this.sensitivePermissions;
   }

   public List<String> getPunishCommands() {
      return this.punishCommands;
   }

   public boolean isDisableOpCommand() {
      return this.disableOpCommand;
   }

   public boolean isBlockCreativeMode() {
      return this.blockCreativeMode;
   }

   public boolean isStopServer() {
      return this.stopServer;
   }

   public boolean isLogoutActionsEnabled() {
      return this.isLogoutActionsEnabled;
   }

   public List<String> getLogoutActions() {
      return this.logoutActions;
   }

   public int getCheckInterval() {
      return this.checkInterval;
   }

   public String getConfigVersion() {
      return this.configVersion;
   }

   public String getDefaultConfigVersion() {
      return this.defaultConfigVersion;
   }

   public boolean isAntiUUIDSpoof() {
      return this.isAntiUUIDSpoof;
   }
}