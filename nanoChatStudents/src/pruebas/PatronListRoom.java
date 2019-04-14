package pruebas;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.um.redes.nanoChat.messageML.NCMessageRoomsInfo;
import es.um.redes.nanoChat.server.roomManager.InfoRoom;

public class PatronListRoom {
	
static String messge = 
		"<message>"
		+"<operation>opCode</operation>"
		+
		"<room>\n"
		+ "<name>NombreRoom1</name>"
		+ "<size>15</size>"
		+ "<numUser>5</numUser>"
		+ " </room>\n"
		+ " <room>\n"
		+ "<name>NombreRoom2</name>"
		+ "<size>15</size>   "
		+ "<numUser>1</numUser>"
		+ "</room>\n"
		+ "<room>\n"
		+ "<name>NombreRoom3</name>"
		+ "<size>15</size>"
		+ "<numUser>10</numUser>"
		+ "</room>\n"
		+"</message>"
		;

private static final String RE_ROOM = "<room>\n(.*?)</room>\n";
private static final String regexpr = "<(\\w+?)>(.*?)</\\1>";

private static final String NAME_ROOM_MARK = "name";
private static final String SIZE_MARK = "size";
private static final String NUMUSER_MARK = "numUser";

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

	public static void main(String[] args) {
		byte s = 4;
		NCMessageRoomsInfo msg =  readFromString(s,messge);
		ArrayList<InfoRoom> rooms = msg.getRooms();
		for(InfoRoom ir: rooms) {
			System.out.println(ir.name);
		}

	}

}
