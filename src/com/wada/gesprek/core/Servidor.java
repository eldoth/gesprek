package com.wada.gesprek.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import android.util.Log;

import com.wada.gesprek.service.MensageiroService;

public class Servidor {

	private ServerSocket serverSocket = null;
	private InetAddress serverIp;
//	private Socket socket;
	Thread thread = null;

	public Servidor(InetAddress serverIp) {
		this.setServerIp(serverIp);
		thread = new Thread(new ServerThread());
		thread.start();
	}

//	public Socket getSocket() {
//		return socket;
//	}

//	public void setSocket(Socket socket) {
//		Log.d(MensageiroService.TAG, "setSocket sendo chamado.");
//		if (socket == null) {
//			Log.d(MensageiroService.TAG, "Setando um socket nulo.");
//		}
//		if (this.socket != null) {
//			if (this.socket.isConnected()) {
//				try {
//					this.socket.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		this.socket = socket;
//	}

	public InetAddress getServerIp() {
		return serverIp;
	}

	public void setServerIp(InetAddress serverIp) {
		this.serverIp = serverIp;
	}

	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public void tearDown() {
		thread.interrupt();
		try {
			serverSocket.close();
		} catch (IOException ioe) {
			Log.e(MensageiroService.TAG, "Error when closing server socket.");
		}
	}

	class ServerThread implements Runnable {

		@Override
		public void run() {

			try {
				if (serverSocket == null) {
					serverSocket = new ServerSocket(0);
					setServerSocket(serverSocket);
					Log.d(MensageiroService.TAG, "Servidor iniciado com IP: "
							+ serverSocket.getInetAddress().getHostAddress()
							+ " Local Port: " + serverSocket.getLocalPort());
				}

				while (!Thread.currentThread().isInterrupted()) {
					Log.d(MensageiroService.TAG,
							"ServerSocket Created, awaiting connection");
					MensageiroServiceImpl.getInstance().setSocket(serverSocket.accept());
					Log.d(MensageiroService.TAG, "Someone connected. Port:"
							+ MensageiroServiceImpl.getInstance().getSocket().getPort() + " Local Port:"
							+ MensageiroServiceImpl.getInstance().getSocket().getLocalPort() + " IP:"
							+ MensageiroServiceImpl.getInstance().getSocket().getInetAddress().getHostAddress());
					if (MensageiroServiceImpl.getInstance()
							.getSolicitadorConexao() == null) {
						MensageiroServiceImpl.getInstance()
								.connectToSolicitadorServer(MensageiroServiceImpl.getInstance().getSocket().getInetAddress(),
										MensageiroServiceImpl.getInstance().getSocket().getPort());
					}
				}
			} catch (IOException e) {
				Log.e(MensageiroService.TAG, "Error creating ServerSocket: ", e);
				e.printStackTrace();
			}
		}
	}

}
