package cc.cosmetica.authmetica;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Authmetica.getInstance().getLogger().info("Player joined");
		CosmeticaApi.getTokenForUuid(e.getPlayer().getUniqueId().toString());
	}
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		MessageSpam.removeUser(e.getPlayer().getUniqueId().toString());
	}
}
