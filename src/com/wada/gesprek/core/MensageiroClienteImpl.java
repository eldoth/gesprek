package com.wada.gesprek.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

import android.app.Activity;
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
	int numSequencia = 0;

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
			Log.d(CLIENT_TAG, "Minbuffersize: " + minBufSize);
			byte[] bufferTotal;
			byte[] bufferVoz = new byte[minBufSize];

			DatagramPacket packet;

			recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
					sampleRate, configuracaoCanalEntrada, formatoAudio,
					minBufSize);
			Log.d(CLIENT_TAG, "Recorder inicializado");

			recorder.startRecording();

			while (enviarAudio) {
				
				recorder.read(bufferVoz, 0, bufferVoz.length);

				// os 4 últimos bytes 0,1,2 e 3 são utilizados para representar o numero de sequencia do pacote
				ByteBuffer byteBuffer = ByteBuffer.allocate(4);
				byteBuffer.putInt(numSequencia);
				byte[] arrayAux = byteBuffer.array();
				
				bufferTotal = new byte[arrayAux.length + bufferVoz.length];
				System.arraycopy(arrayAux, 0, bufferTotal, 0, arrayAux.length);
				System.arraycopy(bufferVoz, 0, bufferTotal, arrayAux.length, bufferVoz.length);

				packet = new DatagramPacket(bufferTotal, bufferTotal.length,
						otherAddress, otherPort);

				try {
					datagramSocket.send(packet);
					if (numSequencia == Integer.MAX_VALUE) {
						Log.d(CLIENT_TAG, "enviada com numseq " + numSequencia);
						numSequencia = 0;
					} else {
						numSequencia++;
					}
//					Log.d(CLIENT_TAG, "Mensagem enviada para " + otherAddress
//							+ " na porta " + otherPort + " com numSeq " + numSequencia);
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
		int ultimoRecebido = -1;

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
			
			Log.d(CLIENT_TAG, "Minbuffersize: " + minBufSize);

			byte[] bufferTotal = new byte[minBufSize + 4];
			byte[] bufferVoz = new byte[minBufSize];

			speaker = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
					configuracaoCanalSaida, formatoAudio, minBufSize,
					AudioTrack.MODE_STREAM);
			
			speaker.play();
			Log.d(CLIENT_TAG, "Volume maximo: " + AudioTrack.getMaxVolume());

			while (!Thread.currentThread().isInterrupted()) {

				DatagramPacket pacote = new DatagramPacket(bufferTotal,
						bufferTotal.length);
				try {
					datagramSocket.receive(pacote);
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(CLIENT_TAG, "Erro ao receber pacote");
				}
//				Log.d(CLIENT_TAG, "Pacote recebido");s

				bufferTotal = pacote.getData();
//				Log.d(CLIENT_TAG, "Dados do pacote lidos no buffer");
				
				
				byte[] arraySeq = new byte[4];
				System.arraycopy(bufferTotal, 0, arraySeq, 0, arraySeq.length);
				
								int numSequenciaAtual = ByteBuffer.wrap(arraySeq).getInt();
				
//				Log.d(CLIENT_TAG, "ultimo recebido " + ultimoRecebido + ", valor atual " + numSequenciaAtual);
				
				if (ultimoRecebido >= Integer.MAX_VALUE - 100) {
					Log.d(CLIENT_TAG, "ultimoRecebido proximo a maxValue " + ultimoRecebido);
					ultimoRecebido = 0;
				}
				if (numSequenciaAtual < ultimoRecebido) {
					Log.d(CLIENT_TAG, "continuei pq " + numSequenciaAtual + " < " + ultimoRecebido);
					continue;
				}
				
				ultimoRecebido = numSequenciaAtual;
				Log.d(CLIENT_TAG, "NumSeq do pacote lido " + numSequenciaAtual);
				
				System.arraycopy(bufferTotal, arraySeq.length, bufferVoz, 0, bufferVoz.length);
				
				speaker.write(bufferVoz, 0, minBufSize);
				Log.d(CLIENT_TAG, "Escrevendo conteudo do buffer no speaker");

			}
		}

		public void tearDown() {
			Thread.currentThread().interrupt();
		}
	}

}
