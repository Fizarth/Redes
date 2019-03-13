package pruebas;

import java.io.IOException;
import java.net.InetSocketAddress;

import es.um.redes.nanoChat.directory.connector.DirectoryConnector;

public class boletin3 {

	public static void main(String[] args) {
		try {
			DirectoryConnector dc = new DirectoryConnector("localhost");
			Boolean registro=dc.registerServerForProtocol(12, 2);
			System.out.println(registro);
			
			registro=dc.registerServerForProtocol(12, 2);
			System.out.println(registro);
			
			InetSocketAddress respuesta=dc.getServerForProtocol(12);
			
			System.out.println("Host: "+respuesta.getHostName()+" puerto: "+respuesta.getPort());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

