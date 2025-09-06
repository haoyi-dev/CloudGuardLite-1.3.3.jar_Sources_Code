package me.ftl2nd.cloudGuardLite;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class ReloadConfigCommand implements CommandExecutor {
   private final CloudGuardLite plugin;
   private final ConfigLoader configLoader;
   private final Checker checker;
   private final EventListener eventListener;
   private final Set<String> cachedOpList;

   public ReloadConfigCommand(CloudGuardLite plugin, ConfigLoader configLoader, Checker checker, EventListener eventListener) {
      this.plugin = plugin;
      this.configLoader = configLoader;
      this.checker = checker;
      this.eventListener = eventListener;
      this.cachedOpList = new HashSet(configLoader.getOpList());
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof ConsoleCommandSender) && sender instanceof Player) {
         Player player = (Player)sender;
         if (!this.configLoader.getOpList().contains(player.getName())) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "Bạn không có quyền thực hiện lệnh này!");
            return true;
         }
      }

      sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "Đang reload config của CloudGuardLite...");
      CompletableFuture.runAsync(() -> {
         try {
            this.checker.stop();
            this.configLoader.reloadConfig();
            this.eventListener.reloadConfig();
            this.checker.reloadConfigData();
            this.checker.startChecking();
            sender.sendMessage(String.valueOf(ChatColor.GREEN) + "Đã reload config của CloudGuardLite thành công!");
            this.plugin.getLogger().info("Đã reload config của CloudGuardLite!");
         } catch (Exception var3) {
            this.plugin.getLogger().severe("Đã xảy ra lỗi khi reload config của CloudGuardLite: " + var3.getMessage());
            sender.sendMessage(String.valueOf(ChatColor.RED) + "Reload config của CloudGuardLite thất bại! Hãy kiểm tra console để biết thêm chi tiết.");
         }

      });
      return true;
   }
}