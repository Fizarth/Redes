package es.um.redes.nanoChat.client.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import es.um.redes.nanoChat.messageML.NCMessage;
import es.um.redes.nanoChat.messageML.NCMessageControl;
import es.um.redes.nanoChat.messageML.NCMessageInfoRoom;
import es.um.redes.nanoChat.messageML.NCMessageNick;
import es.um.redes.nanoChat.messageML.NCMessageRoom;
import es.um.redes.nanoChat.messageML.NCMessageRoomsInfo;
import es.um.redes.nanoChat.messageML.NCRoomMessage;
import es.um.redes.nanoChat.server.roomManager.InfoRoom;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor de NanoChat
public class NCConnector {
	private Socket socket;
	protected DataOutputStream dos;
	protected DataInputStream dis;
	
	public NCConnector(InetSocketAddress serverAddress) throws UnknownHostException, IOException {
		//TODO Se crea el socket a partir de la dirección proporcionada 
		socket = new Socket(serverAddress.getAddress(), serverAddress.getPort());  //si en vez de crearlo vacio le pasamos serverAddress no hace falta poner el bind despues
		//socket.bind(serverAddress);
		//TODO Se extraen los streams de entrada y salida
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
	}


	//Método para registrar el nick en el servidor. Nos informa sobre si la inscripción se hizo con éxito o no.
	public boolean registerNickname_UnformattedMessage(String nick) throws IOException {
		//Funcionamiento resumido: SEND(nick) and RCV(NICK_OK) or RCV(NICK_DUPLICATED)
		
		dos.writeUTF(nick);
		String respuesta = dis.readUTF();
		if(respuesta.compareTo("OK")==0)return true;
		else return false;
		//TODO Enviamos una cadena con el nick por el flujo de salidays
		//TODO Leemos la cadena recibida como respuesta por el flujo de entrada 
		//TODO Si la cadena recibida es NICK_OK entonces no está duplicado (en función de ello modificar el return)
		
	}

	
	//Método para registrar el nick en el servidor. Nos informa sobre si la inscripción se hizo con éxito o no.
	public boolean registerNickname(String nick) throws IOException {
		//Funcionamiento resumido: SEND(nick) and RCV(NICK_OK) or RCV(NICK_DUPLICATED)
		//Creamos un mensaje de tipo RoomMessage con opcode OP_NICK en el que se inserte el nick
		
		NCMessageNick message = (NCMessageNick) NCMessage.makeNickMessage(NCMessage.OP_NICK, nick);
		//Obtenemos el mensaje de texto listo para enviar
		String rawMessage = message.toEncodedString();
		//Escribimos el mensaje en el flujo de salida, es decir, provocamos que se envíe por la conexión TCP
		dos.writeUTF(rawMessage);
		//TODO Leemos el mensaje recibido como respuesta por el flujo de entrada 
		NCMessage msg = NCMessage.readMessageFromSocket(dis);
		//TODO Analizamos el mensaje para saber si está duplicado el nick (modificar el return en consecuencia)	
		if(msg.getOpcode()== NCMessage.OP_OK)return true;
		else return false;
	}
	
	//Método para obtener la lista de salas del servidor
	/*HEMOS PENSADO QUE EN LUGAR 
	 * 		public ArrayList<NCRoomDescription> getRooms() 
	 * Devolver INFO ROOM, ya que eso contenia inf que considerabamos innecesaria.
	 * */
	public ArrayList<InfoRoom> getRooms() throws IOException {
		//Funcionamiento resumido: SND(GET_ROOMS) and RCV(ROOM_LIST)
		NCMessageControl message = (NCMessageControl) NCMessage.makeControlMessage(NCMessage.OP_QUERY_ROOM);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);
		
		NCMessage msg = NCMessage.readMessageFromSocket(dis);
		NCMessageRoomsInfo me = (NCMessageRoomsInfo) msg;
		ArrayList<InfoRoom> rooms = new ArrayList<InfoRoom>();
		if(me.getOpcode() == NCMessage.OP_LIST_ROOM) {
			
			//--TODO dependiendo del mensaje que creemos hacer.
			rooms=me.getRooms();
//			for(InfoRoom i: rooms){
//				System.out.println("NCConector-getRooms-> "+i.name+" "+i.maxMiembros+" "+i.miembros);
//			}
			return rooms;
		}
		
		else{
			//System.out.println(" NCConnector- getRooms: error");
			return null;
		}
	}
	
	//Método para solicitar la entrada en una sala
	public boolean enterRoom(String room) throws IOException {
		//Funcionamiento resumido: SND(ENTER_ROOM<room>) and RCV(IN_ROOM) or RCV(REJECT)
		NCMessageRoom msgSend = (NCMessageRoom) NCMessage.makeRoomMessage(NCMessage.OP_ENTER_ROOM, room);
		dos.writeUTF(msgSend.toEncodedString());
		
		NCMessage msgRev = NCMessage.readMessageFromSocket(dis);
		if (msgRev.getOpcode()== NCMessage.OP_OK) {
			return true;
		}
		//TODO completar el método
		else return false;
	}
	
	//Método para salir de una sala
	public void leaveRoom(String room) throws IOException {
		//Funcionamiento resumido: SND(EXIT_ROOM)
		NCMessageControl msgSend = (NCMessageControl) NCMessage.makeControlMessage(NCMessage.OP_EXIT_ROOM);
		dos.writeUTF(msgSend.toEncodedString());
		
	}
	
	//Método que utiliza el Shell para ver si hay datos en el flujo de entrada
	public boolean isDataAvailable() throws IOException {
		return (dis.available() != 0);
	}
	
	//IMPORTANTE!!
	//TODO Es necesario implementar métodos para recibir y enviar mensajes de chat a una sala
	
	//Método para pedir la descripción de una sala
	public NCRoomDescription getRoomInfo(String room) throws IOException {
		//Funcionamiento resumido: SND(GET_ROOMINFO) and RCV(ROOMINFO)
		
		NCMessageRoom message = (NCMessageRoom ) NCMessage.makeRoomMessage(NCMessage.OP_INFO_ROOM,room);
		String rawMessage = message.toEncodedString();
		dos.writeUTF(rawMessage);
		NCRoomDescription info;
		NCMessage msgRev = NCMessage.readMessageFromSocket(dis);
		if(msgRev.getOpcode() == NCMessage.OP_INFO_ROOM_REQUEST) {
			NCMessageInfoRoom me = (NCMessageInfoRoom) msgRev;
			//--TODO dependiendo del mensaje que creemos hacer.
			info = new NCRoomDescription(me.getName(),me.getNombresUsers(),10); //TODO---- cambiar el 10 por tiempo ultimo msg
			return info;
		}
		
		//TODO Construimos el mensaje de solicitud de información de la sala específica
		//TODO Recibimos el mensaje de respuesta
		//TODO Devolvemos la descripción contenida en el mensaje
		return null;
	}
	
	//Método para cerrar la comunicación con la sala
	//TODO (Opcional) Enviar un mensaje de salida del servidor de Chat
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
