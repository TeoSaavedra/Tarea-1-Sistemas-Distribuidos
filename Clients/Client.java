package Clients;

import Messages.ClientToServerResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.Scanner;

/**
 * Created by teodorosaavedra on 22-09-16.
 */
public class Client {

    String server_cip;
    int server_cport;
    String server_zone;
    InetAddress ip_address;
    byte[] buffer;
    ClientToServerResponse connected_server;

    public Client(String server_ip, String server_zone){
        try {
            this.server_cip = server_ip;
            this.server_cport = 9876;
            this.server_zone = server_zone;
            this.ip_address = InetAddress.getByName("localhost");
            this.buffer = new byte[2048];
        }catch(UnknownHostException e) {
            e.printStackTrace();
            System.err.println(e);
        }
    }

    public void SendMessageToCentralServer(){
        try{
            DatagramSocket output_socket = new DatagramSocket();
            DatagramPacket output_packet = new DatagramPacket(this.server_zone.getBytes(),this.server_zone.getBytes().length,this.ip_address,this.server_cport);
            output_socket.send(output_packet);

            DatagramSocket input_socket = new DatagramSocket(this.server_cport+1);
            DatagramPacket input_packet = new DatagramPacket(buffer, buffer.length );
            input_socket.receive(input_packet);

            ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
            ObjectInputStream oos = new ObjectInputStream(stream);
            ClientToServerResponse response = (ClientToServerResponse) oos.readObject();

            System.out.print("[Cliente]: Nombre: " + response.getServerName());
            System.out.print(", IP Multicast: " + response.getServerMCastIp());
            System.out.print(", IP Peticiones: " + response.getServerIp());
            System.out.println(", Puerto Peticiones: " + response.getServerPort());

            this.connected_server = response;

        } catch (SocketException e  ) {
            e.printStackTrace();
            System.err.println(e);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e);
        } catch (ClassNotFoundException e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }

    public static void main(String [ ] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("[Cliente]: Ingresar IP Servidor Central");
        String server_ip = scanner.nextLine();
        System.out.println("[Cliente]: Introducir Nombre de Zona a explorar, Ej: Casa Central, San Joaquin");
        String server_zone = scanner.nextLine();

        Client client = new Client(server_ip,server_zone);
        client.SendMessageToCentralServer();

    }
}
