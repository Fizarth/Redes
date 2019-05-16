package es.um.redes.nanoChat.messageML;
/*
 * PRIVADO
----

<message>
	<operation> opCode</operation>
	<emisor> usuario1 </emisor>
	<receptor> usuario2 </receptor>
	<mensaje>Mensaje de Texto </mensaje>
</message>



Operaciones válidas:

"MessagePrivate"
*/
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NCMessageChatPrivado extends NCMessage {

	private String mensaje;
	private String emisor;
	private String receptor;

	// Constantes asociadas a las marcas específicas de este tipo de mensaje
//	private static final String RE_NAME = "<mensaje>(.*?)</mensaje>";
	private static final String NAME_MARK = "mensaje";

//	private static final String RE_USER = "<user>(.*?)</user>";
	private static final String EMISOR_MARK = "emisor";
	private static final String RECEPTOR_MARK = "receptor";

	private static final String patron = "<(\\w+?)>(.*?)</\\1>";

	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
	 */
	public NCMessageChatPrivado(byte opcode, String emisor,String receptor, String msg) {
		this.opcode = opcode;
		this.mensaje = msg;
		this.emisor = emisor;
		this.receptor=receptor;
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
		sb.append("<" + EMISOR_MARK + ">" + emisor + "</" + EMISOR_MARK + ">" + END_LINE);
		sb.append("<" + RECEPTOR_MARK + ">" + receptor + "</" + RECEPTOR_MARK + ">" + END_LINE);
		sb.append("<" + NAME_MARK + ">" + mensaje + "</" + NAME_MARK + ">" + END_LINE);
		sb.append("</" + MESSAGE_MARK + ">" + END_LINE);

		return sb.toString(); // Se obtiene el mensaje

	}

	// Parseamos el mensaje contenido en message con el fin de obtener los
	// distintos campos
	public static NCMessageChatPrivado readFromString(byte code, String message) {
		String found_name = null;
		String found_emisor = null;
		String found_receptor = null;

		Pattern pat = Pattern.compile(patron);
		Matcher mat_name = pat.matcher(message);

		while (mat_name.find()) {
			switch (mat_name.group(1)) {
			case EMISOR_MARK:
				found_emisor = mat_name.group(2);
				break;
			case RECEPTOR_MARK:
				found_receptor = mat_name.group(2);
				break;

			case NAME_MARK:

				found_name = mat_name.group(2);
				break;
			default:
				break;
			}
		}

		if (found_name != null && found_emisor != null && found_receptor != null) {
			return new NCMessageChatPrivado(code, found_emisor,found_receptor, found_name);
		} else {
			System.out.println("Error en MessageChatPrivado: no se ha encontrado parametro.");
			return null;
		}

	}

	// Devolvemos el nombre contenido en el mensaje
	public String getName() {
		return mensaje;
	}

	public String getEmisor() {
		return emisor;
	}
	public String getReceptor(){
		return receptor;
	}
}
