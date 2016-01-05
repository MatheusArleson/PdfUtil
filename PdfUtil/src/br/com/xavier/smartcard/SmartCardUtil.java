package br.com.xavier.smartcard;

import java.util.List;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CardTerminals.State;
import javax.smartcardio.TerminalFactory;

public class SmartCardUtil {
	private static final int READER_READY = 0;
	private static final String READER_READY_STR = new String("Leitor Pronto. Insira a senha.");
	
	private static final int READER_WITHOUT_CARD = 1;
	private static final String READER_WITHOUT_CARD_STR = new String("Insira o cart\u00E3o no leitor.");
	
	private static final int MULTIPLE_READERS_CONNECTED = 2;
	private static final String MULTIPLE_READERS_CONNECTED_STR = new String("Mais de um leitor conectado.");
	
	public static final int NO_READER_CONNECTED = 4;
	private static final String NO_READER_CONNECTED_STR = new String("Nenhum Leitor Conectado.");
	
	private SmartCardUtil(){}
	
	private static TerminalFactory initializeSmartCardTerminalsService(){
		return TerminalFactory.getDefault();
	}
	
	private static CardTerminals getSmartCardPluggedTerminals(){
		return initializeSmartCardTerminalsService().terminals();
	}
	
	private static int getNumberOfSmartCardPluggedTerminals() throws CardException{
		return getSmartCardPluggedTerminals().list().size();
	}
	
	private static List<CardTerminal> getPluggedTerminalsWithSmartCards() throws CardException{
		return getSmartCardPluggedTerminals().list(State.CARD_PRESENT);
	}
	
	private static int getNumberOfSmartCardPluggedTerminalsWithSmartCards() throws CardException{
		return getPluggedTerminalsWithSmartCards().size();
	}
	
//	private List<CardTerminal> getPluggedTerminalsWithoutSmartCards() throws CardException{
//		return getSmartCardPluggedTerminals().list(State.CARD_ABSENT);
//	}
	
//	private int getNumberOfSmartCardPluggedTerminalsWithoutSmartCards() throws CardException{
//		return getPluggedTerminalsWithoutSmartCards().size();
//	}
	
	private static int getSmartCardStatus() {
		try {
			if(getNumberOfSmartCardPluggedTerminals() == 0){
				return NO_READER_CONNECTED;
			}else if (getNumberOfSmartCardPluggedTerminals() > 1) {
				return MULTIPLE_READERS_CONNECTED;
			}else if (getNumberOfSmartCardPluggedTerminalsWithSmartCards() == 0){
				return READER_WITHOUT_CARD;
			}
			return READER_READY;
		} catch(CardException e){
			return NO_READER_CONNECTED;
		}
	}
	
	public static boolean isSmartCardReady(){
		if(getSmartCardStatus() == READER_READY){
			return true;
		}
		return false;
	}
	
	public static String getSmartCardStatusString(){
		switch (getSmartCardStatus()) {
		case SmartCardUtil.READER_READY:
			return READER_READY_STR;
		case SmartCardUtil.READER_WITHOUT_CARD:
			return READER_WITHOUT_CARD_STR;
		case SmartCardUtil.MULTIPLE_READERS_CONNECTED:
			return MULTIPLE_READERS_CONNECTED_STR;
		case SmartCardUtil.NO_READER_CONNECTED:
			return NO_READER_CONNECTED_STR;
		}
		return new String("ERRO");
	}
}
