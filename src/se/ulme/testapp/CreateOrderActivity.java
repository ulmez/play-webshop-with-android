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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CreateOrderActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_order_send);

		GetShoppingBasket myTask = new GetShoppingBasket();

		myTask.execute();

		Button createOrderButton = (Button) findViewById(R.id.list_order_send_button);

		createOrderButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				CreateOrderTask createOrderTask = new CreateOrderTask();

				createOrderTask.execute();
			}
		});
	}

	class GetShoppingBasket extends AsyncTask<Void, Void, JSONArray> {

		@Override
		protected JSONArray doInBackground(Void... params) {
			String myResponse = null;

			try {
				myResponse = new DefaultHttpClient()
				.execute(
						new HttpGet("http://" + ip + ":9000/shoppingbasket-for-user/" + getUserData()),
						new BasicResponseHandler()
						);

				return new JSONArray(myResponse);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void onPostExecute(JSONArray result) {
			ListView listView = (ListView) findViewById(R.id.list_order_send);

			listView.setAdapter(new ShoppingBasketAdapter(result));
		}

	}

	class ShoppingBasketAdapter extends BaseAdapter {
		private JSONArray shoppingBaskets;

		public ShoppingBasketAdapter(JSONArray shoppingBaskets) {
			this.shoppingBaskets = shoppingBaskets;
		}

		@Override
		public int getCount() {
			if(shoppingBaskets.length() == 0) {
				return 1;
			} else {
				return shoppingBaskets.length();
			}
		}

		@Override
		public Object getItem(int index) {
			try {
				return shoppingBaskets.getJSONObject(index);
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
			View shoppingBasketsListItem = getLayoutInflater().inflate(
					R.layout.create_order_form, parent, false);

			TextView productNameHeadline = (TextView) shoppingBasketsListItem
					.findViewById(R.id.order_product_name_headline);

			TextView descriptionHeadline = (TextView) shoppingBasketsListItem
					.findViewById(R.id.order_description_headline);

			TextView costHeadline = (TextView) shoppingBasketsListItem
					.findViewById(R.id.order_cost_headline);

			TextView rrpHeadline = (TextView) shoppingBasketsListItem
					.findViewById(R.id.order_rrp_headline);

			TextView quantityHeadline = (TextView) shoppingBasketsListItem
					.findViewById(R.id.order_quantity_headline);

			Button button = (Button) findViewById(R.id.list_order_send_button);

			TextView productName = (TextView) shoppingBasketsListItem
					.findViewById(R.id.order_product_name_text);

			TextView description = (TextView) shoppingBasketsListItem
					.findViewById(R.id.order_description_text);

			TextView cost = (TextView) shoppingBasketsListItem
					.findViewById(R.id.order_cost_text);

			TextView rrp = (TextView) shoppingBasketsListItem
					.findViewById(R.id.order_rrp_text);

			TextView quantity = (TextView) shoppingBasketsListItem
					.findViewById(R.id.order_quantity_text);

			Button edit = (Button) shoppingBasketsListItem
					.findViewById(R.id.change_quantity_button);

			if(shoppingBaskets.length() > 0) {
				productName.setVisibility(View.VISIBLE);
				description.setVisibility(View.VISIBLE);
				cost.setVisibility(View.VISIBLE);
				rrp.setVisibility(View.VISIBLE);
				quantity.setVisibility(View.VISIBLE);
				button.setVisibility(View.VISIBLE);

				descriptionHeadline.setVisibility(View.VISIBLE);
				costHeadline.setVisibility(View.VISIBLE);
				rrpHeadline.setVisibility(View.VISIBLE);
				quantityHeadline.setVisibility(View.VISIBLE);
				edit.setVisibility(View.VISIBLE);

				try {
					JSONObject shoppingBasket = shoppingBaskets.getJSONObject(index);

					productName.setText(" " + shoppingBasket.getJSONObject("product").getString("productName"));
					description.setText(" " + shoppingBasket.getJSONObject("product").getString("description"));
					cost.setText(" " + shoppingBasket.getJSONObject("product").getString("cost"));
					rrp.setText(" " + shoppingBasket.getJSONObject("product").getString("rrp"));
					quantity.setText(" " + shoppingBasket.getString("quantity"));

					edit.setTag(shoppingBasket.getString("shoppingBasketId"));

					edit.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {
							startActivity(new Intent(CreateOrderActivity.this, EditShoppingBasketActivity.class).putExtra("shoppingBasketId", view.getTag().toString()));
						}
					});
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
			} else {
				productNameHeadline.setText("You don't have any products in your shoppingbasket");

				productName.setVisibility(View.GONE);
				description.setVisibility(View.GONE);
				cost.setVisibility(View.GONE);
				rrp.setVisibility(View.GONE);
				quantity.setVisibility(View.GONE);
				button.setVisibility(View.GONE);

				descriptionHeadline.setVisibility(View.GONE);
				costHeadline.setVisibility(View.GONE);
				rrpHeadline.setVisibility(View.GONE);
				quantityHeadline.setVisibility(View.GONE);
				edit.setVisibility(View.GONE);
			}

			return shoppingBasketsListItem;
		}
	}

	class CreateOrderTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			try {
				HttpPost post = new HttpPost("http://" + ip + ":9000/make-order");

				DefaultHttpClient client = new DefaultHttpClient();

				cookies = client.getCookieStore().getCookies();

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

				nameValuePairs.add(new BasicNameValuePair("email", getUserData()));

				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				String response = client.execute(post, new BasicResponseHandler());

				return response;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(CreateOrderActivity.this, "Success!", Toast.LENGTH_LONG).show();

			startActivity(new Intent(CreateOrderActivity.this, CreateOrderActivity.class));
		}
	}

}
