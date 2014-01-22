package com.wada.gesprek.core;

import java.net.InetAddress;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.wada.gesprek.service.MensageiroService;

public final class MensageiroServiceImpl extends MensageiroService<String> {

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

	public synchronized void atualizaSolicitacao(String requisicao) {
		Log.e(TAG, "Atualizando solicitante de comunicação: ");

		Bundle messageBundle = new Bundle();
		messageBundle.putString("Protocolo_conversacao", requisicao);

		Message message = new Message();
		message.setData(messageBundle);
		super.getUpdateSolicitacaoHandler().sendMessage(message);
	}

	@Override
	public void startSolicitadorServer() {
		this.servidor = new Servidor(this.getServerIp());
	}

	@Override
	public void connectToSolicitadorServer(InetAddress address, int port) {
		this.solicitadorConexao = new SolicitadorConexao(address, port);
	}

	@Override
	public void startMensageiroServer() {
		this.mensageiroCliente = new MensageiroClienteImpl();
	}
	
	@Override
	public void connectToMensageiroServer(String msg) {
		String[] pedacosMsg = msg.split(";");
		if (this.getSolicitadorConexao().getAddress().getHostAddress().equals(pedacosMsg[2])) {
			Log.d(TAG, " Endereco: " + this.getSolicitadorConexao().getAddress().getHostAddress());
			Log.d(TAG, " Porta: " + Integer.parseInt(pedacosMsg[3]));
			while (this.mensageiroCliente == null) {
				// Aguarda mensageiroCliente ser inicializado
			}
			this.mensageiroCliente.connectToMensageiroServer(this.getSolicitadorConexao().getAddress(), Integer.parseInt(pedacosMsg[3]));
		} else {
			Log.e(TAG, "O endereço IP não é igual ao IP do socket connectado");
		}
	}

}
