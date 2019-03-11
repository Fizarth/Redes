package es.um.redes.nanoChat.directory.server;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashMap;


public class DirectoryThread extends Thread {
	private static final int PACKET_MAX_SIZE = 128; //Tama�o m�ximo del paquete UDP
	
	//CODIGOS 
	private static final byte COD_OK = 1;
	private static final byte COD_EMPTY = 2;
	private static final byte COD_NO_OK = 3;
	private static final byte COD_REGISTRO = 4;
	private static final byte COD_CONSULTA = 5;
	private static final byte COD_RESPUESTA_CONSULTA = 6;
	//TODO  resto de codigos
	
	
	protected HashMap<Integer,InetSocketAddress> servers; //Estructura para guardar las asociaciones ID_PROTOCOLO -> Dirección del servidor

	protected DatagramSocket socket = null; //Socket de comunicación UDP
	protected double messageDiscardProbability; //Probabilidad de descarte del mensaje
	 

	public DirectoryThread(String name, int directoryPort,double corruptionProbability) throws SocketException {
		
		super(name);
		
		InetSocketAddress serverAddress = new InetSocketAddress(directoryPort);
		socket = new DatagramSocket(serverAddress);
		
		messageDiscardProbability = corruptionProbability;
		
		
		servers = new HashMap<Integer,InetSocketAddress>();
	}

	public void run() {
		byte[] buf = new byte[PACKET_MAX_SIZE];

		System.out.println("Directory starting...");
		boolean running = true;
		while (running) {

			try {	
				DatagramPacket pckt = new DatagramPacket(buf, buf.length);
				socket.receive(pckt);
				byte [] b = pckt.getData();
				String s = new String(b);
				System.out.println(s);
				//extraer direccion del paquete
				InetSocketAddress ca= (InetSocketAddress) pckt.getSocketAddress();
				// Receive request message
				
				try {
					this.socket.receive(pckt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("error al recibir");
				} 
					
				
				InetAddress clienteAddr=pckt.getAddress();
					// 3) Vemos si el mensaje debe ser descartado por la probabilidad de descarte
	
				double rand = Math.random();
				if (rand < messageDiscardProbability) {
					System.err.println("Directory DISCARDED corrupt request from... ");
					continue;
				}
				processRequestFromClient(pckt.getData(), ca);
					
					//TODO (Solo Boletín 2) Devolver una respuesta idéntica en contenido a la solicitud
				System.out.println("Envio algo");
				socket.send(pckt);
					
					// 4) Analizar y procesar la solicitud (llamada a processRequestFromCLient)
				
				
			}catch(IOException e) {}
				
		}
		socket.close();
	}

	//Metodo para procesar la solicitud enviada por clientAddr
	public void processRequestFromClient(byte[] data, InetSocketAddress clientAddr) throws IOException {
		// Extraemos el tipo de mensaje recibido
		ByteBuffer bb = ByteBuffer.wrap(data);
		int codigo=bb.get();
		int protocolo;
		
		switch(codigo) { 
		case COD_CONSULTA:
			// Procesar el caso de que sea una consulta
			protocolo = bb.getInt();
			// Devolver una dirección si existe un servidor (sendServerInfo)
			if (servers.containsKey(protocolo))
				sendServerInfo(servers.get(protocolo),clientAddr);
			else 
				//Devolver una notificación si no existe un servidor (sendEmpty)
				sendEmpty(clientAddr);
			break;
		case COD_REGISTRO: //añadirlo al hash		
			
			System.out.println("Mensaje: REGISTRO");
			
			protocolo = bb.getInt();
			servers.put(protocolo, clientAddr);
			//Procesar el caso de que sea un registro y enviar mediante sendOK
			sendOK(clientAddr);
			break;
		}
		// Send response message back to client at address

	}

	//Metodo para enviar una respuesta vacía (no hay servidor)
	private void sendEmpty(InetSocketAddress clientAddr) throws IOException {
		//TODO Construir respuesta
		//TODO Enviar respuesta
		
		// Prepare response message
		// Send response message back to client at address
		ByteBuffer bb = ByteBuffer.allocate(9); 
		bb.put(COD_EMPTY); 
		byte[] buf  = bb.array();
		DatagramPacket pckt = new DatagramPacket(buf, buf.length, clientAddr);
		socket.send(pckt);
	}

	//Método para enviar la dirección del servidor al cliente
	private void sendServerInfo(InetSocketAddress serverAddress, InetSocketAddress clientAddr) throws IOException {
		
		//formato : cod(1)+ ip(4) + puerto(4)
		byte[] iparr= serverAddress.getAddress().getAddress(); // primer get addr se obtiene el inetAdrres y con el segundo el array 
		ByteBuffer bb = ByteBuffer.allocate(9); 
		bb.put(COD_RESPUESTA_CONSULTA); 
		bb.put(iparr);
		bb.putInt(serverAddress.getPort()); 
		
		byte[] mensaje = bb.array();
		DatagramPacket pckt = new DatagramPacket(mensaje, mensaje.length, clientAddr);
		socket.send(pckt);
		
	}

	//Método para enviar la confirmación del registro
	private void sendOK(InetSocketAddress clientAddr) throws IOException {
		//ByteBuffer bb =  ByteBuffer.allocate(1);
		//bb.put(COD_OK);
		//DatagramPacket pckt = new DatagramPacket(bb.array(), bb.array().length, clientAddr);
		
		byte[] mensaje = new byte[1];
		mensaje[0] = COD_OK;
		DatagramPacket dp = new DatagramPacket(mensaje, 1,clientAddr);
		socket.send(dp);
	}
}
