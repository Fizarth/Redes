package es.um.redes.nanoChat.directory.connector;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	//Tamaño máximo del paquete UDP (los mensajes intercambiados son muy cortos)
	private static final int PACKET_MAX_SIZE = 128;
	//Puerto en el que atienden los servidores de directorio
	private static final int DEFAULT_PORT = 6868;
	//Valor del TIMEOUT
	private static final int TIMEOUT = 1000;
	
	
	//CODIGOS 
		private static final byte COD_OK = 1;
		private static final byte COD_EMPTY = 2;
		private static final byte COD_NO_OK = 3;
		private static final byte COD_REGISTRO = 4;
		private static final byte COD_CONSULTA = 5;
		private static final byte COD_RESPUESTA_CONSULTA = 6;
		//TODO  resto de codigos
		

	private DatagramSocket socket; // socket UDP
	private InetSocketAddress directoryAddress; // direcciï¿½n del servidor de directorio

	public DirectoryConnector(String agentAddress) throws IOException {
		//TODO A partir de la dirección y del puerto generar la dirección de conexión para el Socket
		directoryAddress = new InetSocketAddress(InetAddress.getByName(agentAddress), DEFAULT_PORT);
		//TODO Crear el socket UDP
		socket  = new DatagramSocket();
		

	}
	
	public void enviarPrueba(String s) throws IOException {
		byte[] req = s.getBytes();
		DatagramPacket packet = new DatagramPacket(req, req.length, directoryAddress);
		socket.send(packet);
	}

	/**
	 * Envï¿½a una solicitud para obtener el servidor de chat asociado a un determinado protocolo
	 * 
	 */
	public InetSocketAddress getServerForProtocol(int protocol) throws IOException {

		//TODO Generar el mensaje de consulta llamando a buildQuery()
		byte[] req = new byte [PACKET_MAX_SIZE]; 
		//req= buildQuery(protocol);
		//TODO Construir el datagrama con la consulta
		DatagramPacket packet = new DatagramPacket(req, req.length, directoryAddress);
		
		//TODO Enviar datagrama por el socket
		socket.send(packet);
		
		//TODO preparar el buffer para la respuesta
		byte[] response = new byte [PACKET_MAX_SIZE];
		packet = new DatagramPacket(response, response.length);
		
		
		//TODO Establecer el temporizador para el caso en que no haya respuesta
		socket.setSoTimeout(TIMEOUT);
		//TODO Recibir la respuesta
		socket.receive(packet);
		//TODO Procesamos la respuesta para devolver la dirección que hay en ella
		ByteArrayInputStream response2 = new ByteArrayInputStream(packet.getData());

		
//		enviarPrueba("holi");

		
		return null;
	}


	//Método para generar el mensaje de consulta (para obtener el servidor asociado a un protocolo)
	private byte[] buildQuery(int protocol) {
		//TODO Devolvemos el mensaje codificado en binario según el formato acordado
		
		//formato : cod(1) + protocolo(4)
		
		return null;
	}

	//Método para obtener la dirección de internet a partir del mensaje UDP de respuesta
	private InetSocketAddress getAddressFromResponse(DatagramPacket packet) throws UnknownHostException {
		//TODO Analizar si la respuesta no contiene direcciï¿½n (devolver null)
		//TODO Si la respuesta no está vacía, devolver la dirección (extraerla del mensaje)
		return null;
	}
	
	/**
	 * Envï¿½a una solicitud para registrar el servidor de chat asociado a un determinado protocolo
	 * 
	 */
	public boolean registerServerForProtocol(int protocol, int port) throws IOException {

		//TODO Construir solicitud de registro (buildRegistration)
		//TODO Enviar solicitud
		//TODO Recibe respuesta
		//TODO Procesamos la respuesta para ver si se ha podido registrar correctamente
		
		
		//formato: cod(1) + protocolo(4) + ip(4) + puerto(4)
		return false;
	}


	//Método para construir una solicitud de registro de servidor
	//OJO: No hace falta proporcionar la dirección porque se toma la misma desde la que se enviï¿½ el mensaje
	private byte[] buildRegistration(int protocol, int port) {
		//TODO Devolvemos el mensaje codificado en binario segï¿½n el formato acordado
		return null;
	}

	public void close() {
		socket.close();
	}
}
