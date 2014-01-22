package com.wada.gesprek.core;

import java.net.InetAddress;

public interface MensageiroCliente<T> {
	
	public void connectToMensageiroServer(InetAddress inetAddress, int port);
	
	public boolean isReceiverInicializado();
	
	public int getMyPort();
	
	public void falar();
	
	public void pararFalar();
	
	public void tearDown();

}
