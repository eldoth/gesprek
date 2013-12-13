package com.wada.gesprek.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

import com.wada.gesprek.service.MensageiroService;

public class MensageiroServidor {

	private ServerSocket serverSocket = null;
	private InetAddress serverIp;
	private Socket socket;
	Thread thread = null;

	private MensageiroService mensageiroService;

	public MensageiroServidor(InetAddress serverIp) {
		this.setServerIp(serverIp);
		thread = new Thread(new ServerThread());
		thread.start();
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		Log.d(MensageiroService.TAG, "setSocket sendo chamado.");
		if (socket == null) {
			Log.d(MensageiroService.TAG, "Setando um socket nulo.");
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

	public MensageiroService getMensageiroService() {
		return mensageiroService;
	}

	public void setMensageiroService(MensageiroService mensageiroService) {
		this.mensageiroService = mensageiroService;
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
					serverSocket = new ServerSocket(0, 20, getServerIp());
					setServerSocket(serverSocket);
				}

				while (!Thread.currentThread().isInterrupted()) {
					 Log.d(MensageiroService.TAG,
					 "ServerSocket Created, awaiting connection");
					 setSocket(serverSocket.accept());
				}
			} catch (IOException e) {
				Log.e(MensageiroService.TAG, "Error creating ServerSocket: ", e);
				e.printStackTrace();
			}
		}
	}

}
