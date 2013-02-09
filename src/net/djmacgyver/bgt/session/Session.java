package net.djmacgyver.bgt.session;

import java.util.ArrayList;

import net.djmacgyver.bgt.user.User;

public class Session {
	private static User user;
	private static ArrayList<UserChangeListener> listeners = new ArrayList<UserChangeListener>();
	
	public static void setUser(User user) {
		Session.user = user;
		for (UserChangeListener u : listeners) {
			u.onUserChange(user);
		}
	}
	
	public static User getUser() {
		return user;
	}
	
	public static boolean hasUser() {
		return user != null;
	}
	
	public static void addUserChangeListener(UserChangeListener u) {
		listeners.add(u);
	}
	
	public static void removeUserChangeListener(UserChangeListener u) {
		listeners.remove(u);
	}
}
