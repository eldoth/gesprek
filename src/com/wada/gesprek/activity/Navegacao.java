package com.wada.gesprek.activity;

import com.wada.gesprek.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class Navegacao extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navegacao);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

}
