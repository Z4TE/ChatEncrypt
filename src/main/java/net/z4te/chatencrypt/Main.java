package net.z4te.chatencrypt;

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
        getLogger().info("decryption.bypass");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String message = event.getMessage();

        // チャットを暗号化
        String encryptedMessage = encryptAES(message);

        String displaySender = String.format("<%s> ",sender.getName());

        // コンソールに平文を表示
        Bukkit.getConsoleSender().sendMessage(displaySender + encryptedMessage);

        for(Player recipient : Bukkit.getOnlinePlayers()) {
            // 送信者自身のチャットは平文を表示
            if (recipient == sender) {
                sender.sendMessage(displaySender + message );
            } else {
                // 権限がある場合は復号
                if (!recipient.hasPermission("encryption.bypass") || recipient.isOp()) {
                    String decryptedMessage = decryptAES(encryptedMessage);
                    recipient.sendMessage(displaySender + decryptedMessage);
                } else {
                    // encryption.bypassがある場合は暗号化したものを表示
                    recipient.sendMessage(displaySender + encryptedMessage);
                }
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
