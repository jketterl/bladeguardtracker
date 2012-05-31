package net.djmacgyver.bgt.upstream;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONException;
import org.json.JSONObject;

import com.codebutler.android_websockets.WebSocketClient;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.socket.SocketCommand;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.location.Location;
import android.preference.PreferenceManager;

public class HttpSocketConnection extends Connection {
	private Context context;
	private WebSocketClient socket;
	private boolean connected = false;
	
	public HttpSocketConnection(Context applicationContext) {
		this.context = applicationContext;
	}
	
	private WebSocketClient getSocket()
	{
		if (socket == null) {
			try {
				URI url = new URI(context.getResources().getString(R.string.websocket));
				socket = new WebSocketClient(url, new WebSocketClient.Handler() {
					@Override
					public void onMessage(byte[] data) {
						System.out.println("OMG! Binary data!");
					}
					
					@Override
					public void onMessage(String message) {
						System.out.println(message);
					}
					
					@Override
					public void onError(Exception error) {
						error.printStackTrace();
					}
					
					@Override
					public void onDisconnect(int code, String reason) {
						System.out.println("disconnected");
						connected = false;
					}
					
					@Override
					public void onConnect() {
						System.out.println("connected");
						connected = true;
						SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
						if (!p.getBoolean("anonymous", true)) try {
							// send our authentication data as soon as we are connected
							JSONObject data = new JSONObject();
							data.put("user", p.getString("username", ""));
							data.put("pass", p.getString("password", ""));
							sendCommand(new SocketCommand("auth", data));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, null);
				WebSocketClient.setTrustManagers(new TrustManager[]{
						new X509TrustManager() {
							@Override
							public X509Certificate[] getAcceptedIssuers() {
								// TODO Auto-generated method stub
								return null;
							}
							
							@Override
							public void checkServerTrusted(X509Certificate[] chain, String authType)
									throws CertificateException {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void checkClientTrusted(X509Certificate[] chain, String authType)
									throws CertificateException {
								// TODO Auto-generated method stub
								
							}
						}
				});
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return socket;
	}
	
	private void sendCommand(SocketCommand command) {
		getSocket().send(command.getJson());
	}

	@Override
	public void connect() {
		getSocket().connect();
		getGpsReminder().start();
	}

	@Override
	public void disconnect() {
		sendQuit();
		getGpsReminder().terminate();
		try {
			getSocket().disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void executeLocationSend(Location location) {
		if (!connected)	return;
		try {
			// build a json object to send to the server
			JSONObject data = new JSONObject();
			data.put("lat", location.getLatitude());
			data.put("lon", location.getLongitude());
			if (location.hasSpeed()) data.put("speed", location.getSpeed());
			// send it
			sendCommand(new SocketCommand("log", data));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void sendQuit() {
		if (!connected) return;
		sendCommand(new SocketCommand("quit"));
	}

}
