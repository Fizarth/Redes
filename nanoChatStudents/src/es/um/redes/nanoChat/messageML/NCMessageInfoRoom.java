package es.um.redes.nanoChat.messageML;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * INFO ROOM
----

<message>
      <operation>opCode</operation>
      <room>room1</room>
      <numUser>2</numUser>
      <nick>nick1</nick>
      <nick>nick2</nick>
</message>


Operaciones válidas:

"InfoRoomRequest"
*/
public class NCMessageInfoRoom extends NCMessage{

	private String nombreRoom;
	private int numUsers;
	private String[] nombresUsers;
	
	
	//Constantes asociadas a las marcas específicas de este tipo de mensaje
		private static final String RE_ROOM = "<room>(.*?)</room>";
		private static final String ROOM_MARK = "room";
		
		private static final String RE_NUMUSER = "<numUser>(.*?)</numUser>";
		private static final String NUMUSER_MARK = "numUser";
		
		private static final String RE_NICK = "<nick>(.*?)</nick>";
		private static final String NICK_MARK = "nick";
		
		private static final String patron  = "<(\\w+?)>(.*?)</\\1>";

		/**
		 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
		 */
		public NCMessageInfoRoom(byte opcode, String name, String ...nombres) {
			this.opcode=opcode;
			this.nombreRoom=name;
			this.numUsers=nombres.length;
			this.nombresUsers=nombres;
		}

		@Override
		//Pasamos los campos del mensaje a la codificación correcta en lenguaje de marcas
		public String toEncodedString() {
			StringBuffer sb = new StringBuffer();
			
			sb.append("<"+MESSAGE_MARK+">"+END_LINE);
			sb.append("<"+OPERATION_MARK+">"+opcodeToString(opcode)+"</"+OPERATION_MARK+">"+END_LINE); //Construimos el campo
			sb.append("<"+ROOM_MARK+">"+nombreRoom+"</"+ROOM_MARK+">"+END_LINE);
			sb.append("<"+NUMUSER_MARK+">"+Integer.toString(numUsers)+"</"+NICK_MARK+">"+END_LINE);
			for(int i=0; i<numUsers;i++){
				sb.append("<"+NICK_MARK+">"+nombresUsers[i]+"</"+NICK_MARK+">"+END_LINE);
			}
			sb.append("</"+MESSAGE_MARK+">"+END_LINE);

			return sb.toString(); //Se obtiene el mensaje

		}


		//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
		public static NCMessageInfoRoom readFromString(byte code, String message) {
			String found_name = null;

			Pattern pat = Pattern.compile(patron);
			Matcher mat_name = pat.matcher(message);
			while (mat_name.find()) {
				// Name found
				found_name += mat_name.group(2)+"  ";
			}
			if(found_name.compareTo("")!=0){
				System.out.println(found_name);
			}
				
			else {
				System.out.println("Error en MessageChat: no se ha encontrado parametro.");
					
			}

			return new NCMessageInfoRoom(code, found_name);
		}


		public String getName() {
			return nombreRoom;
		}

		public int size() {
			return numUsers;
		}

		public String[] getNombresUsers() {
			return nombresUsers;
		}

		
		
	}
