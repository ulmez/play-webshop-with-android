package se.ulme.testapp;

import android.os.Bundle;
import android.widget.Toast;

public class StartActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(getUserData() != null) {
			Toast.makeText(this, "Welcome!", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, "Login from the menu, please", Toast.LENGTH_LONG).show();
		}
	}

}
