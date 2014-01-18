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

public class MensageiroClienteImpl implements MensageiroCliente<String>{

	private InetAddress mAddress;
	private int PORT;

	private final String CLIENT_TAG = "MensageiroCliente";

	private Thread mSendThread;
	private Thread mRecThread;

	private MensageiroServiceImpl mensageiroService;

	public MensageiroClienteImpl(InetAddress address, int port) {

		Log.d(CLIENT_TAG, "Creating mensageiroCliente");
		this.mAddress = address;
		this.PORT = port;
		mensageiroService = MensageiroServiceImpl.getInstance();

		mSendThread = new Thread(new SendingThread());
		mSendThread.start();
	}

	public MensageiroServiceImpl getMensageiroService() {
		return mensageiroService;
	}

	public void setMensageiroService(MensageiroServiceImpl mensageiroService) {
		this.mensageiroService = mensageiroService;
	}
	
	public void tearDown() {
		if (getMensageiroService().getSocket() != null) {
			try {
				getMensageiroService().getSocket().close();
				getMensageiroService().setSocket(null);
			} catch (IOException ioe) {
				Log.e(CLIENT_TAG, "Error when closing server socket.");
			}
		}
		if (this.mRecThread != null) {
			this.mRecThread.interrupt();
		}
		if (this.mSendThread != null) {
			this.mSendThread.interrupt();
		}
	}

	@Override
	public void sendMessage(String msg) {
		try {
			Socket socket = getMensageiroService().getSocket();
			
			if (socket == null) {
				Log.d(CLIENT_TAG, "Socket is null, wtf?");
			} else if (socket.getOutputStream() == null) {
				Log.d(CLIENT_TAG, "Socket output stream is null, wtf?");
			}
			Log.d(CLIENT_TAG, "IP:" + socket.getInetAddress().getHostAddress() + " , PORT: " +socket.getPort() + " Local Port: " + socket.getLocalPort());

			PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(getMensageiroService().getSocket()
							.getOutputStream())), true);
			out.println(msg);
			out.flush();
			getMensageiroService().updateMessages((String) msg, true);
		} catch (UnknownHostException e) {
			Log.d(CLIENT_TAG, "Unknown Host", e);
		} catch (IOException e) {
			Log.d(CLIENT_TAG, "I/O Exception", e);
		} catch (Exception e) {
			Log.d(CLIENT_TAG, "Error3", e);
		}
		Log.d(CLIENT_TAG, "Client sent message: " + msg);
	}

	class SendingThread implements Runnable {

		BlockingQueue<String> mMessageQueue;
		private int QUEUE_CAPACITY = 10;

		public SendingThread() {
			mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
		}

		@Override
		public void run() {
			try {
				if (getMensageiroService().getSocket() == null) {
					getMensageiroService()
							.setSocket(new Socket(mAddress, PORT));
					Log.d(CLIENT_TAG, "Client-side socket initialized.");

				} else {
					Log.d(CLIENT_TAG, "Socket already initialized. skipping!");
				}

				mRecThread = new Thread(new ReceivingThread());
				mRecThread.start();
			} catch (UnknownHostException e) {
				Log.d(CLIENT_TAG, "Initializing socket failed, UHE", e);
			} catch (IOException e) {
				Log.d(CLIENT_TAG, "Initializing socket failed, IOE.", e);
			}

			while (!Thread.currentThread().isInterrupted()) {
				try {
					String msg = mMessageQueue.take();
					sendMessage(msg);
				} catch (InterruptedException ie) {
					Log.d(CLIENT_TAG,
							"Message sending loop interrupted, exiting");
				}
			}
		}

		public void tearDown() {
			Thread.currentThread().interrupt();
		}
	}

	class ReceivingThread implements Runnable {

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
						Log.d(CLIENT_TAG, "Read from the stream: " + messageStr);
						getMensageiroService()
								.updateMessages(messageStr, false);
					} else {
						Log.d(CLIENT_TAG, "The nulls! The nulls!");
						break;
					}
				}
				input.close();

			} catch (IOException e) {
				Log.e(CLIENT_TAG, "Server loop error: ", e);
			}
		}

		public void tearDown() {
			Thread.currentThread().interrupt();
		}
	}

}
