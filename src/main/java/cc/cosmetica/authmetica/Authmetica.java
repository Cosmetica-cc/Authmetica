package cc.cosmetica.authmetica;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class Authmetica extends JavaPlugin {
	private static FileConfiguration config;
	private static String password = "";
	private static Authmetica instance;
	private static boolean initializedCleanup = false;

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		config = this.getConfig();
		password = config.getString("cosmetica-password");
		instance = this;
		getServer().getPluginManager().registerEvents(new EventListener(), this);
		if (!initializedCleanup) {
			initializedCleanup = true;
			Thread cleanup = new Thread(() -> {
				while (true) {
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					CosmeticaApi.cleanup();
				}
			});
			cleanup.start();
		}
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		getLogger().info("Authmetica has stopped!");
	}

	public static String getPassword() {
		return password;
	}

	public static Authmetica getInstance() {
		return instance;
	}
}
