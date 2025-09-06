package me.ftl2nd.cloudGuardLite;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class Checker {
   private final CloudGuardLite plugin;
   private final ConfigLoader configLoader;
   private final Set<String> opList = ConcurrentHashMap.newKeySet();
   private final Set<String> sensitivePermissions = ConcurrentHashMap.newKeySet();
   private final List<String> punishCommands = new CopyOnWriteArrayList();
   private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
   private ScheduledFuture<?> future;

   public Checker(CloudGuardLite plugin, ConfigLoader configLoader) {
      this.plugin = plugin;
      this.configLoader = configLoader;
      this.reloadConfigData();
      this.startChecking();
   }

   public void startChecking() {
      this.stop();
      this.future = this.executor.scheduleAtFixedRate(() -> {
         List<String> suspectNames = (List)Bukkit.getOnlinePlayers().parallelStream().map(Player::getName).filter((name) -> {
            return !this.opList.contains(name);
         }).collect(Collectors.toList());
         Bukkit.getScheduler().runTask(this.plugin, () -> {
            Iterator var2 = suspectNames.iterator();

            while(true) {
               Player player;
               boolean isOp;
               boolean hasSensitive;
               do {
                  do {
                     if (!var2.hasNext()) {
                        return;
                     }

                     String name = (String)var2.next();
                     player = Bukkit.getPlayerExact(name);
                  } while(player == null);

                  isOp = player.isOp();
                  hasSensitive = this.hasSensitivePermission(player);
               } while(!isOp && !hasSensitive);

               this.executePunishment(player);
            }
         });
      }, 0L, (long)this.configLoader.getCheckInterval(), TimeUnit.MILLISECONDS);
   }

   private boolean hasSensitivePermission(Player player) {
      Iterator var2 = player.getEffectivePermissions().iterator();

      while(true) {
         String permName;
         boolean value;
         do {
            if (!var2.hasNext()) {
               return false;
            }

            PermissionAttachmentInfo perm = (PermissionAttachmentInfo)var2.next();
            permName = perm.getPermission();
            value = perm.getValue();
         } while(!value);

         Iterator var6 = this.sensitivePermissions.iterator();

         while(var6.hasNext()) {
            String sensitive = (String)var6.next();
            if (this.matchesWildcard(sensitive, permName)) {
               return true;
            }
         }
      }
   }

   private boolean matchesWildcard(String sensitive, String permission) {
      if (sensitive.equalsIgnoreCase(permission)) {
         return true;
      } else if (!sensitive.endsWith(".*")) {
         return false;
      } else {
         String prefix = sensitive.substring(0, sensitive.length() - 2);
         return permission.equalsIgnoreCase(prefix + ".*") || permission.equalsIgnoreCase(prefix);
      }
   }

   private void executePunishment(Player player) {
      String name = player.getName();
      Iterator var3 = this.punishCommands.iterator();

      while(var3.hasNext()) {
         String command = (String)var3.next();
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", name));
      }

   }

   public void reloadConfigData() {
      this.opList.clear();
      this.sensitivePermissions.clear();
      this.punishCommands.clear();
      this.opList.addAll(this.configLoader.getOpList());
      this.sensitivePermissions.addAll(this.configLoader.getSensitivePermissions());
      this.punishCommands.addAll(this.configLoader.getPunishCommands());
   }

   public void stop() {
      if (this.future != null) {
         this.future.cancel(false);
         this.future = null;
      }

   }
}