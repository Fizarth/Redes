package es.um.redes.nanoChat.client.application;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import es.um.redes.nanoChat.client.comm.NCConnector;
import es.um.redes.nanoChat.client.shell.NCCommands;
import es.um.redes.nanoChat.client.shell.NCShell;
import es.um.redes.nanoChat.directory.connector.DirectoryConnector;
import es.um.redes.nanoChat.server.roomManager.InfoMensaje;
import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

public class NCController {
	// Diferentes estados del cliente de acuerdo con el autómata
	private static final byte PRE_CONNECTION = 1;
	private static final byte PRE_REGISTRATION = 2;

	private static final byte PRE_ROOM = 3;
	private static final byte IN_ROOM = 4;
	// Código de protocolo implementado por este cliente
	// Cambiar para cada grupo
	private static final int PROTOCOL = 98044828; // el nuestro es 48848082 +49196746
	// Conector para enviar y recibir mensajes del directorio
	private DirectoryConnector directoryConnector;
	// Conector para enviar y recibir mensajes con el servidor de NanoChat
	private NCConnector ncConnector;
	// Shell para leer comandos de usuario de la entrada estándar
	private NCShell shell;
	// Último comando proporcionado por el usuario
	private byte currentCommand;
	// Nick del usuario
	private String nickname;
	// Sala de chat en la que se encuentra el usuario (si está en alguna)
	private String room;
	// Mensaje enviado o por enviar al chat
	private String chatMessage;
	// Dirección de internet del servidor de NanoChat
	private InetSocketAddress serverAddress;
	// Estado actual del cliente, de acuerdo con el autómata
	private byte clientStatus = PRE_CONNECTION;
	
	//usuario al que se le quiere enviar el mensaje
	private String usReceptor;

	// Constructor
	public NCController() {
		shell = new NCShell();
	}

	// Devuelve el comando actual introducido por el usuario
	public byte getCurrentCommand() {
		return this.currentCommand;
	}

	// Establece el comando actual
	public void setCurrentCommand(byte command) {
		currentCommand = command;
	}

	// Registra en atributos internos los posibles parámetros del comando tecleado
	// por el usuario
	public void setCurrentCommandArguments(String[] args) {
		// Comprobaremos también si el comando es válido para el estado actual del
		// autómata
		switch (currentCommand) {
		case NCCommands.COM_NICK:
			if (clientStatus == PRE_REGISTRATION)
				nickname = args[0];
			break;
		case NCCommands.COM_ENTER:
			room = args[0];
			break;
		case NCCommands.COM_SEND:
			chatMessage = args[0];
			break;
		case NCCommands.COM_SEND_PRIVATE:
			usReceptor=args[0];
			String msgPrivado="";
			for(int i=1;i<args.length;i++){
				msgPrivado+=args[i]+" ";
			}
			chatMessage = msgPrivado;
			break;
		default:
		}
	}

	// Procesa los comandos introducidos por un usuario que aún no está dentro de
	// una sala
	public void processCommand() throws IOException {
		switch (currentCommand) {
		case NCCommands.COM_NICK:
			if (clientStatus == PRE_REGISTRATION) {
				registerNickName();
			} else
				System.out.println("* You have already registered a nickname (" + nickname + ")");
			break;
		case NCCommands.COM_ROOMLIST:
			// LLamar a getAndShowRooms() si el estado actual del autómata lo permite
			if (clientStatus == PRE_ROOM) {
				getAndShowRooms();
			}
			// Si no está permitido informar al usuario
			else
				System.out.println("Debes salir de la sala para solicitar la lista de salas");
			break;
		case NCCommands.COM_ENTER:
			// LLamar a enterChat() si el estado actual del autómata lo permite
			if (clientStatus == PRE_ROOM) {
				enterChat();
			} else
				System.out.println("Ya estas en una sala. Por favor, sal de la sala para entrar en otra.");
			// Si no está permitido informar al usuario
			break;
		case NCCommands.COM_QUIT:
			// Cuando salimos tenemos que cerrar todas las conexiones y sockets abiertos
			ncConnector.disconnect();
			directoryConnector.close();
			break;
		default:
		}
	}

