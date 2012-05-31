package net.djmacgyver.bgt.socket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.upstream.Connection;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;

public class HttpSocketConnection extends Connection {
	private Context context;
	private WebSocketClient socket;
	private boolean connected = false;
	private int requestCount = 0;
	private HashMap <Integer, SocketCommand> requests = new HashMap<Integer, SocketCommand>();
	
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
						if (message.length() == 0) return;
						try {
							JSONObject response = new JSONObject(message);
							if (response.has("requestId")) {
								Integer id = response.getInt("requestId");
								if (requests.containsKey(id)) {
									((SocketCommand) requests.get(id)).updateResult(response);
									requests.remove(id);
								} else {
									Log.e("SocketConnection", "received response for unknown command id: " + id);
								}
							}
						} catch (JSONException e) {
							// propably an old XML message. ignore... for now we only support json
							//System.out.println("unable to parse message: " + message);
						}
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
						authenticate();
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
	
	public void authenticate() {
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
		if (!p.getBoolean("anonymous", true)) try {
			// send our authentication data as soon as we are connected
			JSONObject data = new JSONObject();
			data.put("user", p.getString("username", ""));
			data.put("pass", p.getString("password", ""));
			final SocketCommand command = new SocketCommand("auth", data);
			command.setCallback(new Runnable() {
				@Override
				public void run() {
					System.out.println("login successful? " + command.wasSuccessful());
				}
			});
			sendCommand(command);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void sendCommand(String command) {
		sendCommand(new SocketCommand(command));
	}
	
	private void sendCommand(SocketCommand command) {
		if (!connected) return;
		requests.put(requestCount, command);
		command.setRequestId(requestCount++);
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
		sendCommand("quit");
	}

}
