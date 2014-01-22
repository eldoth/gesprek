package com.wada.gesprek.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.wada.gesprek.R;
import com.wada.gesprek.core.Contato;
import com.wada.gesprek.core.MensageiroServiceImpl;
import com.wada.gesprek.core.Usuario;
import com.wada.gesprek.service.MensageiroService;
import com.wada.gesprek.service.NsdHelper;

public class Buscador extends Activity {

	private NsdHelper mNsdHelper;
	public static final String TAG = "Buscador";
	private MensageiroServiceImpl mensageiroServiceImpl;
	private List<Contato> listaContatos;
	private ArrayAdapter<Contato> arrayAdapter;
	private ListView viewListaContatos;
	private boolean isSolicitanteConversa = false;
	private AlertDialog alertaSolicitacaoConexao;
	private Contato contatoSelecionado;
	
	private String nomeContato;

	private DialogInterface.OnClickListener listenerAbreConversa;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_buscador);

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		try {
			this.mensageiroServiceImpl = new MensageiroServiceImpl();
		} catch (Exception e) {
			e.printStackTrace();
			this.mensageiroServiceImpl = MensageiroServiceImpl.getInstance();
		}

		this.mensageiroServiceImpl.startSolicitadorServer();
		this.listaContatos = new ArrayList<Contato>();

		this.mNsdHelper = new NsdHelper(this, this);
		this.mNsdHelper.initializeServer();

		// Registra Servidor
		while (this.mensageiroServiceImpl.getServidor().getServerSocket() == null
				|| this.mensageiroServiceImpl.getServidor().getServerSocket()
						.getLocalPort() <= -1) {
			// Espera a porta ser registrada pela thread do servidor
		}
		this.mNsdHelper.setMyIP(this.mensageiroServiceImpl.getServerIp());
		this.mNsdHelper.registerService(this.mensageiroServiceImpl
				.getServidor().getServerSocket().getLocalPort(),
				this.mensageiroServiceImpl.getServerIp());

		this.setViewListaContatos((ListView) findViewById(R.id.dispositivosEncontrados));

		this.setArrayAdapter(new ArrayAdapter<Contato>(this,
				R.layout.item_lista_contatos, this.getListaContatos()));

		this.getViewListaContatos().setAdapter(this.getArrayAdapter());
		this.getViewListaContatos().setOnItemClickListener(
				new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						isSolicitanteConversa = true;
						contatoSelecionado = getArrayAdapter()
								.getItem(position);
						mensageiroServiceImpl.connectToSolicitadorServer(
								contatoSelecionado.getHost(),
								contatoSelecionado.getPort());
						while (mensageiroServiceImpl.getSolicitadorConexao()
								.getmReceiveThread() == null) {
							// Espera as threads estarem funcionando
							// corretamente.
						}
						mensageiroServiceImpl.getSolicitadorConexao()
								.sendMessage(
										MensageiroService.REQUISICAO
												+ ";"
												+ Usuario.getInstance()
														.getNome()
												+ ";"
												+ MensageiroServiceImpl
														.getInstance()
														.findIpLocal().getHostAddress());
						atualizaPainelSolicitacaoConversa("local;");
					}
				});

		Handler updateRequisitionHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String chatLine = msg.getData().getString(
						"Protocolo_conversacao");
				atualizaPainelSolicitacaoConversa(chatLine);
			}
		};

		this.mensageiroServiceImpl
				.setUpdateSolicitacaoHandler(updateRequisitionHandler);

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
				this.mNsdHelper.registerService(this.mensageiroServiceImpl
						.getServidor().getServerSocket().getLocalPort(),
						this.mensageiroServiceImpl.getServerIp());
			}
		}
	}

	@Override
	protected void onDestroy() {
		Log.i("OnDestroy: ", "Method Called");
		if (this.mNsdHelper != null) {
			this.mNsdHelper.tearDown();
		}
		if (this.mensageiroServiceImpl != null) {
			this.mensageiroServiceImpl.tearDownServidor();
			this.mensageiroServiceImpl.tearDownCliente();
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

	public void atualizaPainelSolicitacaoConversa(String message) {
		String[] atributos = message.split(";");
		if (atributos[0].equals("local")) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Solicitação de conversa");
			if (isSolicitanteConversa) {
				builder.setMessage("Aguardando a aprovação da solicitação de conversa com "
						+ contatoSelecionado.getNome());
				builder.setNegativeButton("Cancelar",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mensageiroServiceImpl.getSolicitadorConexao()
										.sendMessage(
												MensageiroService.CANCELAR
														+ ";"
														+ Usuario.getInstance()
																.getNome()
														+ ";"
														+ MensageiroServiceImpl
																.getInstance()
																.findIpLocal().getHostAddress());
								alertaSolicitacaoConexao.cancel();
							}
						});
				alertaSolicitacaoConexao = builder.create();
				alertaSolicitacaoConexao.show();
			}

		} else if (atributos[0].equals(MensageiroService.REQUISICAO)) {
			nomeContato = atributos[1];
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Solicitação de conversa");
			builder.setMessage("Deseja iniciar uma conversa com "
					+ atributos[1] + "?");
			builder.setPositiveButton("Aceitar", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					mensageiroServiceImpl.getSolicitadorConexao()
					.sendMessage(
							MensageiroService.ACEITAR
									+ ";"
									+ Usuario.getInstance()
											.getNome()
									+ ";"
									+ MensageiroServiceImpl
											.getInstance()
											.findIpLocal().getHostAddress());
					Intent intent = new Intent(Buscador.this, Conversa.class);
					Bundle bundle = new Bundle();
					bundle.putString("NOME_CONTATO", nomeContato);
					bundle.putSerializable("LOCAL_HOST",
							mensageiroServiceImpl.getServerIp());
					intent.putExtras(bundle);
					intent.putExtra("LOCAL_PORT", mensageiroServiceImpl
							.getServidor().getServerSocket().getLocalPort());
					startActivity(intent);
				}
			});

			builder.setNegativeButton("Rejeitar",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							mensageiroServiceImpl.getSolicitadorConexao()
							.sendMessage(
									MensageiroService.CANCELAR
											+ ";"
											+ Usuario.getInstance()
													.getNome()
											+ ";"
											+ MensageiroServiceImpl
													.getInstance()
													.findIpLocal().getHostAddress());
							alertaSolicitacaoConexao.cancel();
						}
					});
			alertaSolicitacaoConexao = builder.create();
			alertaSolicitacaoConexao.show();
		} else if (atributos[0].equals(MensageiroService.ACEITAR)) {
			Intent intent = new Intent(Buscador.this, Conversa.class);
			Bundle bundle = new Bundle();
			bundle.putString("NOME_CONTATO", contatoSelecionado.getNome());
			bundle.putSerializable("LOCAL_HOST",
					mensageiroServiceImpl.getServerIp());
			intent.putExtras(bundle);
			intent.putExtra("LOCAL_PORT", mensageiroServiceImpl
					.getServidor().getServerSocket().getLocalPort());
			alertaSolicitacaoConexao.cancel();
			startActivity(intent);

		} else if (atributos[0].equals(MensageiroService.REJEITAR)) {
			if (alertaSolicitacaoConexao != null
					&& alertaSolicitacaoConexao.isShowing()) {
				alertaSolicitacaoConexao.cancel();
			}
		} else if (atributos[0].equals(MensageiroService.CANCELAR)) {
			if (alertaSolicitacaoConexao != null
					&& alertaSolicitacaoConexao.isShowing()) {
				alertaSolicitacaoConexao.cancel();
			}
		}
		isSolicitanteConversa = false;
	}

	public boolean isSolicitanteConversa() {
		return isSolicitanteConversa;
	}

	public void setSolicitanteConversa(boolean isSolicitanteConversa) {
		this.isSolicitanteConversa = isSolicitanteConversa;
	}

	public void criaListenerAbreConversa() {
		this.listenerAbreConversa = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Buscador.this, Conversa.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("CONTATO", contatoSelecionado);
				bundle.putSerializable("LOCAL_HOST",
						mensageiroServiceImpl.getServerIp());
				intent.putExtras(bundle);
				intent.putExtra("LOCAL_PORT", mensageiroServiceImpl
						.getServidor().getServerSocket().getLocalPort());
				startActivity(intent);
			}
		};
	}
	
	/**
	 * Endereço IP deve estar no formato "x.x.x.x"
	 * @param enderecoIp
	 * @param nomeUsuario
	 * @return
	 */
	public Contato encontraContatoPorIpUsuario(String enderecoIp, String nomeUsuario) {
		for (Contato c : this.getListaContatos()) {
			if (c.getHost().getHostAddress().equals(enderecoIp) && c.getNome().equals(nomeUsuario)) {
				return c;
			}
		}
		return null;
	}

}
