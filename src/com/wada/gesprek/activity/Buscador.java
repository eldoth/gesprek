package com.wada.gesprek.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.wada.gesprek.R;
import com.wada.gesprek.core.Contato;
import com.wada.gesprek.core.MensageiroServiceTextoImpl;
import com.wada.gesprek.service.NsdHelper;

public class Buscador extends Activity {

	private NsdHelper mNsdHelper;
	public static final String TAG = "Buscador";
	private MensageiroServiceTextoImpl mensageiroServiceTexto;
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

		try {
			this.mensageiroServiceTexto = new MensageiroServiceTextoImpl();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.mensageiroServiceTexto.startServer();
		this.listaContatos = new ArrayList<Contato>();

		this.mNsdHelper = new NsdHelper(this, this);
		this.mNsdHelper.initializeServer();

		// Registra Servidor
		while (this.mensageiroServiceTexto.getMensageiroServidor().getServerSocket().getLocalPort() <= -1) {
			// Espera a porta ser registrada pela thread do servidor
		}
		this.mNsdHelper.setMyIP(this.mensageiroServiceTexto.getServerIp());
		this.mNsdHelper.registerService(
				this.mensageiroServiceTexto.getMensageiroServidor().getServerSocket().getLocalPort(),
				this.mensageiroServiceTexto.getServerIp());

		this.setViewListaContatos((ListView) findViewById(R.id.dispositivosEncontrados));

		this.setArrayAdapter(new ArrayAdapter<Contato>(this,
				R.layout.item_lista_contatos, this.getListaContatos()));

		this.getViewListaContatos().setAdapter(this.getArrayAdapter());
		this.getViewListaContatos().setOnItemClickListener(
				new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Intent intent = new Intent(Buscador.this,
								Conversa.class);
						Bundle bundle = new Bundle();
						bundle.putSerializable("CONTATO", getArrayAdapter()
								.getItem(position));
						bundle.putSerializable("LOCAL_HOST",
								mensageiroServiceTexto.getServerIp());
						intent.putExtras(bundle);
						intent.putExtra("LOCAL_PORT",
								mensageiroServiceTexto.getMensageiroServidor().getServerSocket().getLocalPort());
						startActivity(intent);
					}
				});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	public void clickDiscover(View v) {
		Animation animAlpha = AnimationUtils.loadAnimation(Buscador.this,
				R.anim.anim_alpha);
		v.startAnimation(animAlpha);
		this.mNsdHelper.initializeClient();

		// Descobre Servidores
		this.mNsdHelper.discoverServices();
	}

	@Override
	protected void onPause() {
		if (this.mNsdHelper != null) {
			if (this.mNsdHelper.isDiscoveryStarted()) {
				this.mNsdHelper.stopDiscovery();
			}
			if (this.mNsdHelper.isServiceRegistered()) {
				this.mNsdHelper.unregisterService();
			}
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (this.mNsdHelper != null) {
			if (this.mNsdHelper.getDiscoveryListener() != null
					&& !this.mNsdHelper.isDiscoveryStarted()) {
				this.mNsdHelper.discoverServices();
			}
			if (this.mNsdHelper.getRegistrationListener() != null
					&& !this.mNsdHelper.isServiceRegistered()) {
				this.mNsdHelper.registerService(
						this.mensageiroServiceTexto.getMensageiroServidor().getServerSocket().getLocalPort(),
						this.mensageiroServiceTexto.getServerIp());
			}
		}
	}

	@Override
	protected void onDestroy() {
		if (this.mNsdHelper != null) {
			this.mNsdHelper.tearDown();
		}
		if (this.mensageiroServiceTexto != null) {
			this.mensageiroServiceTexto.tearDownServidor();
			this.mensageiroServiceTexto.tearDownCliente();
		}
		super.onDestroy();
	}

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

	public void atualizarContato(NsdServiceInfo nsdServiceInfo) {
		Contato contatoAtualizado = null;
		int indexContato = -1;
		for (Contato c : this.getListaContatos()) {
			if (c.getHost().equals(nsdServiceInfo.getHost())) {
				contatoAtualizado = c;
				indexContato = this.getListaContatos().indexOf(c);
				continue;
			}
		}
		if (contatoAtualizado != null) {
			this.getListaContatos().set(indexContato, contatoAtualizado);
			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					arrayAdapter.notifyDataSetChanged();
				}
			});
		}
	}

	public void removeContato(NsdServiceInfo nsdServiceInfo) {
		Contato removido = null;
		for (Contato c : this.getListaContatos()) {
			if (c.getServiceName().equals(nsdServiceInfo.getServiceName())
					|| c.getHost().equals(nsdServiceInfo.getHost())) {
				removido = c;
			}
		}
		if (removido != null) {
			this.getListaContatos().remove(removido);
			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					arrayAdapter.notifyDataSetChanged();
				}
			});
		}
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
