package es.um.redes.nanoChat.directory.connector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	// Tamaño máximo del paquete UDP (los mensajes intercambiados son muy cortos)
	private static final int PACKET_MAX_SIZE = 128;
	// Puerto en el que atienden los servidores de directorio
	private static final int DEFAULT_PORT = 6868;
	// Valor del TIMEOUT
	private static final int TIMEOUT = 1000;

	// CODIGOS
	private static final byte COD_OK = 1;
	private static final byte COD_EMPTY = 2;
	private static final byte COD_NO_OK = 3;
	private static final byte COD_REGISTRO = 4;
	private static final byte COD_CONSULTA = 5;
	private static final byte COD_RESPUESTA_CONSULTA = 6;
	// resto de codigos

	private DatagramSocket socket; // socket UDP
	private InetSocketAddress directoryAddress; // dirección del servidor de directorio
	private String myAddress;

	public DirectoryConnector(String agentAddress) throws IOException {

		myAddress = agentAddress;
		// A partir de la dirección y del puerto generar la dirección de conexión para
		// el Socket
		directoryAddress = new InetSocketAddress(InetAddress.getByName(agentAddress), DEFAULT_PORT);
		// Crear el socket UDP
		socket = new DatagramSocket();
	}

	/**
	 * Envía una solicitud para obtener el servidor de chat asociado a un
	 * determinado protocolo
	 * 
	 */
	public InetSocketAddress getServerForProtocol(int protocol) throws IOException {

		// Generar el mensaje de consulta llamando a buildQuery()
		byte[] req = buildQuery(protocol);

		// Construir el datagrama con la consulta
		DatagramPacket pckt = new DatagramPacket(req, req.length, directoryAddress);

		// Enviar datagrama por el socket
		socket.send(pckt);

		// preparar el buffer para la respuesta
		byte[] response = new byte[PACKET_MAX_SIZE];
		pckt = new DatagramPacket(response, response.length);

		int intentos = 5;
		while (intentos > 0) {
			try {
				// Establecer el temporizador para el caso en que no haya respuesta
				// Recibir la respuesta
				socket.setSoTimeout(TIMEOUT);
				socket.receive(pckt);

			} catch (IOException e) {
				System.out.println("TIMEOUT EXCEDIDO, vuelve a mandar el mensaje");
				intentos--;
			}
		}
		
		if (intentos == 0) return null;

		// Procesamos la respuesta para devolver la dirección que hay en ella
		response = pckt.getData();

		// formato : cod(1)+ ip(4) + puerto(4)
		InetSocketAddress respuesta = getAddressFromResponse(pckt);

		return respuesta;
	}

	// Método para generar el mensaje de consulta (para obtener el servidor asociado
	// a un protocolo)
	private byte[] buildQuery(int protocol) {
		// Devolvemos el mensaje codificado en binario según el formato acordado

		// formato : cod(1) + protocolo(4)
		ByteBuffer bb = ByteBuffer.allocate(5);
		bb.put(COD_CONSULTA);
		bb.putInt(protocol);
		return bb.array();
	}

	// Método para obtener la dirección de internet a partir del mensaje UDP de
	// respuesta
	private InetSocketAddress getAddressFromResponse(DatagramPacket packet) throws UnknownHostException {

		// formato : cod(1)+ ip(4) + puerto(4)

		ByteBuffer ret = ByteBuffer.wrap(packet.getData());
		int opCode = ret.get();
		byte[] array = new byte[4];
		ret.get(array);
		int port = ret.getInt();

//		System.out.println("RESPUESTA CONSULTA: "+ opCode+"\t"+InetAddress.getByAddress(array)+" puerto: "+port);

		InetSocketAddress inet = new InetSocketAddress(InetAddress.getByAddress(array), port);

		// Analizar si la respuesta no contiene dirección (devolver null)

		if (inet.getAddress() == null)
			return null;

		// Si la respuesta no está vacía, devolver la dirección (extraerla del mensaje)

		return inet;

	}

	/**
	 * Envía una solicitud para registrar el servidor de chat asociado a un
	 * determinado protocolo
	 * 
	 */
	public boolean registerServerForProtocol(int protocol, int port) throws IOException {

		// Construir solicitud de registro (buildRegistration)
		byte[] solicitud = buildRegistration(protocol, port);
		// Enviar solicitud
		DatagramPacket pckt = new DatagramPacket(solicitud, solicitud.length, directoryAddress);
		socket.send(pckt);
		// Recibe respuesta
//		socket.receive(pckt);
		
		int intentos = 5;
		while (intentos > 0) {
			try {
				// Establecer el temporizador para el caso en que no haya respuesta
				// Recibir la respuesta
				socket.setSoTimeout(TIMEOUT);
				socket.receive(pckt);

			} catch (IOException e) {
				System.out.println("TIMEOUT EXCEDIDO, vuelve a mandar el mensaje de registro");
				intentos--;
			}
		}
		
		if (intentos == 0) return false;
		// Procesamos la respuesta para ver si se ha podido registrar correctamente
		ByteBuffer bb = ByteBuffer.wrap(pckt.getData());
		int codigo = bb.get();
//		System.out.println(codigo);
		if (codigo == COD_OK) {
//			System.out.println("Registrado con éxito");
			return true;
		
		} else {
//			System.out.println("No se ha podido registrar");
			return false;
		}
	}

	// Método para construir una solicitud de registro de servidor
	// OJO: No hace falta proporcionar la dirección porque se toma la misma desde la
	// que se envió el mensaje
	private byte[] buildRegistration(int protocol, int port) {

		// Devolvemos el mensaje codificado en binario según el formato acordado

		// formato: cod(1) + protocolo(4) + ip(4) + puerto(4)
		ByteBuffer bb = ByteBuffer.allocate(13);
		bb.put(COD_REGISTRO);
		bb.putInt(protocol);

		// ---- poner la direccion nuestra (cliente)
		InetSocketAddress as = new InetSocketAddress(myAddress, port);
		byte[] iparr = as.getAddress().getAddress();
		// directoryAddress.getAddress().getAddress();
		bb.put(iparr);
		bb.putInt(port);
		return bb.array();

	}

	public void close() {
		socket.close();
	}
}
