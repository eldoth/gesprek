package com.wada.gesprek.core;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

public class MensageiroClienteImpl implements MensageiroCliente<String> {

	private InetAddress otherAddress;
	private int myPort;
	private int otherPort;
	private boolean isReceiverInicializado = false;
	private final int sampleRate = 44100;
	private final int configuracaoCanalEntrada = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private final int configuracaoCanalSaida = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private final int formatoAudio = AudioFormat.ENCODING_PCM_16BIT;
	private AudioRecord recorder;
	private AudioTrack speaker;
	private boolean enviarAudio = false;

	private final String CLIENT_TAG = "MensageiroCliente";

	private Thread mSendThread;
	private Thread mRecThread;

	private MensageiroServiceImpl mensageiroService;

	public MensageiroClienteImpl() {

		Log.d(CLIENT_TAG, "Creating mensageiroCliente");
		mensageiroService = MensageiroServiceImpl.getInstance();

		mRecThread = new Thread(new ReceivingThread());
		mRecThread.start();
	}

	public MensageiroServiceImpl getMensageiroService() {
		return mensageiroService;
	}

	public void setMensageiroService(MensageiroServiceImpl mensageiroService) {
		this.mensageiroService = mensageiroService;
	}

	@Override
	public boolean isReceiverInicializado() {
		return isReceiverInicializado;
	}

	@Override
	public int getMyPort() {
		return myPort;
	}

	public void tearDown() {
		if (this.mRecThread != null) {
			this.mRecThread.interrupt();
		}
		if (this.mSendThread != null) {
			this.mSendThread.interrupt();
		}
	}

	@Override
	public void connectToMensageiroServer(InetAddress inetAddress, int port) {
		this.otherAddress = inetAddress;
		this.otherPort = port;
		Log.d(CLIENT_TAG, "Endereço e porta do destino setados");

	}

	public void falar() {
		this.enviarAudio = true;
		mSendThread = new Thread(new SendingThread());
		mSendThread.start();
	}

	public void pararFalar() {
		this.enviarAudio = false;
	}

	class SendingThread implements Runnable {

		int numSequencia = 0;
		DatagramSocket datagramSocket;

		@Override
		public void run() {
			try {
				datagramSocket = new DatagramSocket();
			} catch (SocketException e) {
				e.printStackTrace();
				Log.e(CLIENT_TAG, "Erro ao iniciar socket de envio UDP");
			}
			Log.d(CLIENT_TAG, "Servidor UDP de envio inicializado!");

			int minBufSize = AudioRecord.getMinBufferSize(sampleRate,
					configuracaoCanalEntrada, formatoAudio);
			byte[] buffer = new byte[minBufSize];
			Log.d(CLIENT_TAG, "Buffer criado com tamanho " + minBufSize);

			DatagramPacket packet;

			recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
					sampleRate, configuracaoCanalEntrada, formatoAudio,
					minBufSize);
			Log.d(CLIENT_TAG, "Recorder inicializado");

			recorder.startRecording();
			// String myFileName =
			// Environment.getExternalStorageDirectory().getAbsolutePath() +
			// "/ugesprek/audioRecordTest.pcm";

			while (enviarAudio) {
				
				// os 4 primeiros bytes 0,1,2 e 3 são utilizados para representar o numero de sequencia do pacote
				buffer[0] = (byte) numSequencia;
				minBufSize = recorder.read(buffer, 1, buffer.length);

				// putting buffer in the packet
				packet = new DatagramPacket(buffer, buffer.length,
						otherAddress, otherPort);

				try {
					datagramSocket.send(packet);
					if (numSequencia == Integer.MAX_VALUE) {
						numSequencia = 0;
					} else {
						numSequencia++;
					}
					Log.d(CLIENT_TAG, "Mensagem enviada para " + otherAddress
							+ " na porta " + otherPort + " com numSeq " + numSequencia);
				} catch (IOException e) {
					e.printStackTrace();
					Log.d(CLIENT_TAG, "Algum erro ocorreu no envio da mensagem");
				}

			}
		}

		public void tearDown() {
			Thread.currentThread().interrupt();
		}
	}

	class ReceivingThread implements Runnable {

		DatagramSocket datagramSocket;
		int ultimoRecebido;

		@Override
		public void run() {
			try {
				datagramSocket = new DatagramSocket();
			} catch (SocketException e) {
				Log.e(CLIENT_TAG, "Erro ao iniciar socket de recebimento UDP");
				e.printStackTrace();
			}
			myPort = datagramSocket.getLocalPort();
			Log.d(CLIENT_TAG, "Servidor UDP de recebimento inicializado!");
			isReceiverInicializado = true;

			int minBufSize = AudioRecord.getMinBufferSize(sampleRate,
					configuracaoCanalEntrada, formatoAudio);

			byte[] buffer = new byte[minBufSize];

			speaker = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
					configuracaoCanalSaida, formatoAudio, minBufSize,
					AudioTrack.MODE_STREAM);
			speaker.play();
			Log.d(CLIENT_TAG, "Volume maximo: " + AudioTrack.getMaxVolume());

			while (!Thread.currentThread().isInterrupted()) {

				DatagramPacket pacote = new DatagramPacket(buffer,
						buffer.length);
				try {
					datagramSocket.receive(pacote);
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(CLIENT_TAG, "Erro ao receber pacote");
				}
				Log.d(CLIENT_TAG, "Pacote recebido");

				buffer = pacote.getData();
				Log.d(CLIENT_TAG, "Dados do pacote lidos no buffer");
				
				if (buffer[0] <= ultimoRecebido) {
					continue;
				}
				
				ultimoRecebido = buffer[0];
				Log.d(CLIENT_TAG, "NumSeq do pacote lido " + buffer[0]);
				
				speaker.write(buffer, 1, minBufSize);
				Log.d(CLIENT_TAG, "Escrevendo conteudo do buffer no speaker");

			}
		}

		public void tearDown() {
			Thread.currentThread().interrupt();
		}
	}

}
