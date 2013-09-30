package com.wada.gesprek.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wada.gesprek.R;
import com.wada.gesprek.core.Contato;
import com.wada.gesprek.core.Mensageiro;
import com.wada.gesprek.service.NsdHelper;

public class Buscador extends Activity {

	private NsdHelper mNsdHelper;
	private Handler updateHandler;
	public static final String TAG = "Buscador";
	private Mensageiro mensageiro;
	private TextView statusServidor;
	private List<Contato> listaContatos;
	private ArrayAdapter<Contato> arrayAdapter;
	private ListView viewListaContatos;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_buscador);

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		this.updateHandler = new Handler();
		this.mensageiro = new Mensageiro(this.updateHandler);
		
		this.listaContatos = new ArrayList<Contato>();
		
		this.mNsdHelper = new NsdHelper(this, this);
		this.mNsdHelper.initializeServer();

		// Registra Servidor
		while (this.mensageiro.getLocalPort() <= -1) {
			//Espera a porta ser registrada pela thread do servidor
		}
		this.mNsdHelper.setMyIP(this.mensageiro.getServerIp());
		this.mNsdHelper.registerService(this.mensageiro.getLocalPort(), this.mensageiro.getServerIp());

		this.setViewListaContatos((ListView) findViewById(R.id.dispositivosEncontrados));
		
		this.setArrayAdapter(new ArrayAdapter<Contato>(this, R.layout.item_lista_contatos, this.getListaContatos()));
		
		this.getViewListaContatos().setAdapter(this.getArrayAdapter());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	public void clickDiscover(View v) {
		this.mNsdHelper.initializeClient();

		// Descobre Servidores
		this.mNsdHelper.discoverServices();

//		NsdServiceInfo service = this.mNsdHelper.getChosenServiceInfo();
//		if (service != null) {
//			Log.d(TAG, "Conectando...");
//			this.mensageiro.connectToServer(service.getHost(),
//					service.getPort());
//			this.statusServidor.setText("Conectado a algum servidor!");
//			this.statusServidor.setTextColor(getResources().getColor(
//					R.color.Green));
//		} else {
//			Log.d(TAG, "Não existe serviço para conectar!");
//			this.statusServidor.setText("Não existe serviço para conectar!");
//			this.statusServidor.setTextColor(getResources().getColor(
//					R.color.Red));
//		}
	}

	@Override
	protected void onPause() {
		if (this.mNsdHelper != null && this.mNsdHelper.isDiscoveryStarted()) {
			this.mNsdHelper.stopDiscovery();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (this.mNsdHelper != null) {
			if (this.mNsdHelper.getDiscoveryListener() != null && !this.mNsdHelper.isDiscoveryStarted()) {
				this.mNsdHelper.discoverServices();
			}
//			if (this.mensageiro.getLocalPort() > -1) {
//				this.mNsdHelper.registerService(this.mensageiro.getLocalPort(), this.mensageiro.getServerIp());
//			} else {
//				Log.d(TAG, "ServerSocket não foi inicializado.");
//			}
		}
	}

	@Override
	protected void onDestroy() {
		if (this.mNsdHelper != null) {
			this.mNsdHelper.tearDown();
//			this.mNsdHelper = null;
		}
		if (this.mensageiro != null) {
			this.mensageiro.tearDown();
//			this.mensageiro = null;
		}
		super.onDestroy();
	}

	/*@Override
	protected void onStop() {
		if (this.mNsdHelper != null) {
			this.mNsdHelper.tearDown();
//			this.mNsdHelper = null;
		}
		if (this.mensageiro != null) {
			this.mensageiro.tearDown();
//			this.mensageiro = null;
		}
		super.onStop();
	}*/
	
	public List<Contato> getListaContatos() {
		return listaContatos;
	}

	public void setListaContatos(List<Contato> listaContatos) {
		this.listaContatos = listaContatos;
	}

	public void addContato(Contato contato) {
		boolean contatoJaExiste = false;
		for (Contato c : this.getListaContatos()) {
			if (c.equals(contato)) {
				contatoJaExiste = true;
			}
		}
		if (!contatoJaExiste) { 
			this.getListaContatos().add(contato);
			this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					arrayAdapter.notifyDataSetChanged();				
				}
			});
		}
	}
	
	public void removeContato(Contato contato) {
		this.getListaContatos().remove(contato);
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				arrayAdapter.notifyDataSetChanged();				
			}
		});
	}

	public ArrayAdapter<Contato> getArrayAdapter() {
		return arrayAdapter;
	}

	public void setArrayAdapter(ArrayAdapter<Contato> arrayAdapter) {
		this.arrayAdapter = arrayAdapter;
	}

	public ListView getViewListaContatos() {
		return viewListaContatos;
	}

	public void setViewListaContatos(ListView viewListaContatos) {
		this.viewListaContatos = viewListaContatos;
	}
	
}