	// Método para registrar el nick del usuario en el servidor de NanoChat
	private void registerNickName() {
		try {
			// Pedimos que se registre el nick (se comprobará si está duplicado)
			boolean registered = ncConnector.registerNickname(nickname);
			// Cambiar la llamada anterior a registerNickname() al usar mensajes
			// formateados
			if (registered) {
				// Si el registro fue exitoso pasamos al siguiente estado del autómata
				System.out.println("* Your nickname is now " + nickname);
				clientStatus = PRE_ROOM;
			} else
				// En este caso el nick ya existía
				System.out.println("* The nickname is already registered. Try a different one.");
		} catch (IOException e) {
			System.out.println("* There was an error registering the nickname");
		}
	}

	// Método que solicita al servidor de NanoChat la lista de salas e imprime el
	// resultado obtenido
	private void getAndShowRooms() throws IOException {
		// Lista que contendrá las descripciones de las salas existentes
		ArrayList<NCRoomDescription> salas = new ArrayList<>();
		// Le pedimos al conector que obtenga la lista de salas
		salas = ncConnector.getRooms();

		// Una vez recibidas iteramos sobre la lista para imprimir información de
		// cada sala
		for (NCRoomDescription info : salas) {
			System.out.println(info.toPrintableString());
		}
	}

	// Método para tramitar la solicitud de acceso del usuario a una sala concreta
	private void enterChat() throws IOException {
		// Se solicita al servidor la entrada en la sala correspondiente
		// ncConnector.enterRoom()
		boolean acceso = ncConnector.enterRoom(room);
		// Si la respuesta es un rechazo entonces informamos al usuario y salimos
		if (!acceso) {
			System.out.println("No se ha podido entrar en la sala");
			
			clientStatus = PRE_ROOM;
//			
		}
		// En caso contrario informamos que estamos dentro y seguimos
		// Cambiamos el estado del autómata para aceptar nuevos comandos
		else {

			System.out.println("* You are in " + room + " now");
			clientStatus = IN_ROOM;

			do {

				// Pasamos a aceptar sólo los comandos que son válidos dentro de una sala
				readRoomCommandFromShell();
				processRoomCommand();
			} while (currentCommand != NCCommands.COM_EXIT);
			System.out.println("* Your are out of the room");
			// Llegados a este punto el usuario ha querido salir de la sala, cambiamos
			// el estado del autómata -> lo hacemos en la funcion exitRoom()

		}
	}

	// Método para procesar los comandos específicos de una sala
	private void processRoomCommand() throws IOException {
		switch (currentCommand) {
		case NCCommands.COM_ROOMINFO:
			// El usuario ha solicitado información sobre la sala y llamamos al método que
			// la obtendrá
			getAndShowInfo();
			break;
		case NCCommands.COM_SEND:
			// El usuario quiere enviar un mensaje al chat de la sala
			sendChatMessage();
			break;
		case NCCommands.COM_SEND_PRIVATE:
			sendChatPrivateMessage();
			break;
		case NCCommands.COM_SOCKET_IN:
			// En este caso lo que ha sucedido es que hemos recibido un mensaje desde la
			// sala y hay que procesarlo
			processIncommingMessage();
			break;
		case NCCommands.COM_EXIT:
			// El usuario quiere salir de la sala
			exitTheRoom();
			room="";
		}
	}

	// Método para solicitar al servidor la información sobre una sala y para
	// mostrarla por pantalla
	private void getAndShowInfo() throws IOException {
		// Pedimos al servidor información sobre la sala en concreto
		NCRoomDescription info = ncConnector.getRoomInfo(room);
		// Mostramos por pantalla la información
		System.out.println(info.toPrintableString());
		for (String name : info.members) {
			System.out.println("Usuario:\t" + name);
		}

	}

