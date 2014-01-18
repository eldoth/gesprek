package com.wada.gesprek.core;

import java.net.InetAddress;

import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wada.gesprek.service.MensageiroService;

public final class MensageiroServiceImpl extends MensageiroService<String> {

	private static AudioRecord audioRecord;
	private static Integer frequency;
	private Handler updateMessageHandler;

	private static MensageiroServiceImpl mensageiroServiceTextoImpl = null;

	public MensageiroServiceImpl() throws Exception {
		if (mensageiroServiceTextoImpl == null) {
			super.setServerIp(this.findIpLocal());
			mensageiroServiceTextoImpl = this;
		} else {
			throw new Exception("Serviço de Mensageito já existe!");
		}
	}

	public static MensageiroServiceImpl getInstance() {
		if (mensageiroServiceTextoImpl != null) {
			return mensageiroServiceTextoImpl;
		}
		return null;

	}

	public void setUpdateHandler(Handler updateHandler) {
		if (updateHandler != null) {
			this.updateMessageHandler = updateHandler;
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
			msg = "eu: " + msg + " originada de IP "
					+ this.getSocket().getInetAddress().getHostAddress()
					+ " Port: " + this.getSocket().getPort() + " Local Port: "
					+ this.getSocket().getLocalPort();
		} else {
			msg = "them: " + msg;
		}

		Bundle messageBundle = new Bundle();
		messageBundle.putString("msg", msg);

		Message message = new Message();
		message.setData(messageBundle);
		this.updateMessageHandler.sendMessage(message);

	}

	public synchronized void atualizaSolicitacao(String requisicao) {
		Log.e(TAG, "Atualizando solicitante de comunicação: ");

		Bundle messageBundle = new Bundle();
		messageBundle.putString("Protocolo_conversacao", requisicao);

		Message message = new Message();
		message.setData(messageBundle);
		super.getUpdateSolicitacaoHandler().sendMessage(message);
	}

	@Override
	public void startServer() {
		this.servidor = new Servidor(this.getServerIp());
	}

	@Override
	public void connectToServer(InetAddress address, int port) {
		this.solicitadorConexao = new SolicitadorConexao(address, port);
	}

	public void inicializa() {
		// TODO
		// audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 11025,
		// AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT,
		// bufferSizeInBytes);
	}

}
