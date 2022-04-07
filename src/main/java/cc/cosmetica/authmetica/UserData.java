package cc.cosmetica.authmetica;

public class UserData {
	public String token;
	public long timestamp;
	public UserData(String token, long timestamp) {
		this.token = token;
		this.timestamp = timestamp;
	}
}
