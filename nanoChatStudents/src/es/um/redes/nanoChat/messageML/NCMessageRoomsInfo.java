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
	
	private static final String regexpr = "<(\\w+?)>(.*?)</\\1>";
	


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
		
		for (InfoRoom ir : rooms) {
			sb.append("<"+ROOM_MARK+">"+END_LINE);
			sb.append("<"+NAME_ROOM_MARK+">"+ir.name+"</"+NAME_ROOM_MARK+">"+END_LINE);
			sb.append("<"+SIZE_MARK+">"+ir.maxMiembros+"</"+SIZE_MARK+">"+END_LINE);
			sb.append("<"+NUMUSER_MARK +">"+ir.miembros+"</"+NUMUSER_MARK +">"+END_LINE);
			sb.append("</"+ROOM_MARK+">"+END_LINE);	
		}
		
		sb.append("</"+MESSAGE_MARK+">"+END_LINE);

		return sb.toString(); //Se obtiene el mensaje

	}


	//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
	public static NCMessageRoomsInfo readFromString(byte code, String message) {
		
		
		ArrayList<InfoRoom> found_rooms = new ArrayList<InfoRoom>();
		
		Pattern pat_room = Pattern.compile(RE_ROOM);

		Matcher mat_room = pat_room.matcher(message);
		while (mat_room.find()) {
			
			System.out.println("\n\n"+mat_room.group(1)+"\n\n");
			
			Pattern pat_reg = Pattern.compile(regexpr);
			Matcher mat_reg = pat_reg.matcher(mat_room.group(1));
			
			String found_name = null;
			int found_size = 0;
			int found_miembros = 0;
			
			//sabemos que obligatoriamente tendrá estos campos, por lo que tendrá que hacer match 3 veces.
			for(int i = 0; i<3;i++) {
				if(mat_reg.find()) {
					System.out.println(mat_reg.group(1));
					switch(mat_reg.group(1)){
					case NAME_ROOM_MARK:
						found_name = mat_reg.group(2);
						break;
					case SIZE_MARK: 
						found_size = Integer.parseInt(mat_reg.group(2));
						break;
					case NUMUSER_MARK:
						found_miembros = Integer.parseInt(mat_reg.group(2));
						break;
						
					}
				}				
			}
			InfoRoom found_room = new InfoRoom(found_name, found_size,found_miembros);
			found_rooms.add(found_room);
			
		}
		if(found_rooms.isEmpty()) 
			System.out.println("No se han encontrado salas disponibles");
		
			
		return new NCMessageRoomsInfo(code, found_rooms);
	}


	//Devolvemos el nombre contenido en el mensaje
	public ArrayList<InfoRoom> getRooms() {
		return rooms;
	}


}
