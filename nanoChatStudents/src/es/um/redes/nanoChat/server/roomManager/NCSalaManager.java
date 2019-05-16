package es.um.redes.nanoChat.server.roomManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;


import es.um.redes.nanoChat.messageML.NCMessage;
import es.um.redes.nanoChat.messageML.NCMessageChat;
import es.um.redes.nanoChat.messageML.NCMessageChatPrivado;
import es.um.redes.nanoChat.messageML.NCMessageControl;

public class NCSalaManager extends NCRoomManager {
	
	//Superclase String roomName
	NCRoomDescription roomDescription = null;
		/* 	roomName; members; timeLastMessage; */
	private HashMap<String, Socket> miembros;
	
	

	public NCSalaManager(String roomName) {
		this.roomName=roomName;
		this.miembros = new HashMap<String,Socket>();	
		ArrayList<String> members = new ArrayList<String>();
		int tamano = (int)(Math.random()*6+2);
		this.roomDescription = new NCRoomDescription(roomName, members, 0, tamano );
		
	}
	
	@Override
	public boolean registerUser(String u, Socket s) {
//		if (this.miembros.put(u, s) != null) { // ESTO SE PUEDE HACER???
		if (!this.miembros.keySet().contains(u)) { 
			this.roomDescription.members.add(u);
			//-------
			this.miembros.put(u,s);
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
				NCMessageChat mensaje = new NCMessageChat(NCMessage.OP_MESSAGE,u ,message);
				dos.writeUTF(mensaje.toEncodedString());
			}			
		}
		this.roomDescription.timeLastMessage = System.currentTimeMillis();
	}

	@Override
	public void removeUser(String u) {
		if(this.miembros.containsKey(u)) {
			this.roomDescription.members.remove(u);
			this.miembros.remove(u);
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

	@Override
	public void sendPrivateMessage(String emisor, String receptor, String message) throws IOException {
		boolean receptorEncontrado=false;
		for(String usur : roomDescription.members) {
			if (usur.compareTo(emisor)!=0) {
				if(usur.compareTo(receptor)==0){
					receptorEncontrado=true;
					Socket s = this.miembros.get(receptor);
					DataOutputStream dos = new DataOutputStream(s.getOutputStream());
					NCMessageChatPrivado mensaje = new NCMessageChatPrivado(NCMessage.OP_MESSAGE_PRIVATE,emisor,receptor ,message);
					dos.writeUTF(mensaje.toEncodedString());
				} 
			}			
		}if(!receptorEncontrado){
			System.out.println("Usuario "+receptor+" no esta en la sala");
			Socket s = this.miembros.get(emisor);
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			NCMessageControl control= new NCMessageControl(NCMessage.OP_NO_OK);
			dos.writeUTF(control.toEncodedString());
			
		}
		
	}
	
	
}
