package net.z4te.chatencrypt;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;

public final class Main extends JavaPlugin implements Listener {

    private SecretKey secretKey;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
        secretKey = generateSecretKey();
        getLogger().info("");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String message = event.getMessage();

        String encryptedMessage = encryptAES(message);

        String formattedMessage = String.format("<%s> %s");

        for(Player recipient : Bukkit.getOnlinePlayers()) {
            if (recipient.hasPermission("encrypt.use")) {
                String decryptedMessage = decryptAES(encryptedMessage);
                recipient.sendMessage(sender.getName() + ":" + decryptedMessage);
            } else {
                recipient.sendMessage(sender.getName() + ":" + encryptedMessage);
            }
        }
        event.setCancelled(true);
    }

    private SecretKey generateSecretKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            return keyGen.generateKey();
        } catch (Exception e) {
            getLogger().severe("Failed to generate secret key");
            return null;
        }
    }

    // AES暗号化
    private String encryptAES(String input) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(input.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            getLogger().severe("Encryption failed");
            return null;
        }
    }

        // AES復号
    private String decryptAES(String input) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(input);
            return new String(cipher.doFinal(decodedBytes));
        } catch (Exception e) {
            getLogger().severe("Decryption failed");
            return null;
        }
    }
}
