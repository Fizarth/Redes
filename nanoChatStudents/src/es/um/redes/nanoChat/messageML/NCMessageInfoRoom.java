package es.um.redes.nanoChat.messageML;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory.Default;

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
	private ArrayList<String> nombresUsers;
	
	
	//Constantes asociadas a las marcas específicas de este tipo de mensaje
		private static final String RE_ROOM = "<room>(.*?)</room>";
		private static final String ROOM_MARK = "room";
		
		private static final String RE_NUMUSER = "<numUser>(.*?)</numUser>";
		private static final String NUMUSER_MARK = "numUser";
		
		private static final String RE_NICK = "<nick>(.*?)</nick>";
		private static final String NICK_MARK = "nick";
		
		private static final String patron  = "<([^message]\\w+?)>(.*?)</\\1>";

		/**
		 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
		 */
		public NCMessageInfoRoom(byte opcode, String name, ArrayList<String> usuarios) {
			this.opcode=opcode;
			this.nombreRoom=name;
			this.numUsers=usuarios.size();
			this.nombresUsers=usuarios;
		}

		@Override
		//Pasamos los campos del mensaje a la codificación correcta en lenguaje de marcas
		public String toEncodedString() {
			StringBuffer sb = new StringBuffer();
			
			sb.append("<"+MESSAGE_MARK+">"+END_LINE);
			sb.append("<"+OPERATION_MARK+">"+opcodeToString(opcode)+"</"+OPERATION_MARK+">"+END_LINE); //Construimos el campo
			sb.append("<"+ROOM_MARK+">"+nombreRoom+"</"+ROOM_MARK+">"+END_LINE);
			sb.append("<"+NUMUSER_MARK+">"+Integer.toString(numUsers)+"</"+NICK_MARK+">"+END_LINE);
			for(String nick : nombresUsers){
				sb.append("<"+NICK_MARK+">"+nick+"</"+NICK_MARK+">"+END_LINE);
			}
			sb.append("</"+MESSAGE_MARK+">"+END_LINE);

			return sb.toString(); //Se obtiene el mensaje

		}


		//Parseamos el mensaje contenido en message con el fin de obtener los distintos campos
		public static NCMessageInfoRoom readFromString(byte code, String message) {
			String found_name = null;
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
				default:
					break;
				}
				
			}
			if(found_name!=null || found_usuarios.size()!=0){
//				System.out.println(found_name);
//				for (String string : found_usuarios) {
//					System.out.println(string);
//				}
				
				return new NCMessageInfoRoom(code, found_name,found_usuarios);
			}
				
			else {
				System.out.println("Error en MessageinfoRoom: no se ha encontrado parametro.");
				return null;	
			}

			
		}


		public String getName() {
			return nombreRoom;
		}

		public int size() {
			return numUsers;
		}

		public ArrayList<String> getNombresUsers() {
			return nombresUsers;
		}

		
		
	}
