package es.um.redes.nanoChat.directory.server;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashMap;


public class DirectoryThread extends Thread {

	//Tamaño máximo del paquete UDP
	private static final int PACKET_MAX_SIZE = 128;
	
	//CODIGOS 
			private static final byte COD_OK = 1;
			private static final byte COD_EMPTY = 2;
			private static final byte COD_NO_OK = 3;
			private static final byte COD_REGISTRO = 4;
			private static final byte COD_CONSULTA = 5;
			private static final byte COD_RESPUESTA_CONSULTA = 6;
			
			
	//Estructura para guardar las asociaciones ID_PROTOCOLO -> Dirección del servidor
	protected HashMap<Integer,InetSocketAddress> servers;

	//Socket de comunicación UDP
	protected DatagramSocket socket = null;
	//Probabilidad de descarte del mensaje
	protected double messageDiscardProbability;

	public DirectoryThread(String name, int directoryPort,
			double corruptionProbability)
			throws SocketException {
		super(name);
		// Anotar la dirección en la que escucha el servidor de Directorio
		InetSocketAddress serverAddress = new InetSocketAddress(directoryPort);
		
 		// Crear un socket de servidor
		socket = new DatagramSocket(serverAddress);
		
		messageDiscardProbability = corruptionProbability;
		//Inicialización del mapa
		servers = new HashMap<Integer,InetSocketAddress>();
	}

	public void run() {
		byte[] buf = new byte[PACKET_MAX_SIZE];

		System.out.println("Directory starting...");
		boolean running = true;
		while (running) {

			try {
				// 1) Recibir la solicitud por el socket
				DatagramPacket pckt = new DatagramPacket(buf, buf.length);
				socket.receive(pckt);
				
				// 2) Extraer quién es el cliente (su dirección)
				buf = new byte[PACKET_MAX_SIZE];
				InetSocketAddress clientAddr = (InetSocketAddress) pckt.getSocketAddress();
				
				// (Solo Boletín 2) Devolver una respuesta idéntica en contenido a la solicitud
//				pckt = new DatagramPacket(buf, buf.length, ca);
//				socket.send(pckt);
				
				double rand = Math.random();
				if (rand < messageDiscardProbability) {
					System.err.println("Directory DISCARDED corrupt request from... ");
					continue;
				}
				
				
				// 4) Analizar y procesar la solicitud (llamada a processRequestFromCLient)
				processRequestFromClient(pckt.getData(), clientAddr);
				
				// 5) Tratar las excepciones que puedan producirse
			} catch (IOException e) {
				//  Auto-generated catch block
				e.printStackTrace();
			} 
				
				// 3) Vemos si el mensaje debe ser descartado por la probabilidad de descarte

				
				
		}
		socket.close();
	}

	//Método para procesar la solicitud enviada por clientAddr
	public void processRequestFromClient(byte[] data, InetSocketAddress clientAddr) throws IOException {
		
		// 1) Extraemos el tipo de mensaje recibido
		ByteBuffer bb = ByteBuffer.wrap(data);
		int codigo=bb.get();
		int protocolo= bb.getInt();
		
		
//		System.out.println(codigo);
		
		
		switch(codigo) { 
		
		// 2) Procesar el caso de que sea un registro y enviar mediante sendOK
		case COD_REGISTRO:
			if(!servers.containsValue(clientAddr)){
				byte[] iparr= new byte[4];
				bb.get(iparr);
				int puertoNuevo = bb.getInt();
				InetSocketAddress addr = new InetSocketAddress(clientAddr.getAddress().getHostAddress(),puertoNuevo);	
				servers.put(protocolo, addr);
				sendOK(clientAddr);
			}
			else sendNO_OK(clientAddr);
			
//			System.out.println("Registrado: "+ clientAddr.getHostName()+":" +clientAddr.getPort()+ "\tProtocolo: "+ protocolo );
			
			break;
		
			// 3) Procesar el caso de que sea una consulta
		case COD_CONSULTA:
			// 3.1) Devolver una dirección si existe un servidor (sendServerInfo)
			if(servers.containsKey(protocolo)){
				InetSocketAddress consultaAddr = new InetSocketAddress(servers.get(protocolo).getAddress(), servers.get(protocolo).getPort());
//				System.out.println("Consulta: "+ consultaAddr.getHostName()+":" +consultaAddr.getPort()+ "\tProtocolo: "+ protocolo );

				sendServerInfo(consultaAddr, clientAddr);
			}
			// 3.2) Devolver una notificación si no existe un servidor (sendEmpty)
			else sendEmpty(clientAddr);
			break;
		
		}
	}

	//Método para enviar una respuesta vacía (no hay servidor)
	private void sendEmpty(InetSocketAddress clientAddr) throws IOException {
		// Construir respuesta
		//Formato cod(1)
		ByteBuffer bb = ByteBuffer.allocate(1); 
		bb.put(COD_EMPTY); 
		byte[] buf  = bb.array();
		DatagramPacket pckt = new DatagramPacket(buf, buf.length, clientAddr);
		
		// Enviar respuesta
		socket.send(pckt);
	}

	//Método para enviar la dirección del servidor al cliente
	private void sendServerInfo(InetSocketAddress serverAddress, InetSocketAddress clientAddr) throws IOException {
		// Obtener la representación binaria de la dirección
		
		byte[] iparr= serverAddress.getAddress().getAddress(); // primer get addr se obtiene el inetAdrres y con el segundo el array 

		// Construir respuesta
		//formato : cod(1)+ ip(4) + puerto(4)
		ByteBuffer bb = ByteBuffer.allocate(9); 
		bb.put(COD_RESPUESTA_CONSULTA); 
		bb.put(iparr);
		bb.putInt(serverAddress.getPort());
		String cadena="";
		
		for (int i = 0; i<iparr.length;i++) {
			cadena+=iparr[i];
		}
//		System.out.println("ENVIO CONSULTA: "+COD_RESPUESTA_CONSULTA+"\t"+cadena+":"+serverAddress.getPort());
				
		// Enviar respuesta
		byte[] mensaje = bb.array();
		DatagramPacket pckt = new DatagramPacket(mensaje, mensaje.length, clientAddr);
		socket.send(pckt);
	}

	//Método para enviar la confirmación del registro
	private void sendOK(InetSocketAddress clientAddr) throws IOException {
		// Construir respuesta
		//formato: cod(1)
		ByteBuffer bb =  ByteBuffer.allocate(1);
		bb.put(COD_OK);
		DatagramPacket pckt = new DatagramPacket(bb.array(), bb.array().length, clientAddr);
		
		// Enviar respuesta
		socket.send(pckt);
	}
	
	private void sendNO_OK(InetSocketAddress clientAddr) throws IOException {
		//formato: cod(1)
				ByteBuffer bb =  ByteBuffer.allocate(1);
				bb.put(COD_NO_OK);
				DatagramPacket pckt = new DatagramPacket(bb.array(), bb.array().length, clientAddr);
				socket.send(pckt);
	}
}