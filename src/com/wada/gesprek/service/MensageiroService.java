package com.wada.gesprek.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.util.Log;

import com.wada.gesprek.core.MensageiroCliente;
import com.wada.gesprek.core.MensageiroServidor;

public abstract class MensageiroService<T> {

	public static final String TAG = "Mensageiro";

	protected MensageiroServidor mensageiroServidor;
	protected MensageiroCliente<T> mensageiroCliente;
	private InetAddress myIp;
	private Socket socket;

	public MensageiroServidor getMensageiroServidor() {
		return mensageiroServidor;
	}

	public void setMensageiroServidor(MensageiroServidor mensageiroServidor) {
		this.mensageiroServidor = mensageiroServidor;
	}

	public MensageiroCliente<T> getMensageiroCliente() {
		return mensageiroCliente;
	}

	public void setMensageiroCliente(MensageiroCliente<T> mensageiroCliente) {
		this.mensageiroCliente = mensageiroCliente;
	}

	public void tearDownServidor() {
		if (mensageiroServidor != null) {
			mensageiroServidor.tearDown();
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
		if (socket != null) {
			if (socket.isConnected()) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		this.socket = socket;
	}

}
