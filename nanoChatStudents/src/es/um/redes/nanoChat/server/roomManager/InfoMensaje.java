package es.um.redes.nanoChat.server.roomManager;



public class InfoMensaje {
	
	public String usuario; //usuario que envía el mensaje.
	public String texto; // mensaje enviado.
	public boolean privado; // si es un mensaje privado o no.
	
	public InfoMensaje(String usuario, String text, boolean privado) {
		this.usuario=usuario;
		this.texto=text;
		this.privado=privado;
	}
}
