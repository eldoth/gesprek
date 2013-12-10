package com.wada.gesprek.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import com.wada.gesprek.R;
import com.wada.gesprek.core.Usuario;

public class Login extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	public void clickLogin(View v) {
		Animation animAlpha = AnimationUtils.loadAnimation(Login.this, R.anim.anim_alpha);	
		v.startAnimation(animAlpha);
		EditText usuario = (EditText) findViewById(R.id.editTextUsuario);
		EditText senha = (EditText) findViewById(R.id.editTextSenha);
		try {
			Usuario u = new Usuario(usuario.getText().toString(), "mac_address", senha.getText().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Intent intent = new Intent(this, Buscador.class);
		startActivity(intent);
	}

}
