package ir.limoo.driver.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

	@JsonProperty("id")
	private String id;

	@JsonProperty("username")
	private String username;

	@JsonProperty("nickname")
	private String nickname;

	@JsonProperty("first_name")
	private String firstName;

	@JsonProperty("last_name")
	private String lastName;

	@JsonProperty("avatar_hash")
	private String avatarHash;

	@JsonProperty("is_bot")
	private Boolean isBot;

	public String getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getNickname() {
		return nickname;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getAvatarHash() {
		return avatarHash;
	}

	public Boolean getBot() {
		return isBot;
	}

	public String getDisplayName() {
		if (nickname != null && !nickname.isEmpty())
			return nickname;
		String displayName = "";
		if (firstName != null && !firstName.isEmpty())
			displayName += firstName;
		if (lastName != null && !lastName.isEmpty())
			displayName += lastName;
		if (!displayName.isEmpty())
			return displayName.trim();
		return username;
	}
}
