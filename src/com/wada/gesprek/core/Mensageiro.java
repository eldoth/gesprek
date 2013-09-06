package com.wada.gesprek.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.http.conn.util.InetAddressUtils;

import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public final class Mensageiro {

	private static AudioRecord audioRecord;
	private static Integer frequency;
	private Handler updateHandler;
	private MensageiroServidor mensageiroServidor;
	private MensageiroCliente mensageiroCliente;

	private static final String TAG = "Mensageiro";
	
	private Socket socket;
	private int port = -1;
	private InetAddress serverIp;
	
	public Mensageiro(Handler handler) {
		this.updateHandler = handler;
		this.setServerIp(this.findIpLocal());
		mensageiroServidor = new MensageiroServidor(handler);
	}
	
	public void tearDown() {
		if (mensageiroServidor != null) {
			mensageiroServidor.tearDown();
		}
		if (mensageiroCliente != null) {
			mensageiroCliente.tearDown();
		}
    }
	
	public void tearDownCliente() {
		if (mensageiroCliente != null) {
			mensageiroCliente.mRecThread.interrupt();
			mensageiroCliente.mSendThread.interrupt();
			mensageiroCliente.tearDown();
		}
	}
	
    public void connectToServer(InetAddress address, int port) {
        this.mensageiroCliente = new MensageiroCliente(address, port);
    }

    public void sendMessage(String msg) {
        if (this.mensageiroCliente != null) {
            this.mensageiroCliente.sendMessage(msg);
        }
    }
    
    public InetAddress getServerIp() {
		return this.serverIp;
	}

	public void setServerIp(InetAddress serverIp) {
		this.serverIp = serverIp;
	}
	
	public InetAddress findIpLocal() {
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface ni : interfaces) {
				List<InetAddress> addresses = Collections.list(ni.getInetAddresses());
				for (InetAddress address : addresses) {
					if (!address.isLoopbackAddress()) {
						boolean isIPV4 = InetAddressUtils.isIPv4Address(address.getHostAddress().toUpperCase());
						if (isIPV4) {
							return address;
						}
					}
				}
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public int getLocalPort() {
        return this.port;
    }
    
    public void setLocalPort(int port) {
        this.port = port;
    }
    
    private Socket getSocket() {
        return this.socket;
    }
    
	private synchronized void setSocket(Socket socket) {
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

    
    public synchronized void updateMessages(String msg, boolean local) {
        Log.e(TAG, "Atualizando mensagem: " + msg);

        if (local) {
            msg = "me: " + msg;
        } else {
            msg = "them: " + msg;
        }

        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);

        Message message = new Message();
        message.setData(messageBundle);
        this.updateHandler.sendMessage(message);

    }

	public void inicializa() {
		//TODO
		// audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 11025,
		// AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT,
		// bufferSizeInBytes);
	}
	
	 private class MensageiroServidor {
	        ServerSocket serverSocket = null;
	        Thread thread = null;

	        public MensageiroServidor(Handler handler) {
	            thread = new Thread(new ServerThread());
	            thread.start();
	        }

	        public void tearDown() {
	            thread.interrupt();
	            try {
	                serverSocket.close();
	            } catch (IOException ioe) {
	                Log.e(TAG, "Error when closing server socket.");
	            }
	        }

	        class ServerThread implements Runnable {

	            @Override
	            public void run() {

	                try {
	                    serverSocket = new ServerSocket(0, 20, serverIp);
	                    setLocalPort(serverSocket.getLocalPort());
	                    setServerIp(serverSocket.getInetAddress());
	                    
	                    while (!Thread.currentThread().isInterrupted()) {
	                        Log.d(TAG, "ServerSocket Created, awaiting connection");
	                        setSocket(serverSocket.accept());
	                        Log.d(TAG, "Connected.");
//	                        if (mensageiroCliente == null) {
//	                            port = socket.getPort();
//	                            InetAddress address = socket.getInetAddress();
//	                            connectToServer(address, port);
//	                        }
	                    }
	                } catch (IOException e) {
	                    Log.e(TAG, "Error creating ServerSocket: ", e);
	                    e.printStackTrace();
	                }
	            }
	        }
	    }

	    private class MensageiroCliente {

	        private InetAddress mAddress;
	        private int PORT;

	        private final String CLIENT_TAG = "MensageiroCliente";

	        private Thread mSendThread;
	        private Thread mRecThread;

	        public MensageiroCliente(InetAddress address, int port) {

	            Log.d(CLIENT_TAG, "Creating mensageiroCliente");
	            this.mAddress = address;
	            this.PORT = port;

	            mSendThread = new Thread(new SendingThread());
	            mSendThread.start();
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
	                    if (getSocket() == null) {
	                        setSocket(new Socket(mAddress, PORT));
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
	                        Log.d(CLIENT_TAG, "Message sending loop interrupted, exiting");
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
	                            socket.getInputStream()));
	                    while (!Thread.currentThread().isInterrupted()) {

	                        String messageStr = null;
	                        messageStr = input.readLine();
	                        if (messageStr != null) {
	                            Log.d(CLIENT_TAG, "Read from the stream: " + messageStr);
	                            updateMessages(messageStr, false);
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

	        public void tearDown() {
	        	if (getSocket() != null) {
	        		try {
	        			getSocket().close();
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

	        public void sendMessage(String msg) {
	            try {
	                Socket socket = getSocket();
	                if (socket == null) {
	                    Log.d(CLIENT_TAG, "Socket is null, wtf?");
	                } else if (socket.getOutputStream() == null) {
	                    Log.d(CLIENT_TAG, "Socket output stream is null, wtf?");
	                }

	                PrintWriter out = new PrintWriter(
	                        new BufferedWriter(
	                                new OutputStreamWriter(getSocket().getOutputStream())), true);
	                out.println(msg);
	                out.flush();
	                updateMessages(msg, true);
	            } catch (UnknownHostException e) {
	                Log.d(CLIENT_TAG, "Unknown Host", e);
	            } catch (IOException e) {
	                Log.d(CLIENT_TAG, "I/O Exception", e);
	            } catch (Exception e) {
	                Log.d(CLIENT_TAG, "Error3", e);
	            }
	            Log.d(CLIENT_TAG, "Client sent message: " + msg);
	        }
	    }

}
