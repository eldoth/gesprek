package com.wada.gesprek.core;

import java.io.Serializable;
import java.net.InetAddress;

import android.net.nsd.NsdServiceInfo;


/**
 * @author leowada
 *
 */
public class Contato implements Serializable{
	
	/** 
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String nome;
	
	private String serviceName;
    private String serviceType;
    private InetAddress host;
    private int port;

	
	public Contato(NsdServiceInfo service) {
		this.serviceName = service.getServiceName();
		this.serviceType = service.getServiceType();
		this.host = service.getHost();
		this.port = service.getPort();
		Usuario usuario = Usuario.getInstance();
		this.nome = service.getServiceName().split(";")[1].replace("\\032", " ");
	}
	
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public InetAddress getHost() {
		return host;
	}

	public void setHost(InetAddress host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public void setNsdServiceInfo(NsdServiceInfo serviceInfo) {
		this.serviceName = serviceInfo.getServiceName();
		this.serviceType = serviceInfo.getServiceType();
		this.host = serviceInfo.getHost();
		this.port = serviceInfo.getPort();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Contato other = (Contato) obj;
		if(this.getNome() != other.getNome()) {
			return false;
		}
		return true;
	}
	
	public String toString() {
		return nome;
	}

}
