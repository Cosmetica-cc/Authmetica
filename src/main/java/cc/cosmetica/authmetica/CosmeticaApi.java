package cc.cosmetica.authmetica;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.yggdrasil.response.User;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CosmeticaApi {
	private static Map<String, Long> lookingUpUsers = new HashMap<>();
	private static Map<String, UserData> userTokenCache = new HashMap<>();
	private static final JsonParser PARSER = new JsonParser();


	public static void cleanup() {
		for (String uuid : userTokenCache.keySet()) {
			if (userTokenCache.get(uuid).timestamp < System.currentTimeMillis() - 7200L * 1000L)
				userTokenCache.remove(uuid);
		}
	}

	public static void getTokenForUuid(String uuid) {
		if (lookingUpUsers.containsKey(uuid)) return;
		if (userTokenCache.containsKey(uuid) && userTokenCache.get(uuid).timestamp > System.currentTimeMillis() - 7130L * 1000L) {
			MessageSpam.addUser(uuid, userTokenCache.get(uuid));
			return;
		}
		String password = Authmetica.getPassword();
		if (password.isEmpty()) return;
		Thread thread = new Thread(() -> {
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				final HttpGet httpGet = new HttpGet("https://api.cosmetica.cc/client/getauthtoken?password=" + password + "&uuid=" + uuid);
				try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
					String authToken = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
					try (CloseableHttpClient httpClient2 = HttpClients.createDefault()) {
						final HttpGet httpGet2 = new HttpGet("https://api.cosmetica.cc/client/verifyforauthtokens?token=" + authToken + "&uuid=" + uuid);
						try (CloseableHttpResponse response2 = httpClient2.execute(httpGet2)) {
							JsonObject jsonObject = PARSER.parse(EntityUtils.toString(response2.getEntity(), StandardCharsets.UTF_8).trim()).getAsJsonObject();
							if (!jsonObject.has("master_token")) throw new Exception("nice");
							String masterToken = jsonObject.get("master_token").getAsString();
							UserData data = new UserData(masterToken, System.currentTimeMillis());
							userTokenCache.put(uuid, data);
							lookingUpUsers.remove(uuid);
							MessageSpam.addUser(uuid, data);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				lookingUpUsers.remove(uuid);
				kickPlayer(uuid, "error");
			}
		});
		thread.start();
	}

	public static void removeUserFromCache(String uuid) {
		userTokenCache.remove(uuid);
	}

	public static UserData getUserData(String uuid) {
		return userTokenCache.getOrDefault(uuid, null);
	}

	public static void kickPlayer(String uuid, String message) {
		Bukkit.getScheduler().runTask(Authmetica.getInstance(), () -> {
			Player player = Bukkit.getPlayer(UUID.fromString(uuid));
			if (player != null && player.isOnline()) {
				player.kickPlayer(message);
			}
		});
	}
}
