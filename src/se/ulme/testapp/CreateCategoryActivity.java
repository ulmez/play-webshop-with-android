package se.ulme.testapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.ulme.play_webshop_with_android.R;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class CreateCategoryActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.create_category_form);

		new GetStaffsFromServer().execute();

		Button button = (Button) findViewById(R.id.create_category);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				CreateCategoryTask createCategoryTask = new CreateCategoryTask();

				createCategoryTask.execute();
			}
		});
	}

	class GetStaffsFromServer extends AsyncTask<Void, Void, JSONArray> {
		@Override
		protected JSONArray doInBackground(Void... params) {
			try {
				String response = new DefaultHttpClient().execute(new HttpGet(
						"http://" + ip + ":9000/staffs"),
						new BasicResponseHandler());
				return new JSONArray(response);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void onPostExecute(JSONArray result) {
			Spinner spinner = (Spinner) findViewById(R.id.staff_list);
			spinner.setAdapter(new StaffAdapter(result));
		}
	}

	class StaffAdapter extends BaseAdapter implements SpinnerAdapter {
		private JSONArray staffs;

		public StaffAdapter(JSONArray staffs) {
			this.staffs = staffs;
		}

		@Override
		public int getCount() {
			return staffs.length();
		}

		@Override
		public Object getItem(int index) {
			try {
				return staffs.getJSONObject(index);
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public long getItemId(int index) {
			try {
				return staffs.getJSONObject(index).getInt("staffId");
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public View getView(int index, View convertView, ViewGroup parent) {
			TextView textView = new TextView(getApplicationContext());
			try {
				JSONObject staff = staffs.getJSONObject(index);
				textView.setText(staff.getString("firstName") + " " + staff.getString("surName"));
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
			textView.setTextSize(18);
			return textView;
		}
	}

	class CreateCategoryTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			try {
				HttpPost post = new HttpPost("http://" + ip + ":9000/insert-category");

				DefaultHttpClient client = new DefaultHttpClient();

				cookies = client.getCookieStore().getCookies();

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

				EditText categoryName = (EditText) findViewById(R.id.category_name_text);
				Spinner staff = (Spinner) findViewById(R.id.staff_list);
				String staffId = Long.toString(staff.getSelectedItemId());

				nameValuePairs.add(new BasicNameValuePair("name", categoryName.getText().toString()));
				nameValuePairs.add(new BasicNameValuePair("staff", staffId));

				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				String response = client.execute(post, new BasicResponseHandler());

				return response;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			getFlashOrSessionFromPlay();
		}

		public Map<String, String> getFlashOrSessionFromPlay() {
			Map<String, String> flashFields = null;

			for(Cookie c : cookies) {
				if("PLAY_FLASH".equals(c.getName())){
					flashFields = getFlashFields(c);

					EditText categoryName = (EditText) findViewById(R.id.category_name_text);

					if(categoryName.getText().toString().equals("")) {
						categoryName.setError(flashFields.get("empty-name"));
					} else if (flashFields.containsKey("already-used-name")) {
						categoryName.setError(flashFields.get("already-used-name"));
					}

					if(flashFields.containsKey("check")) {
						categoryName.setText("");

						Toast.makeText(getApplicationContext(), "Success in creating new category!", Toast.LENGTH_LONG).show();
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
				}
			}

			return flashFields;
		}
	}

}
