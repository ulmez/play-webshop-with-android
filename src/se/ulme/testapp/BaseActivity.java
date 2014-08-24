package se.ulme.testapp;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.cookie.Cookie;

import se.ulme.play_webshop_with_android.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.MenuItem;

public class BaseActivity extends Activity {

	//	static protected final String ip = "192.168.1.7";
	static protected final String ip = "192.168.1.100";
	static protected List<Cookie> cookies;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);

		if(getUserData() == null) {
			menu.findItem(R.id.start_menu_list).setVisible(false);
			menu.findItem(R.id.create_product).setVisible(false);
			menu.findItem(R.id.create_category).setVisible(false);
			menu.findItem(R.id.list_products).setVisible(false);
			menu.findItem(R.id.list_categories_show).setVisible(false);
			menu.findItem(R.id.create_order).setVisible(false);
			menu.findItem(R.id.log_out).setVisible(false);
			menu.findItem(R.id.log_in).setVisible(true);
		} else if(getUserData().length() > 0) {
			menu.findItem(R.id.start_menu_list).setVisible(true);
			menu.findItem(R.id.create_product).setVisible(true);
			menu.findItem(R.id.create_category).setVisible(true);
			menu.findItem(R.id.list_products).setVisible(true);
			menu.findItem(R.id.list_categories_show).setVisible(true);
			menu.findItem(R.id.create_order).setVisible(true);
			menu.findItem(R.id.log_out).setVisible(true);
			menu.findItem(R.id.log_in).setVisible(false);
		}

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {

		case R.id.start_menu_list:
			startActivity(new Intent(this, StartActivity.class));
			return true;
		case R.id.create_product:
			startActivity(new Intent(this, CreateProductActivity.class));
			return true;
		case R.id.create_category:
			startActivity(new Intent(this, CreateCategoryActivity.class));
			return true;
		case R.id.list_products:
			startActivity(new Intent(this, ListProductsActivity.class));
			return true;
		case R.id.list_categories_show:
			startActivity(new Intent(this, ListCategoriesActivity.class));
			return true;
		case R.id.create_order:
			startActivity(new Intent(this, CreateOrderActivity.class));
			return true;
		case R.id.log_in:
			startActivity(new Intent(this, LogInActivity.class));
			return true;
		case R.id.log_out:
			clearUserData();
			startActivity(new Intent(this, LogOutActivity.class));
			return true;
		}

		return false;
	}

	public String getUserData() {
		SharedPreferences pref = getApplicationContext().getSharedPreferences("userPref", 0);

		return pref.getString("username", null);
	}

	public void clearUserData() {
		SharedPreferences pref = getApplicationContext().getSharedPreferences("userPref", 0);
		Editor editor = pref.edit();

		editor.clear();
		editor.commit();
	}

	protected Map<String, String> getFlashFields(Cookie cookie) {
		List<String> usernamePassword = Arrays.asList(cookie.getValue().split("&"));
		List<String> up = new ArrayList<>();

		for(String s1 : usernamePassword) {
			List<String> temp = Arrays.asList(s1.split("="));
			for(String s2 : temp) {
				up.add(s2);
			}
		}

		List<String> listKeys = new ArrayList<>();
		List<String> listValues = new ArrayList<>();

		int check = 0;
		String restUrl;

		for(String s : up) {
			try {
				restUrl = URLDecoder.decode(s, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}

			if(check == 0) {
				check = 1;
				listKeys.add(restUrl);
			} else {
				check = 0;
				listValues.add(restUrl);
			}
		}

		Map<String, String> flashFields = new HashMap<>();

		for(int i = 0; i < listKeys.size(); i++) {
			flashFields.put(listKeys.get(i), listValues.get(i));
		}

		return flashFields;
	}

}
