package es.um.redes.nanoChat.server.roomManager;

public class InfoRoom {
	
	public String name;
	public int maxMiembros;
	public int miembros;
	
	public InfoRoom(String name, int maxMiembros, int miembros) {
		this.name = name;
		this.maxMiembros = maxMiembros;
		this.miembros = miembros;
	}
}
