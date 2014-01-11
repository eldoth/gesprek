package com.wada.gesprek.activity;

import java.net.InetAddress;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.wada.gesprek.R;
import com.wada.gesprek.core.Contato;
import com.wada.gesprek.core.MensageiroServiceTextoImpl;
import com.wada.gesprek.service.MensageiroService;

public class Conversa extends Activity {
	
	private MensageiroServiceTextoImpl mensageiroServiceTextoImpl;
	private TextView mStatusView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversa);
		Contato contatoSelecionado = (Contato) this.getIntent().getSerializableExtra("CONTATO"); 
		this.setTitle(contatoSelecionado.getNome());
		
		mStatusView = (TextView) findViewById(R.id.status);
		
		mensageiroServiceTextoImpl = MensageiroServiceTextoImpl.getInstance();
		Handler updateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String chatLine = msg.getData().getString("msg");
			addChatLine(chatLine);
			}
		};
		mensageiroServiceTextoImpl.setUpdateHandler(updateHandler);
//		mensageiroServiceTextoImpl.startServer();
		mensageiroServiceTextoImpl.connectToServer(contatoSelecionado.getHost(), contatoSelecionado.getPort());
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
		
//		Contato contato = (Contato) this.getIntent().getSerializableExtra("CONTATO");
//		InetAddress contatoHost = contato.getHost();
//		int contatoPort = contato.getPort();
//		
//		if (contatoHost != null && contatoPort != 0) {
//            Log.d(MensageiroService.TAG, "Connecting.");
//            mensageiroServiceTextoImpl.connectToServer(contatoHost,
//            		contatoPort);
//        } else {
//            Log.d(MensageiroService.TAG, "No service to connect to!");
//        }
		EditText messageView = (EditText) this.findViewById(R.id.chatInput);
		if (messageView != null) {
            String messageString = messageView.getText().toString();
            if (!messageString.isEmpty()) {
            	mensageiroServiceTextoImpl.sendMessage(messageString);
            }
            messageView.setText("");
        }
	}
	
	public void addChatLine(String line) {
		mStatusView.append("\n" + line);
	}

}
