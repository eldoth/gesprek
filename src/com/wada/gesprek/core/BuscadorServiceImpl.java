package com.wada.gesprek.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import com.wada.gesprek.service.BuscadorService;
import com.wada.gesprek.service.MensageiroService;

import android.util.Log;

public class BuscadorServiceImpl implements BuscadorService {

	private InetAddress myIp;
	private Socket socket;
	private ServerSocket serverSocket;
	Thread thread;

	@Override
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

	public void startServer() {
		thread = new Thread(new ServerThread());
	}

	@Override
	public InetAddress getServerIp() {
		return this.myIp;
	}

	@Override
	public void setServerIp(InetAddress serverIp) {
		this.myIp = serverIp;
	}

	@Override
	public Socket getSocket() {
		return this.socket;
	}

	@Override
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

	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	@Override
	public void tearDown() {
		if (thread != null) {
			thread.interrupt();
			try {
				serverSocket.close();
			} catch (IOException ioe) {
				Log.e(MensageiroService.TAG,
						"Error when closing server socket.");
			}
		}
	}

	class ServerThread implements Runnable {

		@Override
		public void run() {

			try {
				if (serverSocket == null) {
					serverSocket = new ServerSocket(0, 20, getServerIp());
					setServerSocket(serverSocket);
				}

			} catch (IOException e) {
				Log.e(MensageiroService.TAG, "Error creating ServerSocket: ", e);
				e.printStackTrace();
			}
		}
	}

}
