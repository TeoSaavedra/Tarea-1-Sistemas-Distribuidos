package Servers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Scanner;
import Messages.ClientToServerResponse;

/**
 * Created by teodorosaavedra on 20-09-16.
 */
public class CentralServer implements Runnable{

    private HashMap<String,ClientToServerResponse> diccionario;
    private int port;
    private DatagramSocket socket;
    private byte[] buffer;

    public CentralServer(){
        diccionario = new HashMap<String, ClientToServerResponse>();
        port = 49876;
        buffer = new byte[2048];
    }

    public void addToDictionary(ClientToServerResponse server){
        diccionario.put(server.getServerName(),server);
    }

    public ClientToServerResponse getResponse(String server_name){
        return diccionario.get(server_name);
    }

    public void SendResponsetoClient(ClientToServerResponse response, InetAddress address){
        try{
            DatagramSocket socket = new DatagramSocket();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutput oo = new ObjectOutputStream(stream);
            oo.writeObject(response);
            oo.close();

            DatagramPacket packet = new DatagramPacket(stream.toByteArray(),stream.toByteArray().length , address, port+1);
            socket.send(packet);
            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try{
            socket = new DatagramSocket(this.port);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (true) {
                socket.receive(packet);
                String server_name = new String(buffer, 0, packet.getLength());
                System.out.println("[Servidor Central] Respuesta a " + packet.getAddress() + " por " + server_name);

                ClientToServerResponse response = this.getResponse(server_name);
                if(response != null){
                    System.out.print("[Servidor Central]: Nombre: " + response.getServerName());
                    System.out.print(", IP Multicast: " + response.getServerMCastIp());
                    System.out.print(", IP Peticiones: " + response.getServerIp());
                    System.out.println(", Puerto Peticiones: " + response.getServerPort());
                    this.SendResponsetoClient(response,packet.getAddress());
                }
                packet.setLength(buffer.length);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String [ ] args)
    {

        CentralServer central_server = new CentralServer();
        Thread receptor = new Thread(central_server);
        receptor.start();
        Scanner scanner = new Scanner(System.in);

        while(true) {
            System.out.println("[Servidor Central]: Agregar Servidor de Zona");
            System.out.println("[Servidor Central]: Nombre:");
            String server_name = scanner.nextLine();
            System.out.println("[Servidor Central]: IP Multicast:");
            String server_mcastip = scanner.nextLine();
            System.out.println("[Servidor Central]: IP Peticiones:");
            String server_ip = scanner.nextLine();
            System.out.println("[Servidor Central]: Puerto Peticiones:");
            String server_port = scanner.nextLine();

            ClientToServerResponse server = new ClientToServerResponse(server_name, server_mcastip, server_ip, server_port);
            central_server.addToDictionary(server);
            System.out.println("[Servidor Central]: Servidor Agregado.");
        }

    }

}

