package com.wada.gesprek.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wada.gesprek.R;
import com.wada.gesprek.core.MensageiroServiceImpl;
import com.wada.gesprek.core.Usuario;
import com.wada.gesprek.service.MensageiroService;

public class Conversa extends Activity {

	private MensageiroServiceImpl mensageiroServiceImpl;
	private TextView mStatusView;

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
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				
				case MotionEvent.ACTION_DOWN:
					mensageiroServiceImpl.getMensageiroCliente().falar();
					break;
				case MotionEvent.ACTION_UP:
					mensageiroServiceImpl.getMensageiroCliente().pararFalar();
				}
				return true;
			}
		});
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

}
