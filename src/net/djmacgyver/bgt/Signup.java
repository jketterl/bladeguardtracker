package net.djmacgyver.bgt;

import net.djmacgyver.bgt.http.HttpClient;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.Window;
import android.widget.TextView;

public class Signup extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        TextView t = (TextView) findViewById(R.id.title);
        t.setText(R.string.new_user);
        addPreferencesFromResource(R.xml.signup);

        Preference signup = findPreference("signup");

		final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
        signup.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (p.getString("username", "").equals("")) return false;
				if (p.getString("password", "").equals("")) return false;
				if (!p.getString("password", "a").equals(p.getString("password_confirm", "b"))) return false;
				HttpClient c = new HttpClient(getApplicationContext());
				HttpPost req = new HttpPost(Config.baseUrl + "signup");
				try {
					req.setEntity(new StringEntity("user=" + p.getString("username", null) + "&pass=" + p.getString("password", "")));
					c.execute(req);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}
		});
	}
}
