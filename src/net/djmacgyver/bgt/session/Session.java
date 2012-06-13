package net.djmacgyver.bgt.session;

import net.djmacgyver.bgt.user.User;

public class Session {
	private static User user;
	
	public static void setUser(User user) {
		Session.user = user;
	}
	
	public static User getUser() {
		return user;
	}
}
