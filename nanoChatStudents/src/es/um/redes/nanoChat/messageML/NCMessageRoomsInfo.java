package es.um.redes.nanoChat.messageML;
/*
 * ROOMS INFO
----

<message>
      <operation>opCode</operation>
      <room>
            <name>NombreRoom1</name>    
            <size> 15 </size>  
            <numUser>5</numUser>
      </room>
      <room>
            <name>NombreRoom2</name>   
            <size> 15 </size>     
            <numUser>1</numUser>
      </room>
      <room>
            <name>NombreRoom3</name>     
            <size> 15 </size>   
            <numUser>10</numUser>
      </room>
        ….
</message>



Operaciones válidas:

"ListRoom"
*/

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.um.redes.nanoChat.server.roomManager.InfoRoom;

public class NCMessageRoomsInfo extends NCMessage{
	
	private String mensaje;
	private ArrayList<InfoRoom> rooms;
	
	//Constantes asociadas a las marcas específicas de este tipo de mensaje
	private static final String RE_ROOM = "<room>(.*?)</room>";
	private static final String ROOM_MARK = "room";
	
	private static final String RE_NAME_ROOM = "<name>(.*?)</name>";
	private static final String NAME_ROOM_MARK = "name";
	
	private static final String RE_SIZE = "<size>(.*?)</size>";
	private static final String SIZE_MARK = "size";
	
	private static final String RE_NUMUSER = "<numUser>(.*?)</numUser>";
	private static final String NUMUSER_MARK = "numUser";
	


	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
	 */
	public NCMessageRoomsInfo(byte opcode, ArrayList<InfoRoom> rooms) {
		this.opcode = opcode;
		this.rooms = rooms;
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
	public static NCMessageRoom readFromString(byte code, String message) {
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
		
		return new NCMessageRoom(code, found_name);
	}


	//Devolvemos el nombre contenido en el mensaje
	public String getName() {
		return mensaje;
	}


}
