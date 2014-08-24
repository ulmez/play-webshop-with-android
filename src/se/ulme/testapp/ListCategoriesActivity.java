package se.ulme.testapp;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.ulme.play_webshop_with_android.R;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListCategoriesActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_categories_show);

		GetCategories myTask = new GetCategories();

		myTask.execute();
	}

	class GetCategories extends AsyncTask<Void, Void, JSONArray> {

		@Override
		protected JSONArray doInBackground(Void... params) {
			String myResponse = null;

			try {
				myResponse = new DefaultHttpClient()
				.execute(
						new HttpGet("http://" + ip + ":9000/categories"),
						new BasicResponseHandler()
						);

				return new JSONArray(myResponse);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void onPostExecute(JSONArray result) {
			ListView listView = (ListView) findViewById(R.id.list_categories_show);

			listView.setAdapter(new CategoryAdapter(result));
		}

	}

	class CategoryAdapter extends BaseAdapter {
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
			return index;
		}

		@Override
		public View getView(int index, View convertView, ViewGroup parent) {
			View categoriesListItem = getLayoutInflater().inflate(
					R.layout.categories_list_item_show, parent, false);

			TextView categoryName = (TextView) categoriesListItem
					.findViewById(R.id.category_text_show);

			TextView responsible = (TextView) categoriesListItem
					.findViewById(R.id.responsible_text_show);

			try {
				JSONObject category = categories.getJSONObject(index);


				categoryName.setText(" " + category.getString("categoryName"));
				responsible.setText(" " + category.getJSONObject("staff").getString("firstName") + " " + category.getJSONObject("staff").getString("surName"));
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}

			return categoriesListItem;
		}
	}

}
