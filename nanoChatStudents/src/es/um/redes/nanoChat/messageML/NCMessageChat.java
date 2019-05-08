package es.um.redes.nanoChat.messageML;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
 * CHAT
----

<message>
	<operation> opCode</operation>
	<user> usuario </user>
	<mensaje>Mensaje de Texto </mensaje>
</message>



Operaciones válidas:

"Message"
*/
public class NCMessageChat extends NCMessage{
	
	private String mensaje;
	private String user;
	
	//Constantes asociadas a las marcas específicas de este tipo de mensaje
	private static final String RE_NAME = "<mensaje>(.*?)</mensaje>";
	private static final String NAME_MARK = "mensaje";
	
	private static final String RE_USER = "<user>(.*?)</user>";
	private static final String USER_MARK = "user";
	
	private static final String patron  = "<([^message]\\w+?)>(.*?)</\\1>";

	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
	 */
	public NCMessageChat(byte opcode, String usuario, String msg) {
		this.opcode = opcode;
		this.mensaje = msg;
		this.user = usuario;
	}

	@Override
	//Pasamos los campos del mensaje a la codificación correcta en lenguaje de marcas
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<"+MESSAGE_MARK+">"+END_LINE);
		sb.append("<"+OPERATION_MARK+">"+opcodeToString(opcode)+"</"+OPERATION_MARK+">"+END_LINE); //Construimos el campo
		sb.append("<"+USER_MARK+">"+user+"</"+USER_MARK+">"+END_LINE);
		sb.append("<"+NAME_MARK+">"+mensaje+"</"+NAME_MARK+">"+END_LINE);
		sb.append("</"+MESSAGE_MARK+">"+END_LINE);

		return sb.toString(); //Se obtiene el mensaje

	}


	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
	public static NCMessageChat readFromString(byte code, String message) {
		String found_name = null;
		String found_user = null;
		// Tienen que estar los campos porque el mensaje es de tipo RoomMessage
		
	
		
		Pattern pat = Pattern.compile(patron);
		Matcher mat_name = pat.matcher(message);
		
		
		while (mat_name.find()) {
			switch (mat_name.group(1)) {
			case USER_MARK:
				found_user= mat_name.group(2);
				break;

			case NAME_MARK:
				found_name = mat_name.group(2);
				break;
			default:
				break;
			}
		}
			
		if(found_name!=null || found_user != null){
			return new NCMessageChat(code, found_user,found_name);
		}	
		else {		
			System.out.println("Error en MessageChat: no se ha encontrado parametro.");
			return null;	
		}
		
		
	}
	


	//Devolvemos el nombre contenido en el mensaje
	public String getName() {
		return mensaje;
	}
	
	public String getUser() {
		return user;
	}
}