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
import net.djmacgyver.bgt.event.Event;
import net.djmacgyver.bgt.session.Session;
import net.djmacgyver.bgt.socket.command.AuthenticationCommand;
import net.djmacgyver.bgt.socket.command.SubscribeUpdatesCommand;
import net.djmacgyver.bgt.socket.command.UnsubscribeUpdatesCommand;
import net.djmacgyver.bgt.user.User;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;

import com.codebutler.android_websockets.WebSocketClient;

public class HttpSocketConnection {
	public static final int STATE_DISCONNECTED = 0;
	public static final int STATE_CONNECTING = 1;
	public static final int STATE_CONNECTED = 2;
	public static final int STATE_DISCONNECTING = 3;
	
	private Context context;
	private WebSocketClient socket;
	private int requestCount = 0;
	private SparseArray<SocketCommand> requests = new SparseArray<SocketCommand>();
	private LinkedList<SocketCommand> queue;
	private ArrayList<HttpSocketListener> listeners = new ArrayList<HttpSocketListener>();
	//private ArrayList<String> subscribed = new ArrayList<String>();
	private HashMap <Event, ArrayList<String>> subscribed = new HashMap<Event, ArrayList<String>>();
	private SocketCommand authentication;
	
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
					private boolean valid = true;
					
					@Override
					public void onMessage(byte[] data) {
						if (!valid) return;
						System.out.println("OMG! Binary data!");
					}
					
