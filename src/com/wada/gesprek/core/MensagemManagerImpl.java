package com.wada.gesprek.core;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.wada.gesprek.manager.MensagemManager;

public class MensagemManagerImpl implements MensagemManager{

	public void persist(Mensagem mensagem) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		String stringDateTime = fmt.print(mensagem.getData());
	}
	
}
