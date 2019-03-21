package es.um.redes.nanoChat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import es.um.redes.nanoChat.client.shell.NCCommands;
import es.um.redes.nanoChat.messageML.NCMessage;
import es.um.redes.nanoChat.messageML.NCMessageControl;
import es.um.redes.nanoChat.messageML.NCMessageNick;
import es.um.redes.nanoChat.server.roomManager.NCRoomManager;

/**
 * A new thread runs for each connected client
 */
public class NCServerThread extends Thread {
	
	private Socket socket = null;
	//Manager global compartido entre los Threads
	private NCServerManager serverManager = null;
	//Input and Output Streams
	private DataInputStream dis;
	private DataOutputStream dos;
	//Usuario actual al que atiende este Thread
	String user;
	//RoomManager actual (dependerá de la sala a la que entre el usuario)
	NCRoomManager roomManager;
	//Sala actual
	String currentRoom;

	//Inicialización de la sala
	public NCServerThread(NCServerManager manager, Socket socket) throws IOException {
		super("NCServerThread");
		this.socket = socket;
		this.serverManager = manager;
	}

	//Main loop
	public void run() {
		try {
			//Se obtienen los streams a partir del Socket
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			//En primer lugar hay que recibir y verificar el nick
			receiveAndVerifyNickname();
			//Mientras que la conexión esté activa entonces...
			while (true) {
				//TODO Obtenemos el mensaje que llega y analizamos su código de operación
				NCMessage message = NCMessage.readMessageFromSocket(dis);
				switch (message.getOpcode()) {
				//TODO 1) si se nos pide la lista de salas se envía llamando a sendRoomList();
				//TODO 2) Si se nos pide entrar en la sala entonces obtenemos el RoomManager de la sala,
				//TODO 2) notificamos al usuario que ha sido aceptado y procesamos mensajes con processRoomMessages()
				//TODO 2) Si el usuario no es aceptado en la sala entonces se le notifica al cliente
				
				//una vez que entro en la sala: llamo a -> processRoomMenssage()
				}
			}
		} catch (Exception e) {
			//If an error occurs with the communications the user is removed from all the managers and the connection is closed
			System.out.println("* User "+ user + " disconnected.");
			serverManager.leaveRoom(user, currentRoom);
			serverManager.removeUser(user);
		}
		finally {
			if (!socket.isClosed())
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
	}

	//Obtenemos el nick y solicitamos al ServerManager que verifique si está duplicado
	private void receiveAndVerifyNickname() throws IOException {
		//La lógica de nuestro programa nos obliga a que haya un nick registrado antes de proseguir
//		String nick = dis.readUTF();
//		if(serverManager.addUser(nick))
//			dos.writeUTF("OK");
//		else  {dos.writeUTF("DUPLICATED");
//			System.out.println(nick + " Duplicado ");
//			}
		
		NCMessage msg = NCMessage.readMessageFromSocket(dis);
		NCMessageNick nick = (NCMessageNick) msg;
		if(serverManager.addUser(nick.getName())){
			NCMessageControl msgreq = (NCMessageControl)NCMessage.makeControlMessage(NCMessage.OP_OK);
			dos.writeUTF(msgreq.toEncodedString());
		}
			
		else  {
			NCMessageControl msgreq = (NCMessageControl)NCMessage.makeControlMessage(NCMessage.OP_NO_OK);
			dos.writeUTF(msgreq.toEncodedString());
			}
		
		//TODO Entramos en un bucle hasta comprobar que alguno de los nicks proporcionados no está duplicado
		//TODO Extraer el nick del mensaje
		//TODO Validar el nick utilizando el ServerManager - addUser()
		//TODO Contestar al cliente con el resultado (éxito o duplicado)
	}

	//Mandamos al cliente la lista de salas existentes
	private void sendRoomList()  {
		//TODO La lista de salas debe obtenerse a partir del RoomManager y después enviarse mediante su mensaje correspondiente
	}

	private void processRoomMessages()  {
		//TODO Comprobamos los mensajes que llegan hasta que el usuario decida salir de la sala
		boolean exit = false;
		while (!exit) {
			//TODO Se recibe el mensaje enviado por el usuario
			//TODO Se analiza el código de operación del mensaje y se trata en consecuencia
		}
	}
	
}
