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
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

public class CreateProductActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.create_product_form);

		new GetCategoriesFromServer().execute();

		Button createButton = (Button) findViewById(R.id.create_product);

		createButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				ListView category = (ListView) findViewById(R.id.categories_list);
				List<View> rawCheckBoxes = category.getTouchables();
				List<CheckBox> checkBoxes = new ArrayList<>();

				for(int i = 0; i < rawCheckBoxes.size(); i++) {
					if(rawCheckBoxes.get(i) instanceof CheckBox) {
						CheckBox cb = (CheckBox) rawCheckBoxes.get(i);
						if(cb.isChecked()) {
							checkBoxes.add(cb);
						}
					}
				}

				CreateProductTask createProductTask = new CreateProductTask();

				List<String> categories = new ArrayList<>();

				for(CheckBox cb : checkBoxes) {
					categories.add(Integer.toString(cb.getId()));
				}

				createProductTask.setCategories(categories);
				createProductTask.execute();
			}
		});
	}

	class GetCategoriesFromServer extends AsyncTask<Void, Void, JSONArray> {

		@Override
		protected JSONArray doInBackground(Void... params) {
			try {
				String response = new DefaultHttpClient().execute(new HttpGet(
						"http://" + ip + ":9000/categories"),
						new BasicResponseHandler());
				return new JSONArray(response);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void onPostExecute(JSONArray result) {
			ListView spinner = (ListView) findViewById(R.id.categories_list);
			spinner.setAdapter(new CategoryAdapter(result));
		}
	}

	class CategoryAdapter extends BaseAdapter implements SpinnerAdapter {
		private JSONArray categories;

		public CategoryAdapter(JSONArray categories) {
			this.categories = categories;
		}

		@Override
		public int getCount() {
			return categories.length();
		}

		@Override
		public Object getItem(int index) {
			try {
				return categories.getJSONObject(index);
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public long getItemId(int index) {
			try {
				return categories.getJSONObject(index).getInt("id");
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public View getView(int index, View convertView, ViewGroup parent) {
			View categoriesListItem = getLayoutInflater().inflate(
					R.layout.categories_list_item, parent, false);

			CheckBox categoryName = (CheckBox) categoriesListItem
					.findViewById(R.id.checkbox_category);

			try {
				JSONObject category = categories.getJSONObject(index);
				categoryName.setId(Integer.parseInt(category.getString("categoryId")));
				categoryName.setText(category.getString("categoryName"));
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}

			return categoriesListItem;
		}
	}

	class CreateProductTask extends AsyncTask<Void, Void, String> {
		private List<String> categories;

		public void setCategories(List<String> categories) {
			this.categories = categories;
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				HttpPost post = new HttpPost("http://" + ip + ":9000/insert-product");

				DefaultHttpClient client = new DefaultHttpClient();

				cookies = client.getCookieStore().getCookies();

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

				EditText productName = (EditText) findViewById(R.id.product_name_text);
				EditText description = (EditText) findViewById(R.id.description_text);
				EditText cost = (EditText) findViewById(R.id.cost_text);
				EditText rrp = (EditText) findViewById(R.id.rrp_text);

				nameValuePairs.add(new BasicNameValuePair("name", productName.getText().toString()));
				nameValuePairs.add(new BasicNameValuePair("description", description.getText().toString()));
				nameValuePairs.add(new BasicNameValuePair("cost", cost.getText().toString()));
				nameValuePairs.add(new BasicNameValuePair("rrp", rrp.getText().toString()));

				for (String c : categories) {
					nameValuePairs.add(new BasicNameValuePair("category", c));
				}

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

					EditText productName = (EditText) findViewById(R.id.product_name_text);
					EditText description = (EditText) findViewById(R.id.description_text);
					EditText cost = (EditText) findViewById(R.id.cost_text);
					EditText rrp = (EditText) findViewById(R.id.rrp_text);

					if(productName.getText().toString().equals("")) {
						productName.setError(flashFields.get("empty-name"));
					}

					if(description.getText().toString().equals("")) {
						description.setError(flashFields.get("empty-description"));
					}

					if(cost.getText().toString().equals("")) {
						cost.setError(flashFields.get("empty-cost"));
					} else if(!Check.isNumeric(cost.getText().toString())) {
						cost.setError(flashFields.get("no-integer-cost"));
					}

					if(rrp.getText().toString().equals("")) {
						rrp.setError(flashFields.get("empty-rrp"));
					} else if(!Check.isNumeric(rrp.getText().toString())) {
						rrp.setError(flashFields.get("no-integer-rrp"));
					}

					ListView category = (ListView) findViewById(R.id.categories_list);
					List<View> rawCheckBoxes = category.getTouchables();
					List<CheckBox> checkBoxes = new ArrayList<>();

					for(int i = 0; i < rawCheckBoxes.size(); i++) {
						if(rawCheckBoxes.get(i) instanceof CheckBox) {
							CheckBox cb = (CheckBox) rawCheckBoxes.get(i);
							if(cb.isChecked()) {
								checkBoxes.add(cb);
							}
						}
					}

					if(flashFields.containsKey("empty-category")) {
						Toast.makeText(CreateProductActivity.this, flashFields.get("empty-category"), Toast.LENGTH_LONG).show();
					}

					if(flashFields.containsKey("check")) {
						productName.setText("");
						description.setText("");
						cost.setText("");
						rrp.setText("");

						for(CheckBox cb : checkBoxes) {
							cb.setChecked(false);
						}

						Toast.makeText(getApplicationContext(), "Success in creating new product!", Toast.LENGTH_LONG).show();
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
