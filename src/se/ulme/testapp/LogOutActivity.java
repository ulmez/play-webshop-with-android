package se.ulme.testapp;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

public class LogOutActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		new LogOutTask().execute();
	}

	class LogOutTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			try {
				HttpPost post = new HttpPost("http://" + ip + ":9000/log-out");

				DefaultHttpClient client = new DefaultHttpClient();

				if (cookies != null) {
					for (Cookie cookie : cookies) {
						client.getCookieStore().addCookie(cookie);
					}
				}

				String response = client.execute(post,
						new BasicResponseHandler());

				cookies = client.getCookieStore().getCookies();

				return response;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(LogOutActivity.this, "Logged out", Toast.LENGTH_LONG).show();
		}
	}

}
