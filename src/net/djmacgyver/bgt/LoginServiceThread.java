package net.djmacgyver.bgt;

import java.io.IOException;

import net.djmacgyver.bgt.http.HttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import android.content.Context;

public class LoginServiceThread extends Thread {
	// 6h timeout... this should be enough ;)
	private static final int TIMEOUT = 518400000;
	
	private HttpClient client;
	private Context context;
	private boolean terminate = false;
	private String user;
	private String pass;
	private boolean invalidated = true;
	
	public LoginServiceThread(Context context, String user, String pass) {
		this.context = context;
		this.user = user;
		this.pass = pass;
	}
	
	public void setCredentials(String user, String pass) {
		this.user = user;
		this.pass = pass;
		invalidated = true;
		this.interrupt();
	}
	
	@Override
	public void run() {
		while (!terminate) try {
			int nextTimeout = performLogin();
			Thread.sleep(nextTimeout);
		} catch (InterruptedException e) {}
		getClient().getConnectionManager().shutdown();
	}
	
	private HttpClient getClient() {
		if (client == null) {
			client = new HttpClient(context);
		}
		return client;
	}
	
	private int performLogin() {
		try {
			int status = -1;
			if (!invalidated) {
				HttpGet req = new HttpGet(context.getResources().getString(R.string.base_url) + "login");
				HttpResponse res = getClient().execute(req);
				status = res.getStatusLine().getStatusCode();			
				res.getEntity().consumeContent();
			}
			
			if (status != 200) {
				HttpPost post = new HttpPost(context.getResources().getString(R.string.base_url) + "login");
				post.setEntity(new StringEntity("user=" + user + "&pass=" + pass));
				HttpResponse res = getClient().execute(post);
				if (res.getStatusLine().getStatusCode() == 403) {
					// login failed!
					terminate();
				} else {
					invalidated = false;
				}
				res.getEntity().consumeContent();
			}
			
			return TIMEOUT;
		} catch (IOException e) {
			return 60000;
		}
	}
	
	public void terminate() {
		this.terminate = true;
	}
}
