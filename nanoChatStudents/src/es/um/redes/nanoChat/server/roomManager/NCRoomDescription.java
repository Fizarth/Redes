package es.um.redes.nanoChat.server.roomManager;


import java.util.ArrayList;
import java.util.Date;

public class NCRoomDescription {
	//Campos de los que, al menos, se compone una descripción de una sala 
	public String roomName;
	public int maxMiembros;
	public ArrayList<String> members;
	public long timeLastMessage;
	
	//Constructor a partir de los valores para los campos
	public NCRoomDescription(String roomName, ArrayList<String> members, long timeLastMessage, int m) {
		this.roomName = roomName;
		this.members = members;
		this.timeLastMessage = timeLastMessage;
		this.maxMiembros = m; // usuarios de 1 a 5 de max
		
	}
		
	//Método que devuelve una representación de la Descripción lista para ser impresa por pantalla
	public String toPrintableString() {
		
		
		StringBuffer sb = new StringBuffer();
		sb.append("Room Name: "+roomName+"\t Members ("+members.size()+ "/"+maxMiembros+") : ");
		for (String member: members) {
			sb.append(member+" ");
		}
		if (timeLastMessage != 0)
			sb.append("\tLast Public message: "+new Date(timeLastMessage).toString());
		else 
			sb.append("\tLast message: not yet");
		return sb.toString();
	}
}
