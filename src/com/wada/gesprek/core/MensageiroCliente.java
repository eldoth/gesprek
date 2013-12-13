package com.wada.gesprek.core;

public interface MensageiroCliente<T> {
	
	public void tearDown();
	
	public void sendMessage(T msg);

}
