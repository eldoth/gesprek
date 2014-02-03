package com.wada.gesprek.activity;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.wada.gesprek.R;
import com.wada.gesprek.core.MensageiroServiceImpl;
import com.wada.gesprek.core.Usuario;
import com.wada.gesprek.service.MensageiroService;

public class Conversa extends Activity {

	private MensageiroServiceImpl mensageiroServiceImpl;
	private TextView mStatusView;
	private boolean isEncerranteConversa = true;

	public final String TAG = "Conversa";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversa);
		String nomeContato = this.getIntent().getStringExtra("NOME_CONTATO");
		this.setTitle(nomeContato);

		mensageiroServiceImpl = MensageiroServiceImpl.getInstance();
		mensageiroServiceImpl.startMensageiroServer();
		while (mensageiroServiceImpl.getMensageiroCliente() == null
				|| !mensageiroServiceImpl.getMensageiroCliente()
						.isReceiverInicializado()) {
			// Aguarda inicializar o servidor UDP de recebimento de mensagem
		}
		mensageiroServiceImpl.getSolicitadorConexao().sendMessage(
				MensageiroService.RECEBER
						+ ";"
						+ Usuario.getInstance().getNome()
						+ ";"
						+ mensageiroServiceImpl.findIpLocal().getHostAddress()
						+ ";"
						+ mensageiroServiceImpl.getMensageiroCliente()
								.getMyPort());
		Button botaoFalar = (Button) findViewById(R.id.botao_falar);
		botaoFalar.setOnTouchListener(new View.OnTouchListener() {
			
			Animation animAlpha = AnimationUtils.loadAnimation(Conversa.this,
					R.anim.anim_alpha);

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					v.startAnimation(animAlpha);
					mensageiroServiceImpl.getMensageiroCliente().falar();
					break;
				case MotionEvent.ACTION_UP:
					mensageiroServiceImpl.getMensageiroCliente().pararFalar();
				}
				return true;
			}
		});

		Handler endRequisitionHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String chatLine = msg.getData().getString(
						"Protocolo_conversacao");
				if (chatLine.contains(MensageiroService.FIM)) {
					isEncerranteConversa = false;
					finish();
				}
			}
		};

		this.mensageiroServiceImpl
				.setEndSolicitacaoHandler(endRequisitionHandler);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	public void addChatLine(String line) {
		mStatusView.append("\n" + line);
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "OnDestroy foi chamado");
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "onPause foi chamado");
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.i(TAG, "onStop foi chamado");
		if (isEncerranteConversa) {
			this.enviaMensagemEncerramento();
		}
		this.encerraConversa();
		super.onStop();
	}

	private void enviaMensagemEncerramento() {
		this.mensageiroServiceImpl.getSolicitadorConexao().sendMessage(
				MensageiroService.FIM
						+ ";"
						+ Usuario.getInstance().getNome()
						+ ";"
						+ MensageiroServiceImpl.getInstance().findIpLocal()
								.getHostAddress());
	}

	private void encerraConversa() {
		this.mensageiroServiceImpl.getMensageiroCliente().tearDown();
		this.mensageiroServiceImpl.getSolicitadorConexao().tearDown();
		try {
			this.mensageiroServiceImpl.getSocket().close();
			this.mensageiroServiceImpl.setSocket(null);
			this.mensageiroServiceImpl.setSolicitadorConexao(null);
			this.mensageiroServiceImpl.setMensageiroCliente(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
