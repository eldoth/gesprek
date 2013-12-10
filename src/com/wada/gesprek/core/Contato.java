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
	
	private Long id;
	private Boolean status;
	private ListaContatos listaContatos;
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
		this.listaContatos = usuario.getListaContatos();
		this.status = true;
		this.nome = service.getServiceName().split(";")[1];
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public ListaContatos getListaContatos() {
		return listaContatos;
	}

	public void setListaContatos(ListaContatos listaContatos) {
		this.listaContatos = listaContatos;
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
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if(this.getNome() != other.getNome()) {
			return false;
		}
		return true;
	}
	
	public String toString() {
		return nome;
	}

}
