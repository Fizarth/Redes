package es.um.redes.nanoChat.messageML;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;


public abstract class NCMessage {
	protected byte opcode;

	// IMPLEMENTAR TODAS LAS CONSTANTES RELACIONADAS CON LOS CODIGOS DE OPERACION
	public static final byte OP_INVALID_CODE = 0;
	public static final byte OP_NICK = 1;
	public static final byte OP_OK = 2;
	public static final byte OP_NO_OK = 3;
	
	public static final byte OP_QUERY_ROOM = 4;
	public static final byte OP_LIST_ROOM = 5;
	public static final byte OP_INFO_ROOM = 6;
	public static final byte OP_INFO_ROOM_REQUEST = 7;
	
	public static final byte OP_ENTER_ROOM = 8;
	public static final byte OP_EXIT_ROOM = 9;
	public static final byte OP_MESSAGE = 10;
	public static final byte OP_MESSAGE_PRIVATE = 11;
	
	public static final byte OP_EXIT = 12;

	
	
	
	public static final char DELIMITER = ':';    //Define el delimitador
	public static final char END_LINE = '\n';    //Define el carácter de fin de línea
	
	public static final String OPERATION_MARK = "operation";
	public static final String MESSAGE_MARK = "message";

	/**
	 * Códigos de los opcodes válidos  El orden
	 * es importante para relacionarlos con la cadena
	 * que aparece en los mensajes
	 */
	private static final Byte[] _valid_opcodes = { 
		OP_NICK,OP_OK,OP_NO_OK,OP_QUERY_ROOM,OP_LIST_ROOM,OP_INFO_ROOM,
		OP_INFO_ROOM_REQUEST,OP_ENTER_ROOM,OP_EXIT_ROOM,OP_MESSAGE,OP_MESSAGE_PRIVATE,OP_EXIT		
		};

	/**
	 * cadena exacta de cada orden
	 */
	private static final String[] _valid_opcodes_str = {
		"Nick","OK","NoOk","QueryRoom","ListRoom","InfoRoom","InfoRoomRequest",
		"EnterRoom","ExitRoom","Message","MessagePrivate","Exit"
	};

	/**
	 * Transforma una cadena en el opcode correspondiente
	 */
	protected static byte stringToOpcode(String opStr) {
		//Busca entre los opcodes si es válido y devuelve su código
		for (int i = 0;	i < _valid_opcodes_str.length; i++) {
			if (_valid_opcodes_str[i].equalsIgnoreCase(opStr)) {
				return _valid_opcodes[i];
			}
		}
		//Si no se corresponde con ninguna cadena entonces devuelve el código de código no válido
		return OP_INVALID_CODE;
	}

	/**
	 * Transforma un opcode en la cadena correspondiente
	 */
	protected static String opcodeToString(byte opcode) {
		//Busca entre los opcodes si es válido y devuelve su cadena
		for (int i = 0;	i < _valid_opcodes.length; i++) {
			if (_valid_opcodes[i] == opcode) {
				return _valid_opcodes_str[i];
			}
		}
		//Si no se corresponde con ningún opcode entonces devuelve null
		return null;
	}
	
	
	
	//Devuelve el opcode del mensaje
	public byte getOpcode() {
		return opcode;

	}

	//Método que debe ser implementado por cada subclase de NCMessage
	protected abstract String toEncodedString();

	//Analiza la operación de cada mensaje y usa el método readFromString() de cada subclase para parsear
	public static NCMessage readMessageFromSocket(DataInputStream dis) throws IOException {
		String message = dis.readUTF();
		String regexpr = "<"+MESSAGE_MARK+">(.*?)</"+MESSAGE_MARK+">";
		Pattern pat = Pattern.compile(regexpr,Pattern.DOTALL);
		Matcher mat = pat.matcher(message);
		if (!mat.find()) {
			System.out.println("Mensaje mal formado:\n"+message);
			return null;
			// Message not found
		} 
		String inner_msg = mat.group(1);  // extraemos el mensaje

		String regexpr1 = "<"+OPERATION_MARK+">(.*?)</"+OPERATION_MARK+">";
		Pattern pat1 = Pattern.compile(regexpr1);
		Matcher mat1 = pat1.matcher(inner_msg);
		if (!mat1.find()) {
			System.out.println("Mensaje mal formado:\n" +message);
			return null;
			// Operation not found
		} 
		String operation = mat1.group(1);  // extraemos la operación
		
		byte code = stringToOpcode(operation);
		if (code == OP_INVALID_CODE) return null;
		
		switch (code) {
		//Parsear el resto de mensajes 
		case OP_NICK:{
			return NCMessageNick.readFromString(code, message);
		}
		case OP_OK:{
			return NCMessageControl.readFromString(code);
		}
		case OP_NO_OK:{
			return NCMessageControl.readFromString(code);
		}
		case OP_EXIT_ROOM:{
			return NCMessageControl.readFromString(code);
		}
		case OP_ENTER_ROOM:{
			return NCMessageRoom.readFromString(code,message);
		}
		case OP_QUERY_ROOM:{
			return NCMessageControl.readFromString(code);
		}
		case OP_LIST_ROOM:{
			return NCMessageRoomsInfo.readFromString(code, message);
		}
		case OP_INFO_ROOM:{
			return NCMessageRoom.readFromString(code,message);
		}
		case OP_INFO_ROOM_REQUEST:{
			return NCMessageInfoRoom.readFromString(code, message);
		}
		case OP_MESSAGE:{
			return NCMessageChat.readFromString(code, message);
		}
		case OP_MESSAGE_PRIVATE:{
			return NCMessageChatPrivado.readFromString(code, message);
		}
		case OP_EXIT:{
			return NCMessageControl.readFromString(code);
		}
		default:
			System.err.println("Unknown message type received:" + code);
			return null;
		}

	}

	//Programar el resto de métodos para crear otros tipos de mensajes
	
	
	public static NCMessage makeControlMessage(byte code) {
		return (new NCMessageControl(code));
	}
	public static NCMessage makeNickMessage(byte code, String nick) {
		return (new NCMessageNick(code, nick));
	}
	public static NCMessage makeRoomMessage(byte code, String room) {
		return (new NCMessageRoom(code, room));
	}
	
	public static NCMessage makeChatMessage(byte code, String user,String msg){
		return (new NCMessageChat(code, user, msg));
	}
	public static NCMessage makeChatMessagePrivate(byte code, String emisor,String receptor,String msg){
		return (new NCMessageChatPrivado(code, emisor,receptor, msg));
	}
	
	public static NCMessageInfoRoom makeInfoRoomMessage(byte code, NCRoomDescription room){
		return (new NCMessageInfoRoom(code,room));
	}
	
	public static NCMessageRoomsInfo makeRoomsInfoMessage(byte code, ArrayList<NCRoomDescription> info){
		return (new NCMessageRoomsInfo(code, info));
	}
	
}

