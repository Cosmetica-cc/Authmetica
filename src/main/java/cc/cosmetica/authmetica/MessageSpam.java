package cc.cosmetica.authmetica;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class MessageSpam {
	private static Map<String, Long> usersToSpam = new HashMap<>();


	public static void addUser(String uuid, UserData data) {
		if (usersToSpam.containsKey(uuid)) return;
		long timestamp = System.currentTimeMillis();
		usersToSpam.put(uuid, timestamp);
		Thread thread = new Thread(() -> {
			while (usersToSpam.containsKey(uuid) && usersToSpam.get(uuid) == timestamp) {
				Player player = Bukkit.getPlayer(UUID.fromString(uuid));
				if (player != null && player.isOnline()) {
					if (timestamp > System.currentTimeMillis() - 50000L) {
						//player.sendMessage("yeet " + CosmeticaApi.getUserData(uuid).token);

						BaseComponent[] component = new ComponentBuilder("§7\n§6§lCosmetica §8§l>§r ").append("Click here").color(ChatColor.GOLD).underlined(true).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://cosmetica.cc/manage?" + CosmeticaApi.getUserData(uuid).token)).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§c§lDon't click this link on stream!").create())).append("§7 to log into Cosmetica!\n§7").create();
						player.spigot().sendMessage(component);
					} else {
						removeUser(uuid, data);
					}
					try {
						for (int i = 0; i < 100; i++) {
							if (!usersToSpam.containsKey(uuid) || usersToSpam.get(uuid) != timestamp) {
								break;
							} else {
								Thread.sleep(50);
							}
						}
					} catch (InterruptedException e) {}
				} else {
					removeUser(uuid, data);
				}
			}
			Authmetica.getInstance().getLogger().info("User data was removed, stopping messages");
			removeUser(uuid, data);
		});
		thread.start();
	}

	public static void removeUser(String uuid, UserData data) {
		usersToSpam.remove(uuid, data);
		removeUserCompletely(uuid);

	}

	public static void removeUser(String uuid) {
		usersToSpam.remove(uuid);
		removeUserCompletely(uuid);
	}

	private static void removeUserCompletely(String uuid) {
		CosmeticaApi.kickPlayer(uuid, "§c§lYou took too long!\n\n§r§6Rejoin and click the link to log into Cosmetica");
	}
}