					@Override
					public void onMessage(String message) {
						if (!valid) return;
						if (message.length() == 0) return;
						try {
							JSONObject response = new JSONObject(message);
							if (response.has("requestId")) {
								Integer id = response.getInt("requestId");
								SocketCommand request = requests.get(id);
								if (request != null) {
									request.updateResult(response);
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
						if (!valid) return;
												
						System.out.println("Error on websocket connection!");
						error.printStackTrace();
						
						// try disconnecting the current socket (if not already disconnected)
						if (queue == null) try {
							// if the disconnect succeeds it should call onDisconnect()
							socket.disconnect();
						} catch (Exception e) {}
						// we can never be sure whether onDisconnect() is called, so to be sure we call it at least 
						// once here.
						// onDisconnect() is protected against double execution using valid
						onDisconnect(-1, error.getMessage());
					}
					
					private void reconnect(){
						System.out.println("reconnect()");

						// put up a new queue
						// this also prevents new commands from being sent
						if (queue == null) queue = new LinkedList<SocketCommand>();
						
						// send state notification
						setState(STATE_CONNECTING);
						
						// give the server 10s grace time
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							System.out.println("interrupted!");
						}
						
						// we might have changed our mind while we were sleeping...
						if (doDisconnect) {
							doDisconnect = false;
							return;
						}
						
						// if we get to here, we really want that connection to be back up
						getSocket().connect();
					}
					
					@Override
					public void onDisconnect(int code, String reason) {
						if (!valid) return;
						
						// invalidate this handler 
						valid = false;
						
						// invalidate authentication (must be re-sent on the next connect)
						authentication = null;
						
						// send state updates
						setState(STATE_DISCONNECTED);
						socket = null;
						
						// we got disconnected.
						System.out.println("disconnected");

						// cancel all outstanding requests (they won't receive data on a closed socket anyway)
						cancelRequests();
						
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
						if (!valid) return;
						System.out.println("connected");
						
						synchronized (queue) {
							// first things first: send handshake & authentication.
							sendHandshake();
							authentication = authenticate();
							Runnable r = new Runnable() {
								@Override
								public void run() {
									// get the current queue
									LinkedList<SocketCommand> q = queue;
									queue = null;
									
									// send all queued commands
									while (!q.isEmpty()) sendCommand(q.poll());
								}
							};
							if (authentication != null) authentication.addCallback(r); else r.run();
						}
						
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
	
	protected void cancelRequests() {
		// outstanding requests will not be answered; update them as false
		for (int i = 0; i < requests.size(); i++) {
			SocketCommand c = requests.get(requests.keyAt(i));
			c.updateResult(false);
		}
		requests.clear();
	}
	
	public SocketCommand getAuthentication(){
		if (authentication == null) {
			Session.setUser(null);
			SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
			if (p.getBoolean("anonymous", true)) return null;
			// send our authentication data as soon as we are connected
			authentication = new AuthenticationCommand(p.getString("username", ""), p.getString("password", ""));
			authentication.addCallback(new Runnable() {
				@Override
				public void run() {
					if (authentication == null) return;
					System.out.println(authentication);
					if (authentication.wasSuccessful()) {
						try {
							Session.setUser(new User(authentication.getResponseData().getJSONObject(0)));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
		return authentication;
	}

	protected SocketCommand authenticate() {
		SocketCommand auth = getAuthentication();
		if (auth == null) return null;
		return sendCommand(auth, false, true);
	}
	
	private void sendHandshake() {
		try {
			JSONObject json = new JSONObject();
			JSONObject handshake = new JSONObject();
			handshake.put("platform", "android");
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			handshake.put("version", info.versionName);
			handshake.put("build", info.versionCode);
			json.put("handshake", handshake);
			getSocket().send(json.toString());
		} catch (JSONException e) {
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected SocketCommand sendCommand(SocketCommand command, boolean doQueue, boolean bypassQueue) {
		if (queue != null && !bypassQueue) {
			if (!doQueue) return command;
			synchronized (queue) {
				queue.add(command);
				return command;
			}
		}
		requests.put(requestCount, command);
		command.setRequestId(requestCount++);
		getSocket().send(command.getJson());
		return command;
	}
	
	public SocketCommand sendCommand(SocketCommand command, boolean doQueue) {
		return sendCommand(command, doQueue, false);
	}
	
	public SocketCommand sendCommand(SocketCommand command) {
		return sendCommand(command, true);
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
		if (socket == null | !doDisconnect || queue != null || requests.size() > 0) return;
		try {
			getSocket().disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ArrayList<String> getEventSubscriptions(Event event) {
		if (!subscribed.containsKey(event)) {
			ArrayList<String> list = new ArrayList<String>();
			subscribed.put(event, list);
			return list;
		}
		return subscribed.get(event);
	}
	
	public HttpSocketConnection subscribeUpdates(Event event, String[] categories) {
		ArrayList<String> eventSubscriptions = getEventSubscriptions(event);
		for (String cat : categories) {
			if (!eventSubscriptions.contains(cat)) eventSubscriptions.add(cat);
		}
		sendCommand(new SubscribeUpdatesCommand(event, categories));
		return this;
	}

	public HttpSocketConnection subscribeUpdates(Event event, String category) {
		return subscribeUpdates(event, new String[]{category});
	}
	
	public HttpSocketConnection unSubscribeUpdates(Event event, String[] categories) {
		ArrayList<String> eventSubscriptions = getEventSubscriptions(event);
		for (String cat : categories) {
			if (eventSubscriptions.contains(cat)) eventSubscriptions.remove(cat);
		}
		sendCommand(new UnsubscribeUpdatesCommand(event, categories));
		return this;
	}
	
	public HttpSocketConnection unSubscribeUpdates(Event event, String category) {
		return unSubscribeUpdates(event, new String[]{category});
	}
	
	protected void sendUpdate(JSONObject update) {
		synchronized (listeners) {
			Iterator<HttpSocketListener> i = listeners.iterator();
			while (i.hasNext()) i.next().receiveUpdate(update);
		}
	}
	
	private void sendCommand(String command, JSONObject data) {
		synchronized (listeners) {
			Iterator<HttpSocketListener> i = listeners.iterator();
			while (i.hasNext()) i.next().receiveCommand(command, data);
		}
	}
	
	protected void fireStateChange(int newState) {
		synchronized (listeners) {
			Iterator<HttpSocketListener> i = listeners.iterator();
			while (i.hasNext()) i.next().receiveStateChange(newState);
		}
	}
	
	public void addListener(HttpSocketListener listener)
	{
		synchronized (listeners) {
			if (listeners.contains(listener)) return;
			listeners.add(listener);
		}
	}
	
	public void removeListener(HttpSocketListener listener)
	{
		synchronized (listeners) {
			if (!listeners.contains(listener)) return;
			listeners.remove(listener);
		}
	}

	private void sendSubscriptions() {
		// re-subscribe to all categories (in case of a re-connect)
		Iterator<Event> i = subscribed.keySet().iterator();
		while (i.hasNext()) {
			Event event = i.next();
			ArrayList<String> eventSubscriptions = getEventSubscriptions(event);
			String[] sub = eventSubscriptions.toArray(new String[eventSubscriptions.size()]);
			sendCommand(new SubscribeUpdatesCommand(event, sub));
		}
	}
	
	private void setState(int newState)	{
		state = newState;
		fireStateChange(newState);
	}
	
	public int getState() {
		return state;
	}
}
