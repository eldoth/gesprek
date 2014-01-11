package com.wada.gesprek.core;

import java.net.InetAddress;

import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wada.gesprek.service.MensageiroService;

public final class MensageiroServiceTextoImpl extends MensageiroService<String> {

	private static AudioRecord audioRecord;
	private static Integer frequency;
	private Handler updateHandler;

	private static MensageiroServiceTextoImpl mensageiroServiceTextoImpl = null;

	public MensageiroServiceTextoImpl() throws Exception {
		if (mensageiroServiceTextoImpl == null) {
			super.setServerIp(this.findIpLocal());
			mensageiroServiceTextoImpl = this;
		} else {
			throw new Exception("Serviço de Mensageito já existe!");
		}
	}
	
	public static MensageiroServiceTextoImpl getInstance() {
		if (mensageiroServiceTextoImpl != null) {
			return mensageiroServiceTextoImpl;
		}
		return null;
		
	}
	
	public void setUpdateHandler(Handler updateHandler) {
		if (updateHandler != null) {
			this.updateHandler = updateHandler;
		}
	}

	@Override
	public void sendMessage(String msg) {
		if (this.mensageiroCliente != null) {
			this.mensageiroCliente.sendMessage(msg);
		}
	}

	public synchronized void updateMessages(String msg, boolean isLocal) {
		Log.e(TAG, "Atualizando mensagem: " + msg);

		if (isLocal) {
			msg = "eu: " + msg;
		} else {
			msg = "them: " + msg;
		}

		Bundle messageBundle = new Bundle();
		messageBundle.putString("msg", msg);

		Message message = new Message();
		message.setData(messageBundle);
		this.updateHandler.sendMessage(message);

	}
	
	@Override
	public void startServer() {
		this.mensageiroServidor = new MensageiroServidor(this.getServerIp());
	}
	
	@Override
	public void connectToServer(InetAddress address, int port) {
		this.mensageiroCliente = new MensageiroClienteTexto(address, port);
	}

	public void inicializa() {
		// TODO
		// audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 11025,
		// AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT,
		// bufferSizeInBytes);
	}

}
