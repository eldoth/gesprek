package com.wada.gesprek.core;

import org.joda.time.DateTime;

public class Mensagem {

	private Long id;
	private DateTime data;
	private Usuario emissor;
	private Usuario receptor;
	// TODO: Mudar para o tipo que é o AUDIO
	private String conteudo;
	
	public Mensagem(Usuario emissor, Usuario receptor) {
		data = new DateTime();
		this.emissor = emissor;
		this.receptor = receptor;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DateTime getData() {
		return data;
	}

	public void setData(DateTime data) {
		this.data = data;
	}

	public Usuario getEmissor() {
		return emissor;
	}

	public void setEmissor(Usuario emissor) {
		this.emissor = emissor;
	}

	public Usuario getReceptor() {
		return receptor;
	}

	public void setReceptor(Usuario receptor) {
		this.receptor = receptor;
	}

	public String getConteudo() {
		return conteudo;
	}

	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}

}
