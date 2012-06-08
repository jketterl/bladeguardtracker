package net.djmacgyver.bgt.socket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.keepalive.KeepAliveTarget;
import net.djmacgyver.bgt.keepalive.KeepAliveThread;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;

public class HttpSocketConnection implements KeepAliveTarget {
	public static final int STATE_DISCONNECTED = 0;
	public static final int STATE_CONNECTING = 1;
	public static final int STATE_CONNECTED = 2;
	public static final int STATE_DISCONNECTING = 3;
	
	private Context context;
	private WebSocketClient socket;
	private boolean connected = false;
	private int requestCount = 0;
	private HashMap <Integer, SocketCommand> requests = new HashMap<Integer, SocketCommand>();
	private LinkedList<SocketCommand> queue;
	private ArrayList<HttpSocketListener> listeners = new ArrayList<HttpSocketListener>();
	private ArrayList<String> subscribed = new ArrayList<String>();
	
	private int state = STATE_DISCONNECTED;
	
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
							} else if (response.has("event") && response.getString("event").equals("update")) {
								if (response.has("data")) {
									sendUpdate(response.getJSONObject("data"));
								} else {
									System.out.println("received message without data!");
								}
							} else if (response.has("command")) {
								JSONObject data = response.has("data") ? response.getJSONObject("data") : new JSONObject();
								sendCommand(response.getString("command"), data);
							}
						} catch (JSONException e) {
							// propably an old XML message. ignore... for now we only support json
							System.out.println("unable to parse message: " + message);
						}
						checkDisconnect();
					}
					
					@Override
					public void onError(Exception error) {
						error.printStackTrace();
						reconnect();
					}
					
					private void reconnect(){
						// put up a new queue
						queue = new LinkedList<SocketCommand>();
						// try disconnecting the old socket (if not already disconnected)
						try {
							socket.disconnect();
						} catch (Exception e) {}
						socket = null;
						// give the server 10s grace time
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {}
						getSocket().connect();
					}
					
					@Override
					public void onDisconnect(int code, String reason) {
						setState(STATE_DISCONNECTED);
						
						// we got disconnected.
						System.out.println("disconnected");
						
						// connected is already false - why were we connected in the first place? WTF?
						if (!connected) return;
						connected = false;
						
						// the disconnection was intentional - ok. accept it and leave it that way.
						if (doDisconnect) {
							doDisconnect = false;
							return;
						}
						
						// otherwise: try to reconnect
						reconnect();
					}
					
					@Override
					public void onConnect() {
						System.out.println("connected");
						connected = true;
						
						// first things first: send our authentication.
						authenticate();
						
						// send all queued commands
						LinkedList<SocketCommand> queue = HttpSocketConnection.this.queue;
						HttpSocketConnection.this.queue = null;
						while (!queue.isEmpty()) sendCommand(queue.poll());
						
						sendSubscriptions();
						
						setState(STATE_CONNECTED);
						
						// check whether this connection is eligible for disconnection
						checkDisconnect();
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
	
	public SocketCommand authenticate() {
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
		if (p.getBoolean("anonymous", true)) return null;
		try {
			// send our authentication data as soon as we are connected
			JSONObject data = new JSONObject();
			data.put("user", p.getString("username", ""));
			data.put("pass", p.getString("password", ""));
			SocketCommand command = new SocketCommand("auth", data);
			return sendCommand(command);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public SocketCommand sendCommand(String command) {
		return sendCommand(new SocketCommand(command));
	}
	
	public SocketCommand sendCommand(SocketCommand command) {
		if (!connected) {
			queue.add(command);
			return command;
		}
		requests.put(requestCount, command);
		command.setRequestId(requestCount++);
		getSocket().send(command.getJson());
		return command;
	}

	public void connect() {
		setState(STATE_CONNECTING);
		if (queue != null) return;
		queue = new LinkedList<SocketCommand>();
		getSocket().connect();
	}

	public void disconnect() {
		setState(STATE_DISCONNECTING);
		doDisconnect = true;
		checkDisconnect();
	}
	
	private boolean doDisconnect = false;
	
	private void checkDisconnect() {
		if (!connected || !doDisconnect || queue != null || !requests.isEmpty()) return;
		try {
			getSocket().disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public SocketCommand sendLocation(Location location) {
		try {
			// build a json object to send to the server
			JSONObject data = new JSONObject();
			data.put("lat", location.getLatitude());
			data.put("lon", location.getLongitude());
			if (location.hasSpeed()) data.put("speed", location.getSpeed());
			// send it
			return sendCommand(new SocketCommand("log", data));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void sendQuit() {
		// it is possible that the connection has been closed before we were able so send the "quit" message.
		try {
			sendCommand("quit");
		} catch (NullPointerException e) {}
	}

	public HttpSocketConnection subscribeUpdates(String category) {
		if (subscribed.contains(category)) return this;
		try {
			if (connected) {
				JSONObject data = new JSONObject();
				data.put("category", category);
				sendCommand(new SocketCommand("subscribeUpdates", data));
			}
			subscribed.add(category);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public HttpSocketConnection unSubscribeUpdates(String category) {
		if (!subscribed.contains(category)) return this;
		try {
			if (connected) {
				JSONObject data = new JSONObject();
				data.put("category", category);
				sendCommand(new SocketCommand("unSubscribeUpdates", data));
			}
			subscribed.remove(category);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	protected void sendUpdate(JSONObject update) {
		Iterator<HttpSocketListener> i = listeners.iterator();
		while (i.hasNext()) {
			i.next().receiveUpdate(update);
		}
	}
	
	protected void sendCommand(String command, JSONObject data) {
		Iterator<HttpSocketListener> i = listeners.iterator();
		while (i.hasNext()) {
			i.next().receiveCommand(command, data);
		}
	}
	
	protected void fireStateChange(int newState) {
		Iterator<HttpSocketListener> i = listeners.iterator();
		while (i.hasNext()) i.next().receiveStateChange(newState);
	}
	
	public void addListener(HttpSocketListener listener)
	{
		if (listeners.contains(listener)) return;
		listeners.add(listener);
	}
	
	public void removeListener(HttpSocketListener listener)
	{
		if (!listeners.contains(listener)) return;
		listeners.remove(listener);
	}

	private void sendSubscriptions() {
		// re-subscribe to all categories (in case of a re-connect)
		Iterator<String> i = subscribed.iterator();
		while (i.hasNext()) {
			try {
				JSONObject data = new JSONObject();
				data.put("category", i.next());
				sendCommand(new SocketCommand("subscribeUpdates", data));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void keepAlive(KeepAliveThread source) {
	}

	public void sendGpsUnavailable() {
		sendCommand("gpsUnavailable");
	}
	
	private void setState(int newState)	{
		state = newState;
		fireStateChange(newState);
	}
	
	public int getState() {
		return state;
	}
}