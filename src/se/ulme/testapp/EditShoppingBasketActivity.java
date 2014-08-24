package se.ulme.testapp;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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

public class EditShoppingBasketActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_oneline_shoppingbasket);

		GetProducts myTask = new GetProducts();

		myTask.execute();

		Button editProductQuantity = (Button) findViewById(R.id.edit_quantity_button);

		editProductQuantity.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				CreateProductTask createProductTask = new CreateProductTask();

				createProductTask.execute();
			}
		});
	}

	class GetProducts extends AsyncTask<Void, Void, JSONArray> {

		@Override
		protected JSONArray doInBackground(Void... params) {
			String myResponse = null;
			try {
				myResponse = new DefaultHttpClient()
				.execute(
						new HttpGet("http://" + ip + ":9000/get-oneline-shoppingbasket/" + EditShoppingBasketActivity.this.getIntent().getStringExtra("shoppingBasketId")),
						new BasicResponseHandler()
						);

				return new JSONArray(myResponse);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void onPostExecute(JSONArray result) {
			ListView listView = (ListView) findViewById(R.id.list_oneline_product);

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
					R.layout.edit_oneline_shoppingbasket_form, parent, false);

			TextView productName = (TextView) productsListItem
					.findViewById(R.id.edit_product_name_text);

			TextView description = (TextView) productsListItem
					.findViewById(R.id.edit_description_text);

			TextView cost = (TextView) productsListItem
					.findViewById(R.id.edit_cost_text);

			TextView rrp = (TextView) productsListItem
					.findViewById(R.id.edit_rrp_text);

			try {
				JSONObject product = products.getJSONObject(index);

				productName.setText(" " + product.getJSONObject("product").getString("productName"));
				description.setText(" " + product.getJSONObject("product").getString("description"));
				cost.setText(" " + product.getJSONObject("product").getString("cost"));
				rrp.setText(" " + product.getJSONObject("product").getString("rrp"));
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
				HttpPost post = new HttpPost("http://" + ip + ":9000/edit-oneline-shoppingbasket");

				DefaultHttpClient client = new DefaultHttpClient();

				cookies = client.getCookieStore().getCookies();

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

				EditText quantity = (EditText) findViewById(R.id.list_edit_oneline_quantity_text);

				nameValuePairs.add(new BasicNameValuePair("edit-quantity", quantity.getText().toString()));
				nameValuePairs.add(new BasicNameValuePair("shoppingbasket-id", EditShoppingBasketActivity.this.getIntent().getStringExtra("shoppingBasketId")));

				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				String response = client.execute(post, new BasicResponseHandler());

				return response;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(EditShoppingBasketActivity.this, "Now the quantity is changed!", Toast.LENGTH_LONG).show();

			startActivity(new Intent(EditShoppingBasketActivity.this, CreateOrderActivity.class));
		}
	}

}
