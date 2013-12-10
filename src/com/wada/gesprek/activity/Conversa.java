package com.wada.gesprek.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.wada.gesprek.R;
import com.wada.gesprek.core.Contato;

public class Conversa extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversa);
		Contato contatoSelecionado = (Contato) this.getIntent().getExtras().get("CONTATO"); 
		this.setTitle(contatoSelecionado.getNome());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	public void clickSend(View v) {
		Animation animAlpha = AnimationUtils.loadAnimation(Conversa.this, R.anim.anim_alpha);	
		v.startAnimation(animAlpha);
		//TODO:
	}

}
