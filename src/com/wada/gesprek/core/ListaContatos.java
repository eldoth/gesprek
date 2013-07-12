package com.wada.gesprek.core;

import java.util.HashSet;
import java.util.Set;

public class ListaContatos {

	private Long id;
	private Usuario usuario;
	private Set<Contato> contatos;

	public ListaContatos(Usuario usuario) {
		this.usuario = usuario;
		this.contatos = new HashSet<Contato>();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Set<Contato> getContatos() {
		return contatos;
	}

	public void setContatos(Set<Contato> contatos) {
		this.contatos = contatos;
	}

	public Boolean addContato(Contato contato) {
		if (contatos.contains(contato)) {
			return false;
		}
		this.contatos.add(contato);
		return true;
	}

	public Boolean removeContato(Contato contato) {
		if (!contatos.contains(contato)) {
			return false;
		}
		contatos.remove(contato);
		return true;
	}

}
