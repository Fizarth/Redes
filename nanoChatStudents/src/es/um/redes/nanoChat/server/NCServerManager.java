package es.um.redes.nanoChat.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;
import es.um.redes.nanoChat.server.roomManager.NCRoomManager;

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
	
	
	//Intenta registrar al usuario en el servidor.
	public synchronized boolean addUser(String user) {
		//TODO Devuelve true si no hay otro usuario con su nombre
		if(!users.contains(user)) {
			users.add(user);
			return true;
		}
		//TODO Devuelve false si ya hay un usuario con su nombre
		else return false;
	}
	
	//Elimina al usuario del servidor
	public synchronized void removeUser(String user) {
		//TODO Elimina al usuario del servidor
	}
	
	//Un usuario solicita acceso para entrar a una sala y registrar su conexión en ella
	public synchronized NCRoomManager enterRoom(String u, String room, Socket s) {
		//TODO Verificamos si la sala existe
		//TODO Decidimos qué hacer si la sala no existe (devolver error O crear la sala)
		//TODO Si la sala existe y si es aceptado en la sala entonces devolvemos el RoomManager de la sala
		return null;
	}
	
	//Un usuario deja la sala en la que estaba 
	public synchronized void leaveRoom(String u, String room) {
		//TODO Verificamos si la sala existe
		//TODO Si la sala existe sacamos al usuario de la sala
		//TODO Decidir qué hacer si la sala se queda vacía
	}
}
