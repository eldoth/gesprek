package com.wada.gesprek.core;

import android.net.nsd.NsdServiceInfo;


public class Contato {
	

	private Long id;
	private Boolean status;
	private ListaContatos listaContatos;
	private NsdServiceInfo nsdServiceInfo;
	
	public Contato(NsdServiceInfo service) {
		this.nsdServiceInfo = service;
		Usuario usuario = Usuario.getInstance();
		this.listaContatos = usuario.getListaContatos();
		this.status = true;
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
	
	public NsdServiceInfo getNsdServiceInfo() {
		return nsdServiceInfo;
	}

	public void setNsdServiceInfo(NsdServiceInfo nsdServiceInfo) {
		this.nsdServiceInfo = nsdServiceInfo;
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
		return true;
	}
	
	public String toString() {
		return this.nsdServiceInfo.getHost().getHostAddress();
	}

}
