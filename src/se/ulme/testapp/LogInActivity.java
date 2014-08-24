package se.ulme.testapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import se.ulme.play_webshop_with_android.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LogInActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);

		Button loginButton = (Button) findViewById(R.id.login_user);

		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				new LogInTask().execute();
			}
		});
	}

	class LogInTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			try{
				HttpPost post = new HttpPost("http://" + ip + ":9000/loggin-authentication");

				DefaultHttpClient client = new DefaultHttpClient();

				cookies = client.getCookieStore().getCookies();

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

				EditText user_name_text = (EditText) findViewById(R.id.username_text);
				EditText pass_text = (EditText) findViewById(R.id.password_text);

				nameValuePairs.add(new BasicNameValuePair("username", user_name_text.getText().toString()));
				nameValuePairs.add(new BasicNameValuePair("password", pass_text.getText().toString()));

				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				String response = client.execute(post, new BasicResponseHandler());

				return response;
			} catch(Exception e){
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			Map<String, String> flashFields = getFlashOrSessionFromPlay();
			String user = "";
			String pass = "";

			SharedPreferences pref = getApplicationContext().getSharedPreferences("userPref", 0);
			Editor editor = pref.edit();

			for(Map.Entry<String, String> me : flashFields.entrySet()) {
				if(me.getKey().equals("username")) {
					editor.putString(me.getKey(), me.getValue()); // Storing string
				}

				if(me.getKey().equals("user")) {
					user = me.getValue();
				}

				if(me.getKey().equals("pass")) {
					pass = me.getValue();
				}
			}

			editor.commit();

			if(getUserData() != null) {
				startActivity(new Intent(LogInActivity.this, StartActivity.class));
			} else {
				if(!user.equals("No username") && !pass.equals("No password")) {
					Toast.makeText(LogInActivity.this, "That's not a valid user", Toast.LENGTH_LONG).show();
				}
			}
		}

		public Map<String, String> getFlashOrSessionFromPlay() {
			Map<String, String> flashFields = null;

			for(Cookie c : cookies) {
				if("PLAY_FLASH".equals(c.getName())){
					flashFields = getFlashFields(c);

					EditText user_name_text = (EditText) findViewById(R.id.username_text);
					EditText pass_text = (EditText) findViewById(R.id.password_text); 

					if(user_name_text.getText().toString().equals("")) {
						user_name_text.setError(flashFields.get("user"));
					}

					if(pass_text.getText().toString().equals("")) {
						pass_text.setError(flashFields.get("pass"));
					}
				} else if("PLAY_SESSION".equals(c.getName())) {
					flashFields = getFlashFields(c);
					String key = "";
					String value = "";
					for(Map.Entry<String, String> es : flashFields.entrySet()) {
						key = es.getKey().substring(es.getKey().length() - 8, es.getKey().length());
						value = es.getValue();
					}

					flashFields.clear();
					flashFields.put(key, value);

					break;
				}
			}

			return flashFields;
		}
	}

}
