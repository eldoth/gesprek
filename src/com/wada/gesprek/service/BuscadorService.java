package com.wada.gesprek.service;

import java.net.InetAddress;
import java.net.Socket;

public interface BuscadorService {

	public static final String TAG = "Buscador";

	public InetAddress findIpLocal();

	/**
	 * Método responsável por criar o servidor do buscador, ou seja, um servidor
	 * que disponibiliza o dispositivo na rede para uma possível conexão com
	 * outro dispositivo.
	 */
	public void startServer();

	public InetAddress getServerIp();

	public void setServerIp(InetAddress serverIp);

	public Socket getSocket();

	public void setSocket(Socket socket);
	
	public void tearDown();

}
