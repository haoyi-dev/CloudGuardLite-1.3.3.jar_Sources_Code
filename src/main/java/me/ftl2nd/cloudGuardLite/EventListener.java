package me.ftl2nd.cloudGuardLite;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class EventListener implements Listener {
   private final CloudGuardLite plugin;
   private final ConfigLoader configLoader;
   private volatile boolean legitStop = false;
   private static final Set<String> STOP_COMMANDS = Set.of("stop", "restart", "reload");
   private Set<String> cachedOpList;

   public EventListener(CloudGuardLite plugin, ConfigLoader configLoader) {
      this.plugin = plugin;
      this.configLoader = configLoader;
      this.cachedOpList = new HashSet(configLoader.getOpList());
   }

   public void reloadConfig() {
      this.cachedOpList = new HashSet(this.configLoader.getOpList());
   }

   @EventHandler
   public void onServerCommand(ServerCommandEvent event) {
      String command = event.getCommand().toLowerCase(Locale.ROOT);
      boolean stopServer = this.configLoader.isStopServer();
      boolean disableOp = this.configLoader.isDisableOpCommand();
      if (stopServer && STOP_COMMANDS.contains(command)) {
         this.legitStop = true;
      }

      if (disableOp && command.startsWith("/op ") || disableOp && command.startsWith("/minecraft:op ")) {
         event.setCancelled(true);
         this.plugin.getLogger().warning("Lệnh OP đã bị chặn bởi CloudGuardLite.");
      }

   }

   @EventHandler
   public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
      String command = event.getMessage();
      boolean stopServer = this.configLoader.isStopServer();
      boolean disableOp = this.configLoader.isDisableOpCommand();
      if (command.length() > 1) {
         String trimmedCommand = command.substring(1).toLowerCase(Locale.ROOT);
         if (stopServer && STOP_COMMANDS.contains(trimmedCommand)) {
            Player player = event.getPlayer();
            if (player.hasPermission("bukkit.command." + trimmedCommand)) {
               this.legitStop = true;
            }
         }
      }

      if (disableOp && command.startsWith("/op ") || disableOp && command.startsWith("/minecraft:op ")) {
         event.setCancelled(true);
         event.getPlayer().sendMessage(String.valueOf(ChatColor.RED) + "Lệnh OP đã bị chặn bởi CloudGuardLite.");
         this.plugin.getLogger().warning("Lệnh OP đã bị chặn bởi CloudGuardLite.");
      }

   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      final Player player = event.getPlayer();
      boolean blockCreativeMode = this.configLoader.isBlockCreativeMode();
      if (this.cachedOpList.contains(player.getName())) {
         (new BukkitRunnable() {
            public void run() {
               player.sendTitle(String.valueOf(ChatColor.GREEN) + "✔", String.valueOf(ChatColor.YELLOW) + "Bạn là người chơi có quyền hợp lệ!", 10, 20, 10);
            }
         }).runTask(this.plugin);
      }

      if (blockCreativeMode && !this.cachedOpList.contains(player.getName()) && event.getPlayer().getGameMode().name().equals("CREATIVE")) {
         player.sendMessage(String.valueOf(ChatColor.YELLOW) + "Gamemode của bạn đã được chuyển về Survival do bạn không có quyền!");
         event.getPlayer().setGameMode(GameMode.SURVIVAL);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerChangeGamemode(PlayerGameModeChangeEvent event) {
      Player player = event.getPlayer();
      boolean blockCreativeMode = this.configLoader.isBlockCreativeMode();
      if (blockCreativeMode && !this.cachedOpList.contains(player.getName()) && event.getNewGameMode().name().equals("CREATIVE")) {
         player.sendMessage(String.valueOf(ChatColor.RED) + "Bạn không được phép ở chế độ Creative!");
         event.setCancelled(true);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onPlayerInventory(InventoryOpenEvent event) {
      Player player = (Player)event.getPlayer();
      boolean blockCreativeMode = this.configLoader.isBlockCreativeMode();
      if (blockCreativeMode && !this.cachedOpList.contains(player.getName()) && event.getPlayer().getGameMode().name().equals("CREATIVE")) {
         player.sendMessage(String.valueOf(ChatColor.RED) + "Bạn không được phép ở chế độ Creative!");
         event.setCancelled(true);
         player.setGameMode(GameMode.SURVIVAL);
      }

   }

   @EventHandler
   public void preJoin(AsyncPlayerPreLoginEvent event) {
      if (!Bukkit.getOnlineMode()) {
         if (this.configLoader.isAntiUUIDSpoof()) {
            String name = event.getName();
            UUID actualUUID = event.getUniqueId();
            boolean isBedrock = false;
            if (Bukkit.getPluginManager().getPlugin("Geyser-Spigot") != null) {
               try {
                  Class<?> geyserApiClass = Class.forName("org.geysermc.geyser.api.GeyserApi");
                  Object apiInstance = geyserApiClass.getMethod("api").invoke((Object)null);
                  isBedrock = (Boolean)geyserApiClass.getMethod("isBedrockPlayer", UUID.class).invoke(apiInstance, actualUUID);
               } catch (Throwable var7) {
               }
            }

            if (!isBedrock && this.isFloodgateUUID(actualUUID)) {
               isBedrock = true;
            }

            if (isBedrock) {
               Bukkit.getLogger().info("[CloudGuardLite] Người chơi " + name + " đã tham gia bằng Bedrock Edition");
            } else {
               UUID expectedUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
               if (!actualUUID.equals(expectedUUID)) {
                  Bukkit.getLogger().warning("[CloudGuardLite] Đã ngăn chặn người chơi " + name + " kết nối với UUID không hợp lệ.");
                  Bukkit.getLogger().warning("[CloudGuardLite] UUID dự tính: " + String.valueOf(expectedUUID));
                  Bukkit.getLogger().warning("[CloudGuardLite] UUID nhận được: " + String.valueOf(actualUUID));
                  event.disallow(Result.KICK_OTHER, String.valueOf(ChatColor.RED) + "CloudGuardLite\nUUID của bạn không khớp với UUID mà chúng tôi dự tính!\nNếu bạn nghĩ đây là một lỗi, hãy liên hệ với quản trị viên máy chủ!");
               }

            }
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      if (this.configLoader.isLogoutActionsEnabled()) {
         if (this.configLoader.getOpList().contains(player.getName())) {
            List<String> commands = this.configLoader.getLogoutActions();
            if (!commands.isEmpty()) {
               Bukkit.getScheduler().runTask(this.plugin, () -> {
                  Iterator var2 = commands.iterator();

                  while(var2.hasNext()) {
                     String raw = (String)var2.next();
                     String command = raw.replace("%player%", player.getName());
                     Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                  }

               });
            }
         }
      }
   }

   private boolean isFloodgateUUID(UUID uuid) {
      return uuid.toString().startsWith("00000000-0000-0000-0009");
   }

   public boolean isLegitStop() {
      return this.legitStop;
   }
}