package es.um.redes.nanoChat.directory.mensajes;

public abstract class Mensaje {
	
	/*
	 * No se como codificar con binario =S, creo que es algo que me he saltado que tengo que mirar.. 
	 * Tipos de mensajes:
	 * 
	 */
	
	//lo que ocupa el campo de control
	protected static final int COD_BYTES = 1;

	private byte opCode;
	
	public static final byte COD_INVALID = 0;
	public static final byte COD_OK = 1;
	public static final byte COD_EMPTY = 2;
	public static final byte COD_NO_OK = 3;
	public static final byte COD_REGISTRO = 4;
	public static final byte COD_CONSULTA = 5;
	public static final byte COD_RESPUESTA_CONSULTA = 6;
	
	public Mensaje() {
		opCode = COD_INVALID;
		
	}
	
	public byte getOpCode() {
		return opCode;
	}
	
	protected final boolean setOpCode(byte opCode) {
		//esto habrá que mirarlo para poner finales 
		if (opCode>0 && opCode< 6) {
			this.opCode = opCode;
			return true;
		}
		return false;
	}
	
	public static Mensaje makeRegistro(int portCliente) {
		byte code = COD_REGISTRO;
		return new MensajeFileInfo??? (portCliente..code bla bla bla);
	}
}
