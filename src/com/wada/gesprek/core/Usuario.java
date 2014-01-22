package com.wada.gesprek.core;

import java.io.Serializable;

public class Usuario implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Usuario usuario = null;

	private String nome;

	public Usuario(String nome) throws Exception {
		if (usuario != null) {
			throw new Exception("Usuário já existe!");
		}
		this.nome = nome;
		usuario = this;
	}
	
	public static Usuario getInstance() {
		if (usuario != null) {
			return usuario;
		}
		return null;
	}


	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nome == null) ? 0 : nome.hashCode());
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
		Usuario other = (Usuario) obj;
		if (nome == null) {
			if (other.nome != null)
				return false;
		} else if (!nome.equals(other.nome))
			return false;
		return true;
	}

}
