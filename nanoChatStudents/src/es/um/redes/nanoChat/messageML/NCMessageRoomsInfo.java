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
            <time> timelast </time>
            <nick> usu1</nick>
            <nick> ....
      </room>
      <room>
            <name>NombreRoom2</name>   
            <size> 15 </size>     
            <numUser>1</numUser>
            <time> timelast </time>
            <nick> usu1</nick>
            <nick> ....
      </room>
      <room>
            <name>NombreRoom3</name>     
            <size> 15 </size>   
            <numUser>10</numUser>
            <time> timelast </time>
            <nick> usu1</nick>
            <nick> ....
      </room>
        ….
</message>



Operaciones válidas:

"ListRoom"
*/

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

public class NCMessageRoomsInfo extends NCMessage {

	private String mensaje;
	private ArrayList<NCRoomDescription> rooms;

	// Constantes asociadas a las marcas específicas de este tipo de mensaje
	private static final String RE_ROOM = "<room>\\n(.*?)</room>\\n";
	private static final String ROOM_MARK = "room";

	private static final String RE_NAME_ROOM = "<name>(.*?)</name>";
	private static final String NAME_ROOM_MARK = "name";

	private static final String RE_SIZE = "<size>(.*?)</size>";
	private static final String SIZE_MARK = "size";

	private static final String RE_NUMUSER = "<numUser>(.*?)</numUser>";
	private static final String NUMUSER_MARK = "numUser";

	private static final String TIME_MARK = "time";
	private static final String NICK_MARK = "nick";

	private static final String regexpr = "<(\\w+?)>(.*?)</\\1>";

	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del
	 * nombre
	 */
	public NCMessageRoomsInfo(byte opcode, ArrayList<NCRoomDescription> rooms) {
		this.opcode = opcode;
		this.rooms = rooms;
	}

	@Override
	// Pasamos los campos del mensaje a la codificación correcta en lenguaje de
	// marcas
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();

		sb.append("<" + MESSAGE_MARK + ">" + END_LINE);
		sb.append("<" + OPERATION_MARK + ">" + opcodeToString(opcode) + "</" + OPERATION_MARK + ">" + END_LINE); // Construimos
																													// el
																													// campo

		for (NCRoomDescription ir : rooms) {
			sb.append("<" + ROOM_MARK + ">" + END_LINE);
			sb.append("<" + NAME_ROOM_MARK + ">" + ir.roomName + "</" + NAME_ROOM_MARK + ">" + END_LINE);
			sb.append("<" + SIZE_MARK + ">" + ir.maxMiembros + "</" + SIZE_MARK + ">" + END_LINE);
			sb.append("<" + NUMUSER_MARK + ">" + ir.members.size() + "</" + NUMUSER_MARK + ">" + END_LINE);
			sb.append("<" + TIME_MARK + ">" + ir.timeLastMessage + "</" + TIME_MARK + ">" + END_LINE);

			for (String s : ir.members) {
				sb.append("<" + NICK_MARK + ">" + s + "</" + NICK_MARK + ">" + END_LINE);
			}

			sb.append("</" + ROOM_MARK + ">" + END_LINE);
		}

		sb.append("</" + MESSAGE_MARK + ">" + END_LINE);

		return sb.toString(); // Se obtiene el mensaje

	}

	// Parseamos el mensaje contenido en message con el fin de obtener los
	// distintos campos
	public static NCMessageRoomsInfo readFromString(byte code, String message) {

		ArrayList<NCRoomDescription> found_rooms = new ArrayList<NCRoomDescription>();

		ArrayList<String> found_name = new ArrayList<>();
		ArrayList<Integer> found_size = new ArrayList<>();
		ArrayList<Integer> found_miembros = new ArrayList<>();
		ArrayList<Long> found_times = new ArrayList<>();
		ArrayList<String> found_nicks = new ArrayList<>();

		Pattern pat_room = Pattern.compile(regexpr); // cambiar RE_ROOM por
														// patron
		Matcher mat_room = pat_room.matcher(message);
		while (mat_room.find()) {
			switch (mat_room.group(1)) {
			case NAME_ROOM_MARK:
				found_name.add(mat_room.group(2));
				break;
			case TIME_MARK:
				found_times.add(Long.parseLong(mat_room.group(2)));
				break;
			case SIZE_MARK:
				found_size.add(Integer.parseInt(mat_room.group(2)));
				break;
			case NUMUSER_MARK:
				found_miembros.add(Integer.parseInt(mat_room.group(2)));
				break;
			case NICK_MARK:
				found_nicks.add(mat_room.group(2));
				break;
			default:
				break;
			}
		}
		for (int i = 0; i < found_name.size(); i++) {
			ArrayList<String> members = new ArrayList<>();
			for (int j = 0; j < found_miembros.get(i); j++) {
				members.add(found_nicks.get(0));
				found_nicks.remove(0);
			}
			NCRoomDescription found_room = new NCRoomDescription(found_name.get(i), members, found_times.get(i));
			found_rooms.add(found_room);
		}
		if (found_rooms.isEmpty())
			System.out.println("Error en MessageRoomsInfo: No se han encontrado salas disponibles");

		return new NCMessageRoomsInfo(code, found_rooms);
	}

	// Devolvemos el nombre contenido en el mensaje
	public ArrayList<NCRoomDescription> getRooms() {
		return rooms;
	}

}
