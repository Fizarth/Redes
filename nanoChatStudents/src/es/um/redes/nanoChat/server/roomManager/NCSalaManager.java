package es.um.redes.nanoChat.server.roomManager;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class NCSalaManager extends NCRoomManager {
	
	//Superclase String roomName
	NCRoomDescription roomDescription ;
		/* 	roomName; members; timeLastMessage; */	
	private HashMap<String, Socket> miembros;


	public NCSalaManager() {
		this.miembros = new HashMap<String,Socket>();	
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
		// TODO Auto-generated method stub
		for(String usur : roomDescription.members) {
			if (usur.compareTo(u)!=0) {
				NCMessageChat mensage = new
				
			}
			
		}
		
	}

	@Override
	public void removeUser(String u) {
		this.miembros.remove(u);
	}

	@Override
	public void setRoomName(String roomName) {
		this.roomName = roomName;
		
	}

	@Override
	public NCRoomDescription getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int usersInRoom() {
		return this.miembros.size();
	}

}
