package net.djmacgyver.bgt.upstream;

import java.io.IOException;

import net.djmacgyver.bgt.Config;
import net.djmacgyver.bgt.http.HttpClient;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;

import android.content.Context;

public class HttpStreamingThread extends Thread {
	private Context context;
	private HttpClient client;
	private boolean terminate = false;
	private StreamingHttpEntity entity;
	private String userName;
	private String password;
	
	public HttpStreamingThread(Context context, String userName, String password) {
		this(context);
		this.userName = userName;
		this.password = password;
	}

	public HttpStreamingThread(Context context) {
		this.context = context;
	}

	private HttpClient getClient() {
		if (client == null) {
			client = new HttpClient(this.context);
		}
		return client;
	}
	
	private StreamingHttpEntity getEntity() {
		if (entity == null) {
			entity = new StreamingHttpEntity(this);
		}
		return entity;
	}
	
	@Override
	public void run() {
		while (!terminate) {
			try {
				HttpPost req = new HttpPost(Config.baseUrl + "log");
				req.setEntity(getEntity());
				if (this.userName != null && this.password != null) {
					getEntity().sendData("uid=" + userName + "&pass=" + password);
				}
				getClient().execute(req).getEntity().consumeContent();
				//entity = null;
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			entity = null;
		}
		getClient().getConnectionManager().shutdown();
	}
	
	public void sendData(String data) {
		getEntity().sendData(data);
	}
	
	public void sendData(byte[] data) {
		getEntity().sendData(data);
	}
	
	public void terminate() {
		terminate = true;
		getEntity().terminate();
		interrupt();
	}
}
