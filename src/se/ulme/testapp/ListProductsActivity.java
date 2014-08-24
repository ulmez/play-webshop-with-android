package se.ulme.testapp;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
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
import android.widget.ListView;
import android.widget.TextView;

public class ListProductsActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_products);

		GetProducts myTask = new GetProducts();

		myTask.execute();


	}

	class GetProducts extends AsyncTask<Void, Void, JSONArray>{

		@Override
		protected JSONArray doInBackground(Void... params) {
			String myResponse = null;
			try {
				myResponse = new DefaultHttpClient()
				.execute(
						new HttpGet("http://" + ip + ":9000/categories-on-product"), 
						new BasicResponseHandler()
						);

				return new JSONArray(myResponse);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void onPostExecute(JSONArray result) {
			ListView listView = (ListView) findViewById(R.id.list_products);

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
					R.layout.products_list_item, parent, false);

			TextView productName = (TextView) productsListItem
					.findViewById(R.id.product_name);

			TextView description = (TextView) productsListItem
					.findViewById(R.id.description_text);

			TextView cost = (TextView) productsListItem
					.findViewById(R.id.cost_text_View);

			TextView rrp = (TextView) productsListItem
					.findViewById(R.id.rrp_text_View);

			TextView cats = (TextView) productsListItem
					.findViewById(R.id.categories_text);

			Button buy = (Button) productsListItem
					.findViewById(R.id.edit_product_button);

			try {
				JSONObject product = products.getJSONObject(index);

				productName.setText(product.getJSONObject("product").getString("productName"));
				description.setText(product.getJSONObject("product").getString("description"));
				cost.setText(" " + product.getJSONObject("product").getString("cost"));
				rrp.setText(" " + product.getJSONObject("product").getString("rrp"));

				String strCategories = "";

				for(int i = 0; i < product.getJSONArray("categories").length(); i++) {
					JSONObject str = product.getJSONArray("categories").getJSONObject(i);
					strCategories += str.getString("categoryName") + ", ";
				}

				strCategories = strCategories.substring(0, strCategories.length() - 2);

				cats.setText(" " + strCategories);

				buy.setTag(product.getJSONObject("product").getString("productId"));

				buy.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						startActivity(new Intent(ListProductsActivity.this, CreateShoppingBasketActivity.class).putExtra("productId", view.getTag().toString()));
					}
				});
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}

			return productsListItem;
		}
	}

}
