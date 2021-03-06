package es.um.redes.nanoChat.messageML;

/*
 * CONTROL
----

<message>
	<operation>operation</operation>
</message>

Operaciones válidas:

"OK"
"NoOk"
"ExitRoom"
"QueryRoom"
"Exit"
*/


public class NCMessageControl extends NCMessage {

	/**
	 * Creamos un mensaje de tipo Room a partir del código de operación y del nombre
	 */
	public NCMessageControl(byte opcode) {
		this.opcode = opcode;

	}

	@Override
	//Construimos el mensaje
	public String toEncodedString() {
		StringBuffer sb = new StringBuffer();		
		sb.append("<"+MESSAGE_MARK+">"+END_LINE);
		sb.append("<"+OPERATION_MARK+">"+opcodeToString(opcode)+"</"+OPERATION_MARK+">"+END_LINE); //Construimos el campo
		sb.append("</"+MESSAGE_MARK+">"+END_LINE);
		return sb.toString();
	}

	//Parseamos el mensaje
	public static NCMessageControl readFromString(byte code) {		
		return new NCMessageControl(code);
	}


}



