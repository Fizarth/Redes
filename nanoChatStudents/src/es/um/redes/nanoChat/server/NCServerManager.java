package es.um.redes.nanoChat.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import es.um.redes.nanoChat.server.roomManager.InfoRoom;
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
	
	//Método para registrar un RoomManager 
	public void registerRoomManager(NCRoomManager rm) {
		//TODO Dar soporte para que pueda haber más de una sala en el servidor
		String roomName = ROOM_PREFIX + (char) nextRoom; 
		rooms.put(roomName, rm);
		rm.setRoomName(roomName);
	}
	
	//Devuelve la descripción de las salas existentes
	public synchronized ArrayList<NCRoomDescription> getRoomList() {
		//TODO Pregunta a cada RoomManager cuál es la descripción actual de su sala
		//TODO Añade la información al ArrayList
		return null;
	}
	//-----Creada por nosotras para devolver la lista de salas
	public synchronized ArrayList<InfoRoom> getRoomsInfo() {
		//TODO Pregunta a cada RoomManager cuál es la descripción actual de su sala
		ArrayList<InfoRoom> salas = new ArrayList<>();
		for(NCRoomManager r: rooms.values()){
			//System.out.println("NCServerManager getInfo "+r.getInfo().name+" "+r.getInfo().miembros);
			//TODO Añade la información al ArrayList
			salas.add(r.getInfo());
		}
		
		return salas;
	}
	
	
	//Intenta registrar al usuario en el servidor.
	public synchronized boolean addUser(String user) {
		//TODO Devuelve true si no hay otro usuario con su nombre
		if(!users.contains(user)) {
			users.add(user);
			return true;
		}
		//TODO Devuelve false si ya hay un usuario con su nombre
		 return false;
	}
	
	//Elimina al usuario del servidor
	public synchronized void removeUser(String user) {
		//TODO Elimina al usuario del servidor
		users.remove(user);
	}
	
	//Un usuario solicita acceso para entrar a una sala y registrar su conexión en ella
	public synchronized NCRoomManager enterRoom(String u, String room, Socket s) {
		//TODO Verificamos si la sala existe
//		System.out.println("NCServerManager contiene "+ room +" : "+rooms.containsKey(room));
		if (rooms.containsKey(room)) {
//			NCRoomManager manager = rooms.get(room);
			NCSalaManager manager = (NCSalaManager) rooms.get(room);
			//---TODO suponemos ahora mismo que entra siempre.
			boolean registrado=manager.registerUser(u,s);
			if(registrado)System.out.println("NCServerManager añade usuario " + u+ " a sala "+room);
			else System.out.println("No se ha podido registrar usuario " + u+ " en sala "+room);
				return manager;
		}
		
		//TODO Decidimos qué hacer si la sala no existe (devolver error O crear la sala)
		else{ //Si no existe la sala, la crea
			NCSalaManager manager = new NCSalaManager(room);
			rooms.put(room, manager);
			manager.registerUser(u, s);
			System.out.println("NCServerManager crea la sala "+room+" y añade al usuario "+u);
			return manager;
			
//			rooms.put(room, value);
		}
		//TODO Si la sala existe y si es aceptado en la sala entonces devolvemos el RoomManager de la sala
		
	}
	
	//Un usuario deja la sala en la que estaba 
	public synchronized void leaveRoom(String u, String room) {
		//TODO Verificamos si la sala existe
		if(rooms.containsKey(room)) {
			NCRoomManager ncManager = rooms.get(room);
			ncManager.removeUser(u);
		}
		//TODO Si la sala existe sacamos al usuario de la sala
		//TODO Decidir qué hacer si la sala se queda vacía
		
		//la dejamos vacia.
	}
	
}
