package com.wada.gesprek.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.os.Handler;
import android.util.Log;

import com.wada.gesprek.core.MensageiroCliente;
import com.wada.gesprek.core.Servidor;
import com.wada.gesprek.core.SolicitadorConexao;

public abstract class MensageiroService<T> {

	public static final String TAG = "Mensageiro";
	public static final String REQUISICAO = "requisicao";
	public static final String CANCELAR = "cancelar";
	public static final String ACEITAR = "aceitar";
	public static final String REJEITAR = "rejeitar";

	protected Servidor servidor;
	protected MensageiroCliente<T> mensageiroCliente;
	protected SolicitadorConexao solicitadorConexao;
	private InetAddress myIp;
	private Socket socket;
	private Handler updateSolicitacaoHandler;

	public Servidor getServidor() {
		return servidor;
	}

	public void setServidor(Servidor servidor) {
		this.servidor = servidor;
	}

	public MensageiroCliente<T> getMensageiroCliente() {
		return mensageiroCliente;
	}

	public void setMensageiroCliente(MensageiroCliente<T> mensageiroCliente) {
		this.mensageiroCliente = mensageiroCliente;
	}

	public SolicitadorConexao getSolicitadorConexao() {
		return solicitadorConexao;
	}

	public void setSolicitadorConexao(SolicitadorConexao solicitadorConexao) {
		this.solicitadorConexao = solicitadorConexao;
	}

	public void tearDownServidor() {
		if (servidor != null) {
			servidor.tearDown();
		}
	}

	public void tearDownCliente() {
		if (mensageiroCliente != null) {
			mensageiroCliente.tearDown();
		}
	}

	public InetAddress findIpLocal() {
		try {
			List<NetworkInterface> interfaces = Collections
					.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface ni : interfaces) {
				List<InetAddress> addresses = Collections.list(ni
						.getInetAddresses());
				for (InetAddress address : addresses) {
					if (!address.isLoopbackAddress()) {
						boolean isIPV4 = InetAddressUtils.isIPv4Address(address
								.getHostAddress().toUpperCase());
						if (isIPV4) {
							return address;
						}
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void sendMessage(T msg) {
		// Extensões devem implementar.
	}

	public void startServer() {
		// Extensões devem implementar.
	}

	public void connectToServer(InetAddress address, int port) {
		// Extensões devem implementar.
	}

	public InetAddress getServerIp() {
		return this.myIp;
	}

	public void setServerIp(InetAddress serverIp) {
		this.myIp = serverIp;
	}

	public Socket getSocket() {
		return this.socket;
	}

	public synchronized void setSocket(Socket socket) {
		Log.d(TAG, "setSocket sendo chamado.");
		if (socket == null) {
			Log.d(TAG, "Setando um socket nulo.");
		}
		if (this.socket != null) {
			if (this.socket.isConnected()) {
				try {
					this.socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		this.socket = socket;
	}

	public Handler getUpdateSolicitacaoHandler() {
		return updateSolicitacaoHandler;
	}

	public void setUpdateSolicitacaoHandler(Handler updateSolicitacaoHandler) {
		this.updateSolicitacaoHandler = updateSolicitacaoHandler;
	}

}