	// Método para notificar al servidor que salimos de la sala
	private void exitTheRoom() throws IOException {
		// Mandamos al servidor el mensaje de salida
		ncConnector.leaveRoom(room);
		// Cambiamos el estado del autómata para indicar que estamos fuera de la
		// sala
		clientStatus = PRE_ROOM;

	}

	// Método para enviar un mensaje al chat de la sala
	private void sendChatMessage() throws IOException {
		// Mandamos al servidor un mensaje de chat
		ncConnector.enviarMensaje(nickname, chatMessage);

	}
	// Método para enviar un mensaje privado al chat de la sala
	private void sendChatPrivateMessage() throws IOException{
		ncConnector.enviarMensaje(nickname, usReceptor,chatMessage);
	}

	// Método para procesar los mensajes recibidos del servidor mientras que el
	// shell estaba esperando un comando de usuario
	private void processIncommingMessage() throws IOException {
		// Recibir el mensaje
		InfoMensaje info = ncConnector.recibirMensaje();
		if(info!=null&&info.privado==false){
			System.out.println("Recibido mensaje público:\n<" + info.usuario + ">\t" + info.texto);
		}
		// En función del tipo de mensaje, actuar en consecuencia
		// (Ejemplo) En el caso de que fuera un mensaje de chat de broadcast
		// mostramos la información de quién envía el mensaje y el mensaje en sí
		else if(info!=null&&info.privado==true){
			System.out.println("Recibido mensaje privado de " + info.usuario + ":\n" + info.texto);
		}
		else{
			System.out.println("ERROR: No se ha podido mandar el mensaje al usuario "+usReceptor);
		}
		
		
	}

	// MNétodo para leer un comando de la sala
	public void readRoomCommandFromShell() {
		// Pedimos un nuevo comando de sala al shell (pasando el conector por si nos
		// llega un mensaje entrante)
		shell.readChatCommand(ncConnector);
		// Establecemos el comando tecleado (o el mensaje recibido) como comando actual
		setCurrentCommand(shell.getCommand());
		// Procesamos los posibles parámetros (si los hubiera)
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	// Método para leer un comando general (fuera de una sala)
	public void readGeneralCommandFromShell() {
		// Pedimos el comando al shell
		shell.readGeneralCommand();
		// Establecemos que el comando actual es el que ha obtenido el shell
		setCurrentCommand(shell.getCommand());
		// Analizamos los posibles parámetros asociados al comando
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	// Método para obtener el servidor de NanoChat que nos proporcione el directorio
	public boolean getServerFromDirectory(String directoryHostname) {
		// Inicializamos el conector con el directorio y el shell
		System.out.println("* Connecting to the directory...");
		// Intentamos obtener la dirección del servidor de NanoChat que trabaja con
		// nuestro protocolo
		try {
			directoryConnector = new DirectoryConnector(directoryHostname);
			serverAddress = directoryConnector.getServerForProtocol(PROTOCOL);
			
			
		} catch (IOException e1) {
			serverAddress = null;
		}
		// Si no hemos recibido la dirección entonces nos quedan menos intentos
		if (serverAddress == null) {
			System.out.println("* Check your connection, the directory is not available.");
			return false;
		} else
			return true;
	}

	// Método para establecer la conexión con el servidor de Chat (a través del
	// NCConnector)
	public boolean connectToChatServer() {
		try {
			// Inicializamos el conector para intercambiar mensajes con el servidor de
			// NanoChat (lo hace la clase NCConnector)
			ncConnector = new NCConnector(serverAddress);
		} catch (IOException e) {
			System.out.println("* Check your connection, the game server is not available.");
			serverAddress = null;
		}
		// Si la conexión se ha establecido con éxito informamos al usuario y cambiamos
		// el estado del autómata
		if (serverAddress != null) {
			System.out.println("* Connected to " + serverAddress);
			clientStatus = PRE_REGISTRATION;
			return true;
		} else
			return false;
	}

	// Método que comprueba si el usuario ha introducido el comando para salir de la
	// aplicación
	public boolean shouldQuit() {
		return currentCommand == NCCommands.COM_QUIT;
	}

}
