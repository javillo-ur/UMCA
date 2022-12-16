package model;

public class StatusRep{
	public static String statusToString(Status s) {
		switch(s) {
			case InGame:
				return "en juego";
			case Waiting:
				return "esperando jugadores";
			case Finished:
				return "juego terminado";
			default:
				return null;
		}
	}
}