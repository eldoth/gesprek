package com.wada.gesprek.activity;

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
import android.widget.TextView;

import com.wada.gesprek.R;
import com.wada.gesprek.core.Mensageiro;
import com.wada.gesprek.service.NsdHelper;

public class Buscador extends Activity {
	
	private NsdHelper mNsdHelper;
	private Handler updateHandler;
	public static final String TAG = "Buscador";
	private Mensageiro mensageiro;
	private TextView statusServidor;

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

        this.mNsdHelper = new NsdHelper(this);
        this.mNsdHelper.initializeNsd();
        
        //Registra Servidor
        if(this.mensageiro.getLocalPort() > -1) {
            this.mNsdHelper.registerService(this.mensageiro.getLocalPort());
        } else {
            Log.d(TAG, "ServerSocket não foi inicializado.");
        }
        
        this.statusServidor = (TextView) findViewById(R.id.status_servidores);
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
	
	public void clickConnect(View v) {
		this.mensageiro.tearDownCliente();

		this.mNsdHelper.initializeResolveListener();
        this.mNsdHelper.initializeDiscoveryListener();
        
        this.mensageiro.criaCliente();
		
        //Descobre Servidores
        this.mNsdHelper.discoverServices();
        
        NsdServiceInfo service = this.mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            Log.d(TAG, "Conectando...");
            this.mensageiro.connectToServer(service.getHost(),
                    service.getPort());
            this.statusServidor.setText("Conectado a algum servidor!");
            this.statusServidor.setTextColor(getResources().getColor(R.color.Green));
        } else {
            Log.d(TAG, "Não existe serviço para conectar!");
            this.statusServidor.setText("Não existe serviço para conectar!");
            this.statusServidor.setTextColor(getResources().getColor(R.color.Red));
        }
    }
	
	@Override
	protected void onPause() {
		if (this.mNsdHelper != null) {
			this.mNsdHelper.tearDown();
		}
		super.onPause();
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        if (this.mNsdHelper != null) {
            this.mNsdHelper.registerService(this.mensageiro.getLocalPort());
            this.mNsdHelper.discoverServices();
        }
    }
	
	@Override
    protected void onDestroy() {
        this.mNsdHelper.tearDown();
        this.mensageiro.tearDown();
        super.onDestroy();
    }

}
