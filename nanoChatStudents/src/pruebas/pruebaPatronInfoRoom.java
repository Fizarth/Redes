package pruebas;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.um.redes.nanoChat.messageML.NCMessageInfoRoom;

public class pruebaPatronInfoRoom {

	public static void main(String[] args) {
		String cadenaOk=
				"<message>"
				+"<operation>opCode</operation>"
				+
				"<room>nombre</room>"
				+ "<numUser>2</numUser>"
				+ "<nick>nick1</nick>"
				+ "<nick>nick2</nick>"
				+"</message>"
				;
			
		String cadenaError= "cadena incorrecta";
		
//		String RE_ROOM = "<room>(.*?)</room>";
//		String RE_NUMUSER = "<numUser>(.*?)</numUser>";
//		String RE_NICK = "<nick>(.*?)</nick>";
//		
//		String patron  = "<([^message]\\w+?)>(.*?)</\\1>";
//		
//		String found_name = "";
//		String aux=null;
//
//		// Tienen que estar los campos porque el mensaje es de tipo RoomMessage
//		Pattern pat_room = Pattern.compile(RE_ROOM);
//		Pattern pat_nick = Pattern.compile(RE_NICK);
//		Pattern pat_numUs = Pattern.compile(RE_NUMUSER);
//		
//		
//	
//		Pattern pat = Pattern.compile(patron);
//		Matcher mat_name = pat.matcher(cadenaOk);
//		while (mat_name.find()) {
//				// Name found
//				found_name += mat_name.group(2)+"  ";
//			}
//			if(found_name.compareTo("")!=0){
//				System.out.println(found_name);
//			}
//			
//			else {
//				System.out.println("Error en MessageChat: no se ha encontrado parametro.");
//				
//			}
		
		NCMessageInfoRoom infoRoom= NCMessageInfoRoom.readFromString((byte) 0, cadenaOk);
		System.out.println(infoRoom.getName()+" "+infoRoom.size()+" "+ infoRoom.getNombresUsers().toString());


	}

}
