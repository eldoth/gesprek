package com.wada.gesprek.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.util.Log;

import com.wada.gesprek.service.MensageiroService;

public class SolicitadorConexao {

	private InetAddress address;
	private int PORT;

	private final String SOLICITADOR_TAG = "SolicitadorConexao";

	private Thread mSendThread;
	private Thread mReceiveThread;

	private MensageiroServiceImpl mensageiroService;

	public SolicitadorConexao(InetAddress address, int port) {

		Log.d(SOLICITADOR_TAG, "Creating solicitadorConexao");
		this.address = address;
		this.PORT = port;
		mensageiroService = MensageiroServiceImpl.getInstance();

		mSendThread = new Thread(new SendThread());
		mSendThread.start();
	}

	public MensageiroServiceImpl getMensageiroService() {
		return mensageiroService;
	}

	public void setMensageiroService(MensageiroServiceImpl mensageiroService) {
		this.mensageiroService = mensageiroService;
	}

	public Thread getmSendThread() {
		return mSendThread;
	}

	public Thread getmReceiveThread() {
		return mReceiveThread;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void sendMessage(String msg) {
		try {
			Socket socket = getMensageiroService().getSocket();

			if (socket == null) {
				Log.d(SOLICITADOR_TAG, "Socket is null, wtf?");
			} else if (socket.getOutputStream() == null) {
				Log.d(SOLICITADOR_TAG, "Socket output stream is null, wtf?");
			}
			Log.d(SOLICITADOR_TAG,
					"IP:" + socket.getInetAddress().getHostAddress()
							+ " , PORT: " + socket.getPort() + " Local Port: "
							+ socket.getLocalPort());

			PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(getMensageiroService().getSocket()
							.getOutputStream())), true);
			out.println(msg);
			out.flush();
		} catch (UnknownHostException e) {
			Log.d(SOLICITADOR_TAG, "Unknown Host", e);
		} catch (IOException e) {
			Log.d(SOLICITADOR_TAG, "I/O Exception", e);
		} catch (Exception e) {
			Log.d(SOLICITADOR_TAG, "Error3", e);
		}
		Log.d(SOLICITADOR_TAG, "Client sent message: " + msg);
	}

	class SendThread implements Runnable {

		BlockingQueue<String> mMessageQueue;
		private int QUEUE_CAPACITY = 10;

		public SendThread() {
			mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
		}

		@Override
		public void run() {
			try {
				if (getMensageiroService().getSocket() == null) {
					getMensageiroService().setSocket(new Socket(address, PORT));
					Log.d(SOLICITADOR_TAG, "Client-side socket initialized.");

				} else {
					Log.d(SOLICITADOR_TAG,
							"Socket already initialized. skipping!");
				}

				mReceiveThread = new Thread(new ReceiveThread());
				mReceiveThread.start();
			} catch (UnknownHostException e) {
				Log.d(SOLICITADOR_TAG, "Initializing socket failed, UHE", e);
			} catch (IOException e) {
				Log.d(SOLICITADOR_TAG, "Initializing socket failed, IOE.", e);
			}

			while (!Thread.currentThread().isInterrupted()) {
				try {
					String msg = mMessageQueue.take();
					sendMessage(msg);
				} catch (InterruptedException ie) {
					Log.d(SOLICITADOR_TAG,
							"Message sending loop interrupted, exiting");
				}
			}
		}

		public void tearDown() {
			Thread.currentThread().interrupt();
		}
	}

	class ReceiveThread implements Runnable {

		@Override
		public void run() {

			BufferedReader input;
			try {
				input = new BufferedReader(new InputStreamReader(
						getMensageiroService().getSocket().getInputStream()));
				while (!Thread.currentThread().isInterrupted()) {

					String messageStr = null;
					messageStr = input.readLine();
					if (messageStr != null) {
						Log.d(SOLICITADOR_TAG, "Read from the stream: "
								+ messageStr);
						Log.d(SOLICITADOR_TAG, "Mensagem recebida com sucesso!");
						if (messageStr.contains(MensageiroService.REQUISICAO)) {
							MensageiroServiceImpl.getInstance()
									.atualizaSolicitacao(messageStr);
						} else if (messageStr
								.contains(MensageiroService.CANCELAR)) {
							MensageiroServiceImpl.getInstance()
									.atualizaSolicitacao(messageStr);
						} else if (messageStr
								.contains(MensageiroService.ACEITAR)) {
							MensageiroServiceImpl.getInstance()
									.atualizaSolicitacao(messageStr);
						} else if (messageStr
								.contains(MensageiroService.REJEITAR)) {
							MensageiroServiceImpl.getInstance()
									.atualizaSolicitacao(messageStr);
						} else if (messageStr.contains(MensageiroService.RECEBER)) {
							MensageiroServiceImpl.getInstance()
									.connectToMensageiroServer(messageStr);
						}
					} else {
						Log.d(SOLICITADOR_TAG, "The nulls! The nulls!");
						break;
					}
				}
				input.close();

			} catch (IOException e) {
				Log.e(SOLICITADOR_TAG, "Server loop error: ", e);
			}
		}

		public void tearDown() {
			Thread.currentThread().interrupt();
		}
	}
}
