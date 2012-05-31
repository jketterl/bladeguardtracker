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

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.location.Location;

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

	@Override
	public void connect() {
		getSocket().connect();
		getGpsReminder().start();
	}

	@Override
	public void disconnect() {
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
		System.out.println("about to send a location update");
		if (!connected) {
			System.out.println("skipping location update, not connected!");
			return;
		}
		try {
			JSONObject obj = new JSONObject();
			obj.put("command", "log");
			JSONObject data = new JSONObject();
			data.put("lat", location.getLatitude());
			data.put("lon", location.getLongitude());
			if (location.hasSpeed()) data.put("speed", location.getSpeed());
			obj.put("data", data);
			getSocket().send(obj.toString());
			System.out.println(obj);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void sendQuit() {
	}

}
