package es.um.redes.nanoChat.server.roomManager;



public class InfoMensaje {
	
	public String usuario;
	public String texto;
	public boolean privado;
	
	public InfoMensaje(String usuario, String text, boolean privado) {
		this.usuario=usuario;
		this.texto=text;
		this.privado=privado;
	}
}
