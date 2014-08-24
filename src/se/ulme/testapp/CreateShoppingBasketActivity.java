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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CreateShoppingBasketActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_one_product);

		GetProduct myTask = new GetProduct();

		myTask.execute();

		Button createButton = (Button) findViewById(R.id.insert_one_product_shoppingbasket);

		createButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				CreateProductTask createProductTask = new CreateProductTask();

				createProductTask.execute();
			}
		});
	}

	class GetProduct extends AsyncTask<Void, Void, JSONArray> {

		@Override
		protected JSONArray doInBackground(Void... params) {
			String myResponse = null;

			try {
				myResponse = new DefaultHttpClient()
				.execute(
						new HttpGet("http://" + ip + ":9000/products/" + CreateShoppingBasketActivity.this.getIntent().getStringExtra("productId")),
						new BasicResponseHandler()
						);

				return new JSONArray(myResponse);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void onPostExecute(JSONArray result) {
			ListView listView = (ListView) findViewById(R.id.list_one_product);

			listView.setAdapter(new ProductAdapter(result));
		}
	}

	class ProductAdapter extends BaseAdapter {
		private JSONArray products;

		public ProductAdapter(JSONArray products) {
			this.products = products;
		}

		@Override
		public int getCount() {
			return products.length();
		}

		@Override
		public Object getItem(int index) {
			try {
				return products.getJSONObject(index);
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public long getItemId(int index) {
			return index;
		}

		@Override
		public View getView(int index, View convertView, ViewGroup parent) {
			View productsListItem = getLayoutInflater().inflate(
					R.layout.create_shoppingbasket_form, parent, false);

			TextView productName = (TextView) productsListItem
					.findViewById(R.id.product_name_one_product_text);

			TextView description = (TextView) productsListItem
					.findViewById(R.id.description_one_product_text);

			TextView cost = (TextView) productsListItem
					.findViewById(R.id.cost_one_product_text);

			TextView rrp = (TextView) productsListItem
					.findViewById(R.id.rrp_one_product_text);

			try {
				JSONObject product = products.getJSONObject(index);

				productName.setText(" " + product.getString("productName"));
				description.setText(product.getString("description"));
				cost.setText(" " + product.getString("cost"));
				rrp.setText(" " + product.getString("rrp"));
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}

			return productsListItem;
		}
	}

	class CreateProductTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			try {
				HttpPost post = new HttpPost("http://" + ip + ":9000/insert-shoppingbasket");

				DefaultHttpClient client = new DefaultHttpClient();

				cookies = client.getCookieStore().getCookies();

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

				EditText quantity = (EditText) findViewById(R.id.quantity_one_product_insert);

				nameValuePairs.add(new BasicNameValuePair("quantity", quantity.getText().toString()));
				nameValuePairs.add(new BasicNameValuePair("email", getUserData()));
				nameValuePairs.add(new BasicNameValuePair("productId", CreateShoppingBasketActivity.this.getIntent().getStringExtra("productId")));

				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				String response = client.execute(post, new BasicResponseHandler());

				return response;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if(getFlashOrSessionFromPlay().containsKey("check")) {
				Toast.makeText(CreateShoppingBasketActivity.this, "Success!", Toast.LENGTH_LONG).show();

				startActivity(new Intent(CreateShoppingBasketActivity.this, ListProductsActivity.class));
			}
		}

		public Map<String, String> getFlashOrSessionFromPlay() {
			Map<String, String> flashFields = null;

			for(Cookie c : cookies) {
				if("PLAY_FLASH".equals(c.getName())) {
					flashFields = getFlashFields(c);

					EditText quantity = (EditText) findViewById(R.id.quantity_one_product_insert);

					if(quantity.getText().toString().equals("")) {
						quantity.setError(flashFields.get("empty-quantity"));
					} else if(!Check.isNumeric(quantity.getText().toString())) {
						quantity.setError(flashFields.get("not-numeric-quantity"));
					} else if(flashFields.containsKey("product-already-used")) {
						Toast.makeText(CreateShoppingBasketActivity.this, flashFields.get("product-already-used"), Toast.LENGTH_LONG).show();
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
