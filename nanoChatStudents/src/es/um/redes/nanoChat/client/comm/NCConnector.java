package es.um.redes.nanoChat.client.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import es.um.redes.nanoChat.messageML.NCMessage;
import es.um.redes.nanoChat.messageML.NCMessageChat;
import es.um.redes.nanoChat.messageML.NCMessageChatPrivado;
import es.um.redes.nanoChat.messageML.NCMessageControl;
import es.um.redes.nanoChat.messageML.NCMessageInfoRoom;
import es.um.redes.nanoChat.messageML.NCMessageNick;
import es.um.redes.nanoChat.messageML.NCMessageRoom;
import es.um.redes.nanoChat.messageML.NCMessageRoomsInfo;
import es.um.redes.nanoChat.messageML.NCRoomMessage;
import es.um.redes.nanoChat.server.roomManager.InfoMensaje;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor de NanoChat
public class NCConnector {
	private Socket socket;
	protected DataOutputStream dos;
	protected DataInputStream dis;

	public NCConnector(InetSocketAddress serverAddress) throws UnknownHostException, IOException {
		// Se crea el socket a partir de la dirección proporcionada
		socket = new Socket(serverAddress.getAddress(), serverAddress.getPort()); 
		// Se extraen los streams de entrada y salida
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());

	}

	// Método para registrar el nick en el servidor. Nos informa sobre si la
	// inscripción se hizo con éxito o no.
	public boolean registerNickname_UnformattedMessage(String nick) throws IOException {
		// Funcionamiento resumido: SEND(nick) and RCV(NICK_OK) or RCV(NICK_DUPLICATED)
		// Enviamos una cadena con el nick por el flujo de salidays
		dos.writeUTF(nick);
		// Leemos la cadena recibida como respuesta por el flujo de entrada
		String respuesta = dis.readUTF();
		// Si la cadena recibida es NICK_OK entonces no está duplicado (en
		// función de ello modificar el return)
		if (respuesta.compareTo("OK") == 0)
			return true;
		else
			return false;
		


	}

	// Método para registrar el nick en el servidor. Nos informa sobre si la
	// inscripción se hizo con éxito o no.
	public boolean registerNickname(String nick) throws IOException {
		// Funcionamiento resumido: SEND(nick) and RCV(NICK_OK) or
		// RCV(NICK_DUPLICATED)
		
		// Creamos un mensaje de tipo NCMessageNick con opcode OP_NICK en el que
		// se inserte el nick
		NCMessageNick message = (NCMessageNick) NCMessage.makeNickMessage(NCMessage.OP_NICK, nick);
		// Obtenemos el mensaje de texto listo para enviar
		String rawMessage = message.toEncodedString();
		// Escribimos el mensaje en el flujo de salida, es decir, provocamos que
		// se envíe por la conexión TCP
		dos.writeUTF(rawMessage);
		// Leemos el mensaje recibido como respuesta por el flujo de
		// entrada
		NCMessage msg = NCMessage.readMessageFromSocket(dis);
		// Analizamos el mensaje para saber si está duplicado el nick
		// (modificar el return en consecuencia)
		if (msg.getOpcode() == NCMessage.OP_OK)
			return true;
		else
			return false;
	}

	// Método para obtener la lista de salas del servidor
	public ArrayList<NCRoomDescription> getRooms() throws IOException {
		// Funcionamiento resumido: SND(GET_ROOMS) and RCV(ROOM_LIST)
		NCMessageControl message = (NCMessageControl) NCMessage.makeControlMessage(NCMessage.OP_QUERY_ROOM);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);

		NCMessage msg = NCMessage.readMessageFromSocket(dis);

		NCMessageRoomsInfo me = (NCMessageRoomsInfo) msg;
		ArrayList<NCRoomDescription> rooms = new ArrayList<NCRoomDescription>();
		if (me.getOpcode() == NCMessage.OP_LIST_ROOM) {

			rooms = me.getRooms();
			return rooms;
		}

		else {
			return null;
		}
	}

	// Método para solicitar la entrada en una sala
	public boolean enterRoom(String room) throws IOException {
		// Funcionamiento resumido: SND(ENTER_ROOM<room>) and RCV(IN_ROOM) or
		// RCV(REJECT)
		NCMessageRoom msgSend = (NCMessageRoom) NCMessage.makeRoomMessage(NCMessage.OP_ENTER_ROOM, room);
		dos.writeUTF(msgSend.toEncodedString());

		NCMessage msgRev = NCMessage.readMessageFromSocket(dis);
		if (msgRev.getOpcode() == NCMessage.OP_OK) {
			return true;
		}
		else
			return false;
	}

	// Método para salir de una sala
	public void leaveRoom(String room) throws IOException {
		// Funcionamiento resumido: SND(EXIT_ROOM)
		NCMessageControl msgSend = (NCMessageControl) NCMessage.makeControlMessage(NCMessage.OP_EXIT_ROOM);
		dos.writeUTF(msgSend.toEncodedString());

	}

	// Método que utiliza el Shell para ver si hay datos en el flujo de entrada
	public boolean isDataAvailable() throws IOException {
		return (dis.available() != 0);
	}

	// IMPORTANTE!!
	// Es necesario implementar métodos para recibir y enviar mensajes de
	// chat a una sala
	public void enviarMensaje(String usuario, String mensajeChat) throws IOException {
		NCMessageChat msgSend = (NCMessageChat) NCMessage.makeChatMessage(NCMessage.OP_MESSAGE, usuario, mensajeChat);
		dos.writeUTF(msgSend.toEncodedString());
	}

	public void enviarMensaje(String usEmisor, String usReceptor, String mensajeChat) throws IOException {
		NCMessageChatPrivado msgSend = (NCMessageChatPrivado) NCMessage
				.makeChatMessagePrivate(NCMessage.OP_MESSAGE_PRIVATE, usEmisor, usReceptor, mensajeChat);
		dos.writeUTF(msgSend.toEncodedString());

	}

	public InfoMensaje recibirMensaje() throws IOException {
		InfoMensaje info;

		NCMessage msgRev = NCMessage.readMessageFromSocket(dis);
		if (msgRev.getOpcode() == NCMessage.OP_MESSAGE) {
			NCMessageChat me = (NCMessageChat) msgRev;
			info = new InfoMensaje(me.getUser(), me.getName(), false);
			return info;
		} else if (msgRev.getOpcode() == NCMessage.OP_MESSAGE_PRIVATE) {
			NCMessageChatPrivado me = (NCMessageChatPrivado) msgRev;
			info = new InfoMensaje(me.getEmisor(), me.getName(), true);
			return info;
		} else if (msgRev.getOpcode() == NCMessage.OP_NO_OK) {
			return null;
		} else {
			System.out.println("ERROR: recibir mensaje");
			return null;
		}

	}

	// Método para pedir la descripción de una sala
	public NCRoomDescription getRoomInfo(String room) throws IOException {
		// Funcionamiento resumido: SND(GET_ROOMINFO) and RCV(ROOMINFO)

		// Construimos el mensaje de solicitud de información de la sala específica
		NCMessageRoom message = (NCMessageRoom) NCMessage.makeRoomMessage(NCMessage.OP_INFO_ROOM, room);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);

		// Recibimos el mensaje de respuesta
		NCRoomDescription info;
		NCMessage msgRev = NCMessage.readMessageFromSocket(dis);

		// Devolvemos la descripción contenida en el mensaje
		if (msgRev.getOpcode() == NCMessage.OP_INFO_ROOM_REQUEST) {
			NCMessageInfoRoom me = (NCMessageInfoRoom) msgRev;
			info = new NCRoomDescription(me.getRoomInfo().roomName, me.getRoomInfo().members,
					me.getRoomInfo().timeLastMessage, me.getRoomInfo().maxMiembros);
			return info;
		}
		
		return null;
	}

	// Método para cerrar la comunicación con la sala
	// (Opcional) Enviar un mensaje de salida del servidor de Chat
	public void disconnect() {
		NCMessageControl msgSend = (NCMessageControl) NCMessage.makeControlMessage(NCMessage.OP_EXIT);
		try {
			if (socket != null) {
				dos.writeUTF(msgSend.toEncodedString());
				socket.close();
			}
		} catch (IOException e) {
		} finally {
			socket = null;
		}
	}

}
