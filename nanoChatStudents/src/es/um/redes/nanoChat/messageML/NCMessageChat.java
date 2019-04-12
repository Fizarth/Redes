package es.um.redes.nanoChat.messageML;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
 * CHAT
----

<message>
	<operation> opCode</operation>
	<mensaje>Mensaje de Texto </mensaje>
</message>



Operaciones válidas:

"Message"
*/
public class NCMessageChat extends NCMessage{
	
	private String mensaje;
	
	//Constantes asociadas a las marcas específicas de este tipo de mensaje
	private static final String RE_NAME = "<mensaje>(.*?)</mensaje>";
	private static final String NAME_MARK = "mensaje";

	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
	 */
	public NCMessageChat(byte opcode, String msg) {
		this.opcode = opcode;
		this.mensaje = msg;
	}

	@Override
	//Pasamos los campos del mensaje a la codificación correcta en lenguaje de marcas
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<"+MESSAGE_MARK+">"+END_LINE);
		sb.append("<"+OPERATION_MARK+">"+opcodeToString(opcode)+"</"+OPERATION_MARK+">"+END_LINE); //Construimos el campo
		sb.append("<"+NAME_MARK+">"+mensaje+"</"+NAME_MARK+">"+END_LINE);
		sb.append("</"+MESSAGE_MARK+">"+END_LINE);

		return sb.toString(); //Se obtiene el mensaje

	}


	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
	public static NCMessageChat readFromString(byte code, String message) {
		String found_name = null;

		// Tienen que estar los campos porque el mensaje es de tipo RoomMessage
		Pattern pat_name = Pattern.compile(RE_NAME);
		Matcher mat_name = pat_name.matcher(message);
		if (mat_name.find()) {
			// Name found
			found_name = mat_name.group(1);
		} else {
			System.out.println("Error en MessageChat: no se ha encontrado parametro.");
			return null;
		}
		
		return new NCMessageChat(code, found_name);
	}


	//Devolvemos el nombre contenido en el mensaje
	public String getName() {
		return mensaje;
	}
}