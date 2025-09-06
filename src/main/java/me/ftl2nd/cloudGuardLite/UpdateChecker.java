package me.ftl2nd.cloudGuardLite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class UpdateChecker {
   private static final String RESOURCE_URL = "https://api.spigotmc.org/legacy/update.php?resource=122418";
   private final String currentVersion;
   private final Logger logger;

   public UpdateChecker(String currentVersion, Logger logger) {
      this.currentVersion = currentVersion;
      this.logger = logger;
   }

   public void checkForUpdate() {
      CompletableFuture.supplyAsync(this::getLatestVersion).thenAccept((latestVersion) -> {
         if (latestVersion == null) {
            this.logger.warning("Không thể kiểm tra phiên bản mới. Vui lòng kiểm tra kết nối mạng hoặc thử lại sau.");
         } else {
            int comparison = this.compareVersions(this.currentVersion, latestVersion);
            if (comparison < 0) {
               this.logger.info(String.format("\u001b[33mĐã có phiên bản mới: %s (Bạn đang dùng: %s). Vui lòng cập nhật để có trải nghiệm tốt nhất!\u001b[0m", latestVersion, this.currentVersion));
            } else if (comparison > 0) {
               this.logger.info("\u001b[35mBạn đang sử dụng phiên bản thử nghiệm của CloudGuardLite!\u001b[0m");
            } else {
               this.logger.info("\u001b[32mBạn đang sử dụng phiên bản mới nhất của CloudGuardLite!\u001b[0m");
            }

         }
      });
   }

   private String getLatestVersion() {
      try {
         HttpURLConnection connection = (HttpURLConnection)(new URL("https://api.spigotmc.org/legacy/update.php?resource=122418")).openConnection();
         connection.setRequestMethod("GET");
         connection.setConnectTimeout(5000);
         connection.setReadTimeout(5000);
         BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

         String var3;
         try {
            var3 = reader.readLine();
         } catch (Throwable var6) {
            try {
               reader.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }

            throw var6;
         }

         reader.close();
         return var3;
      } catch (IOException var7) {
         this.logger.warning("Lỗi khi kiểm tra cập nhật: " + var7.getMessage());
         return null;
      }
   }

   private int compareVersions(String v1, String v2) {
      String[] parts1 = v1.split("\\.");
      String[] parts2 = v2.split("\\.");
      int length = Math.max(parts1.length, parts2.length);

      for(int i = 0; i < length; ++i) {
         int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
         int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
         if (num1 < num2) {
            return -1;
         }

         if (num1 > num2) {
            return 1;
         }
      }

      return 0;
   }
}