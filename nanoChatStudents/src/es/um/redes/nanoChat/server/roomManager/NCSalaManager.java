package es.um.redes.nanoChat.server.roomManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import es.um.redes.nanoChat.messageML.NCMessage;
import es.um.redes.nanoChat.messageML.NCMessageChat;

public class NCSalaManager extends NCRoomManager {
	
	//Superclase String roomName
	NCRoomDescription roomDescription = null;
		/* 	roomName; members; timeLastMessage; */	
	private HashMap<String, Socket> miembros;


	public NCSalaManager() {
		this.miembros = new HashMap<String,Socket>();	
		ArrayList<String> members = new ArrayList<String>();
		this.roomDescription = new NCRoomDescription(roomName, members, 0);
	}
	
	@Override
	public boolean registerUser(String u, Socket s) {
		if (this.miembros.put(u, s) != null) { // ESTO SE PUEDE HACER???
			this.roomDescription.members.add(u);
			return true;
		}
		return false;
	}

	@Override
	public void broadcastMessage(String u, String message) throws IOException {
		for(String usur : roomDescription.members) {
			if (usur.compareTo(u)!=0) {
				Socket s = this.miembros.get(usur);
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				NCMessageChat mensaje = new NCMessageChat(NCMessage.OP_MESSAGE,message);
				dos.writeUTF(mensaje.toEncodedString());
			}			
		}
		this.roomDescription.timeLastMessage = System.currentTimeMillis();
	}

	@Override
	public void removeUser(String u) {
		if(this.miembros.remove(u) != null) {
			this.roomDescription.members.remove(u);
		}
	}

	@Override
	public void setRoomName(String roomName) {
		this.roomName = roomName;
		this.roomDescription.roomName = roomName;
		
	}

	@Override
	public NCRoomDescription getDescription() {
		return this.roomDescription;
			
	}

	@Override
	public int usersInRoom() {
		return this.miembros.size();
	}

}
