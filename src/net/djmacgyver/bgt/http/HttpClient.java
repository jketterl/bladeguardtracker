package net.djmacgyver.bgt.http;

import net.djmacgyver.bgt.R;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

public class HttpClient extends DefaultHttpClient {
	private Context context;
	
	public HttpClient(Context context)	{
		super();
		this.context = context;
	}

	@Override
	protected ClientConnectionManager createClientConnectionManager() {
		SchemeRegistry schreg = new SchemeRegistry();
		schreg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		
		final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
        sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        schreg.register(new Scheme("https", sslSocketFactory, 443));
        
        return new ThreadSafeClientConnManager(this.createHttpParams(), schreg);
	}

	@Override
	protected HttpParams createHttpParams() {
		HttpParams params = new BasicHttpParams();
		try {
			HttpProtocolParams.setUserAgent(
					params,
					context.getString(R.string.app_name) +
					"/" +
					context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName +
					"(Android/" +
					android.os.Build.DEVICE + " " + android.os.Build.VERSION.RELEASE + 
					")"
			);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return params;
	}
}
