package es.um.redes.nanoChat.directory.server;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashMap;


public class DirectoryThread extends Thread {

	int a;
	private static final int PACKET_MAX_SIZE = 128; //Tamaño máximo del paquete UDP
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

				
			DatagramPacket pckt = new DatagramPacket(buf, buf.length);
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
				
				//TODO (Solo Boletín 2) Devolver una respuesta idéntica en contenido a la solicitud
				
				// 4) Analizar y procesar la solicitud (llamada a processRequestFromCLient)
			InetSocketAddress ca= (InetSocketAddress) pckt.getSocketAddress();
			try {
				processRequestFromClient(buf, ca);
			} catch (IOException e) {
				
				e.printStackTrace();
			}
				
		}
		socket.close();
	}

	//Método para procesar la solicitud enviada por clientAddr
	public void processRequestFromClient(byte[] data, InetSocketAddress clientAddr) throws IOException {
		//TODO 1) Extraemos el tipo de mensaje recibido
		DatagramPacket pckt = new DatagramPacket(data, data.length, clientAddr);
		//TODO 2) Procesar el caso de que sea un registro y enviar mediante sendOK
		sendOK(clientAddr);
		//TODO 3) Procesar el caso de que sea una consulta
		//TODO 3.1) Devolver una dirección si existe un servidor (sendServerInfo)
		//TODO 3.2) Devolver una notificación si no existe un servidor (sendEmpty)
		
		
		
		// Send response message back to client at address
		
		
		
		
	}

	//Método para enviar una respuesta vacía (no hay servidor)
	private void sendEmpty(InetSocketAddress clientAddr) throws IOException {
		//TODO Construir respuesta
		//TODO Enviar respuesta
		
		byte[] buf = new byte[PACKET_MAX_SIZE]; // Prepare response message
		// Send response message back to client at address
		DatagramPacket pckt = new DatagramPacket(buf, buf.length, clientAddr);
		socket.send(pckt);
	}

	//Método para enviar la dirección del servidor al cliente
	private void sendServerInfo(InetSocketAddress serverAddress, InetSocketAddress clientAddr) throws IOException {
		//TODO Obtener la representación binaria de la dirección
		//TODO Construir respuesta
		//TODO Enviar respuesta
	}

	//Método para enviar la confirmación del registro
	private void sendOK(InetSocketAddress clientAddr) throws IOException {
		//TODO Construir respuesta
		//TODO Enviar respuesta
		
		byte[] buf = new byte[PACKET_MAX_SIZE]; // Prepare response message
		// Send response message back to client at address
		DatagramPacket pckt = new DatagramPacket(buf, buf.length, clientAddr);
		socket.send(pckt);
	}
}
