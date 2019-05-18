package es.um.redes.nanoChat.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;
import es.um.redes.nanoChat.server.roomManager.NCRoomManager;
import es.um.redes.nanoChat.server.roomManager.NCSalaManager;

/**
 * Esta clase contiene el estado general del servidor (sin la lógica relacionada con cada sala particular)
 */
class NCServerManager {
	
	//Primera habitación del servidor
	final static byte INITIAL_ROOM = 'A';
	final static String ROOM_PREFIX = "Room";
	//Siguiente habitación que se creará
	byte nextRoom;
	//Usuarios registrados en el servidor
	private HashSet<String> users = new HashSet<String>();
	//Habitaciones actuales asociadas a sus correspondientes RoomManagers
	private HashMap<String,NCRoomManager> rooms = new HashMap<String,NCRoomManager>();
	
	NCServerManager() {
		nextRoom = INITIAL_ROOM;
	}
	
	public NCRoomManager getManagerRoom(String room) {
		return this.rooms.get(room);
	}
	
	//Método para registrar un RoomManager 
	public void registerRoomManager(NCRoomManager rm) {
		//Dar soporte para que pueda haber más de una sala en el servidor
		String roomName = ROOM_PREFIX + (char) nextRoom; 
		rooms.put(roomName, rm);
		rm.setRoomName(roomName);
	}
	
	//Devuelve la descripción de las salas existentes
	public synchronized ArrayList<NCRoomDescription> getRoomList() {
		//Pregunta a cada RoomManager cuál es la descripción actual de su sala
				ArrayList<NCRoomDescription> salas = new ArrayList<>();
				for(NCRoomManager r: rooms.values()){
//					System.out.println("NCServerManager getInfo "+r.getDescription().roomName+" "+r.getDescription().members.toString());
					//Añade la información al ArrayList
					salas.add(r.getDescription());
				}
				
				return salas;
	}
	
	
	public synchronized NCRoomDescription getRoomInfo(String room) {
		//Pregunta a cada RoomManager cuál es la descripción actual de su sala
		for(NCRoomManager r: rooms.values()){
			//System.out.println("NCServerManager getInfo "+r.getInfo().name+" "+r.getInfo().miembros);
			//Añade la información al ArrayList
			if(r.getDescription().roomName.compareTo(room)==0)
				return r.getDescription();
		}
		
		return null;
	}
	
	
	//Intenta registrar al usuario en el servidor.
	public synchronized boolean addUser(String user) {
		//Devuelve true si no hay otro usuario con su nombre
		if(!users.contains(user)) {
			users.add(user);
			return true;
		}
		//Devuelve false si ya hay un usuario con su nombre
		 return false;
	}
	
	//Elimina al usuario del servidor
	public synchronized void removeUser(String user) {
		//Elimina al usuario del servidor
		users.remove(user);
	}
	
	//Un usuario solicita acceso para entrar a una sala y registrar su conexión en ella
	public synchronized NCRoomManager enterRoom(String u, String room, Socket s) {
		//Verificamos si la sala existe
//		System.out.println("NCServerManager contiene "+ room +" : "+rooms.containsKey(room));
		if (rooms.containsKey(room)) {
			
//			NCRoomManager manager = rooms.get(room);
			NCSalaManager manager = (NCSalaManager) rooms.get(room);
			
			if(manager.getDescription().members.size() >= manager.getDescription().maxMiembros)
				return null;
			
			boolean registrado=manager.registerUser(u,s);
//			if(registrado)System.out.println("NCServerManager añade usuario " + u+ " a sala "+room);
//			else System.out.println("No se ha podido registrar usuario " + u+ " en sala "+room);
				return manager;
		}
		
		//Decidimos qué hacer si la sala no existe (devolver error O crear la sala) ->Si no existe la sala, la crea
		else{ 
			NCSalaManager manager = new NCSalaManager(room);
			rooms.put(room, manager);
			manager.registerUser(u, s);
//			System.out.println("NCServerManager crea la sala "+room+" y añade al usuario "+u);
			return manager;
			
		}
	}
	
	//Un usuario deja la sala en la que estaba 
	public synchronized void leaveRoom(String u, String room) {
		//Verificamos si la sala existe
		if(rooms.containsKey(room)) {
			NCSalaManager manager = (NCSalaManager) rooms.get(room);
			//Si la sala existe sacamos al usuario de la sala
			manager.removeUser(u);
			//Decidir qué hacer si la sala se queda vacía -> la dejamos vacia.
		}
		
		
	}
	
}
