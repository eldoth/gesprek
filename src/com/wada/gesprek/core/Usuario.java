package com.wada.gesprek.core;

import java.util.Set;

public class Usuario {

	private Long id;
	private String nome;
	private String senha;
	private String identificador;
	private Set<Usuario> contatos;
	private Boolean status;
	private Boolean bloqueado;
	
	public Usuario(String nome, String identificador) {
		this.nome = nome;
		this.identificador = identificador;
		this.status = false;
		this.bloqueado = false;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getIdentificador() {
		return identificador;
	}

	public void setIdentificador(String identificador) {
		this.identificador = identificador;
	}

	public Set<Usuario> getContatos() {
		return contatos;
	}

	public void setContatos(Set<Usuario> contatos) {
		this.contatos = contatos;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public boolean isBloqueado() {
		return bloqueado;
	}

	public void setBloqueado(boolean bloqueado) {
		this.bloqueado = bloqueado;
	}

}
