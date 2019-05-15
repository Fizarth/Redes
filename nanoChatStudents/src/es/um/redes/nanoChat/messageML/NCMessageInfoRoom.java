package es.um.redes.nanoChat.messageML;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.um.redes.nanoChat.server.roomManager.NCRoomDescription;

/*
 * INFO ROOM
----

<message>
      <operation>opCode</operation>
      <room>room1</room>
      <numUser>2</numUser>
      <time> timelast </time>
      <nick>nick1</nick>
      <nick>nick2</nick>
</message>


Operaciones válidas:

"InfoRoomRequest"
*/
public class NCMessageInfoRoom extends NCMessage{

	

	
	private NCRoomDescription room;
	
	
	//Constantes asociadas a las marcas específicas de este tipo de mensaje
		private static final String RE_ROOM = "<room>(.*?)</room>";
		private static final String ROOM_MARK = "room";
		
		private static final String RE_NUMUSER = "<numUser>(.*?)</numUser>";
		private static final String NUMUSER_MARK = "numUser";
		
		private static final String RE_NICK = "<nick>(.*?)</nick>";
		private static final String NICK_MARK = "nick";
		
		private static final String TIME_MARK = "time";
		private static final String SIZE_MARK = "size";
		
		private static final String patron  = "<(\\w+?)>(.*?)</\\1>";

		/**
		 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
		 */
		public NCMessageInfoRoom(byte opcode, NCRoomDescription room ) {
			this.opcode=opcode;
			this.room = room;
		}

		@Override
		//Pasamos los campos del mensaje a la codificación correcta en lenguaje de marcas
		public String toEncodedString() {
			StringBuffer sb = new StringBuffer();
			
			sb.append("<"+MESSAGE_MARK+">"+END_LINE);
			sb.append("<"+OPERATION_MARK+">"+opcodeToString(opcode)+"</"+OPERATION_MARK+">"+END_LINE); //Construimos el campo
			sb.append("<"+ROOM_MARK+">"+room.roomName+"</"+ROOM_MARK+">"+END_LINE);
			sb.append("<"+NUMUSER_MARK+">"+Integer.toString(room.members.size())+"</"+NICK_MARK+">"+END_LINE);
			sb.append("<"+SIZE_MARK+">"+room.maxMiembros+"</"+SIZE_MARK+">"+END_LINE);
			sb.append("<"+TIME_MARK+">"+room.timeLastMessage+"</"+TIME_MARK+">"+END_LINE);
			for(String nick : room.members){
				sb.append("<"+NICK_MARK+">"+nick+"</"+NICK_MARK+">"+END_LINE);
			}
			sb.append("</"+MESSAGE_MARK+">"+END_LINE);

			return sb.toString(); //Se obtiene el mensaje

		}


		//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
		public static NCMessageInfoRoom readFromString(byte code, String message) {
			String found_name = null;
			long found_time =0;
			int found_size = 0;
			ArrayList<String> found_usuarios = new ArrayList<String>();

			Pattern pat = Pattern.compile(patron);
			Matcher mat_name = pat.matcher(message);
			while (mat_name.find()) {
				switch (mat_name.group(1)) {
				case ROOM_MARK:
					found_name= mat_name.group(2);
					break;

				case NICK_MARK:
					found_usuarios.add(mat_name.group(2));
					break;
				case TIME_MARK:
					found_time=Long.parseLong(mat_name.group(2));
					break;
				case SIZE_MARK:
					found_size = Integer.parseInt(mat_name.group(2));
					break;
				default:
					break;
				}
				
			}
			if(found_name!=null || found_usuarios.size()!=0){
//				System.out.println(found_name);
//				for (String string : found_usuarios) {
//					System.out.println(string);
//				}
				NCRoomDescription room = new NCRoomDescription(found_name, found_usuarios, found_time, found_size);
				return new NCMessageInfoRoom(code, room);
			}
				
			else {
				System.out.println("Error en MessageinfoRoom: no se ha encontrado parametro.");
				return null;	
			}

			
		}





	
		
		public NCRoomDescription getRoomInfo() {
			return this.room;
		}

		
		
	}
