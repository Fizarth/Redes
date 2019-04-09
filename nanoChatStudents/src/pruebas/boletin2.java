package pruebas;

import java.io.IOException;

import es.um.redes.nanoChat.directory.*;
import es.um.redes.nanoChat.directory.connector.DirectoryConnector;
import es.um.redes.nanoChat.directory.server.Directory;
import es.um.redes.nanoChat.directory.server.DirectoryThread;

public class boletin2 {

	public static void main(String[] args) {
		
		try {
			DirectoryConnector dc = new DirectoryConnector("localhost");
			dc.registerServerForProtocol(1, 2);
			dc.getServerForProtocol(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
