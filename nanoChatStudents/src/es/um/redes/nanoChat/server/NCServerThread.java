package es.um.redes.nanoChat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import es.um.redes.nanoChat.messageML.NCMessage;
import es.um.redes.nanoChat.messageML.NCMessageChat;
import es.um.redes.nanoChat.messageML.NCMessageChatPrivado;
import es.um.redes.nanoChat.messageML.NCMessageControl;
import es.um.redes.nanoChat.messageML.NCMessageInfoRoom;
import es.um.redes.nanoChat.messageML.NCMessageNick;
import es.um.redes.nanoChat.messageML.NCMessageRoom;
import es.um.redes.nanoChat.messageML.NCMessageRoomsInfo;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;
import es.um.redes.nanoChat.server.roomManager.NCRoomManager;
import es.um.redes.nanoChat.server.roomManager.NCSalaManager;

/**
 * A new thread runs for each connected client
 */
public class NCServerThread extends Thread {

	private Socket socket = null;
	// Manager global compartido entre los Threads
	private NCServerManager serverManager = null;

	// Input and Output Streams
	private DataInputStream dis;
	private DataOutputStream dos;
	// Usuario actual al que atiende este Thread
	String user;
	// RoomManager actual (dependerá de la sala a la que entre el usuario)
	NCRoomManager roomManager;
	// Sala actual
	String currentRoom;

	// Inicialización de la sala
	public NCServerThread(NCServerManager manager, Socket socket) throws IOException {
		super("NCServerThread");
		this.socket = socket;
		this.serverManager = manager;
	}

	// Main loop
	public void run() {
		try {
			// Se obtienen los streams a partir del Socket
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			// En primer lugar hay que recibir y verificar el nick
			receiveAndVerifyNickname();
			// Mientras que la conexión esté activa entonces...
			while (true) {
				// Obtenemos el mensaje que llega y analizamos su código de
				// operación
				NCMessage message = NCMessage.readMessageFromSocket(dis);
				switch (message.getOpcode()) {
				// 1) si se nos pide la lista de salas se envía llamando a
				// sendRoomList();
				case NCMessage.OP_QUERY_ROOM:
					sendRoomList();
					break;
				// 2) Si se nos pide entrar en la sala entonces obtenemos el
				// RoomManager de la sala,
				case NCMessage.OP_ENTER_ROOM:
					NCMessageRoom room = (NCMessageRoom) message;
					roomManager = serverManager.enterRoom(user, room.getName(), socket);
					currentRoom = room.getName();
					if (roomManager != null) {
						// 2) notificamos al usuario que ha sido aceptado y procesamos mensajes con processRoomMessages()
						NCMessageControl msgresp = (NCMessageControl) NCMessage.makeControlMessage(NCMessage.OP_OK);
						dos.writeUTF(msgresp.toEncodedString());
						// una vez que entro en la sala: llamo a -> processRoomMenssage()
						processRoomMessages();
						
					// 2) Si el usuario no es aceptado en la sala entonces se le notifica al cliente
					} else {
						NCMessageControl msgresp = (NCMessageControl) NCMessage.makeControlMessage(NCMessage.OP_NO_OK);
						dos.writeUTF(msgresp.toEncodedString());
					}

					break;

				}
			}
		} catch (Exception e) {
			// If an error occurs with the communications the user is removed
			// from all the managers and the connection is closed
			System.out.println("* User " + user + " disconnected.");
			serverManager.leaveRoom(user, currentRoom);
			serverManager.removeUser(user);
		} finally {
			if (!socket.isClosed())
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
	}

	// Obtenemos el nick y solicitamos al ServerManager que verifique si está
	// duplicado
	private void receiveAndVerifyNickname() throws IOException {
		// La lógica de nuestro programa nos obliga a que haya un nick
		// registrado antes de proseguir
		// String nick = dis.readUTF();
		// if(serverManager.addUser(nick))
		// dos.writeUTF("OK");
		// else {dos.writeUTF("DUPLICATED");
		// System.out.println(nick + " Duplicado ");
		// }

		// Entramos en un bucle hasta comprobar que alguno de los nicks
		// proporcionados no está duplicado
		Boolean duplicated = true;
		while (duplicated) {
			// Extraer el nick del mensaje
			NCMessage msg = NCMessage.readMessageFromSocket(dis);
			NCMessageNick nick = (NCMessageNick) msg;
			// Validar el nick utilizando el ServerManager - addUser()
			// Contestar al cliente con el resultado (éxito o duplicado)
			if (serverManager.addUser(nick.getName())) {
				user = nick.getName();
				NCMessageControl msgresp = (NCMessageControl) NCMessage.makeControlMessage(NCMessage.OP_OK);
				dos.writeUTF(msgresp.toEncodedString());
				duplicated = false;
			}

			else {
				NCMessageControl msgreq = (NCMessageControl) NCMessage.makeControlMessage(NCMessage.OP_NO_OK);
				dos.writeUTF(msgreq.toEncodedString());
			}
		}

	}

	// Mandamos al cliente la lista de salas existentes
	private void sendRoomList() throws IOException {
		// La lista de salas debe obtenerse a partir del RoomManager y
		// después enviarse mediante su mensaje correspondiente
		ArrayList<NCRoomDescription> salas = serverManager.getRoomList();
		NCMessageRoomsInfo msgresp = (NCMessageRoomsInfo) NCMessage.makeRoomsInfoMessage(NCMessage.OP_LIST_ROOM, salas);
		dos.writeUTF(msgresp.toEncodedString());
		

	}

	private void processRoomMessages() throws IOException {
		// Comprobamos los mensajes que llegan hasta que el usuario decida
		// salir de la sala
		boolean exit = false;
		while (!exit) {
			// Se recibe el mensaje enviado por el usuario
			NCMessage message = NCMessage.readMessageFromSocket(dis);
			// Se analiza el código de operación del mensaje y se trata en consecuencia
			switch (message.getOpcode()) {
			case NCMessage.OP_EXIT_ROOM:
				serverManager.leaveRoom(user, currentRoom);
				exit = true;
				break;
			case NCMessage.OP_MESSAGE:
				NCSalaManager managerRoom = (NCSalaManager) serverManager.getManagerRoom(currentRoom);
				NCMessageChat msgIn = (NCMessageChat) message;
				managerRoom.broadcastMessage(user, msgIn.getName());
				break;
			case NCMessage.OP_MESSAGE_PRIVATE:
				managerRoom = (NCSalaManager) serverManager.getManagerRoom(currentRoom);
				NCMessageChatPrivado msgPri = (NCMessageChatPrivado) message;
				managerRoom.sendPrivateMessage(user, msgPri.getReceptor(), msgPri.getName());
				break;
			case NCMessage.OP_INFO_ROOM:
				NCRoomDescription info = serverManager.getRoomInfo(currentRoom);
				NCMessageInfoRoom msgresp = (NCMessageInfoRoom) NCMessage
						.makeInfoRoomMessage(NCMessage.OP_INFO_ROOM_REQUEST, info);
				dos.writeUTF(msgresp.toEncodedString());
				break;
			default:

				break;
			}
		}
	}

}
