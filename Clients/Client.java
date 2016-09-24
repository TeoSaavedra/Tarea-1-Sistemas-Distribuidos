package Clients;

import Messages.ClientToServerResponse;
import Messages.ClientToZoneMessage;
import Utils.Distribumon;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created by teodorosaavedra on 22-09-16.
 */
public class Client implements Runnable{

    int server_cport;
    String server_zone;
    InetAddress ip_address;
    byte[] buffer;
    ClientToServerResponse connected_server;
    MulticastSocket multicast_socket;
    boolean subscribed;
    UUID client_id;
    ArrayList<Distribumon> dmon_captured;
    LinkedHashMap<Integer,Distribumon> dmon_inzone;


    public Client(){
        this.server_cport = 9876;
        this.buffer = new byte[2048];
        this.subscribed = false;
        this.client_id = UUID.randomUUID();
        this.dmon_captured = new ArrayList<Distribumon>();
        this.dmon_inzone = new LinkedHashMap<Integer,Distribumon>();
    }

    public void setServerIp(String server_ip){
        try {
            this.ip_address = InetAddress.getByName(server_ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void setServerZone(String server_zone){
        this.server_zone = server_zone;
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

            output_socket.close();
            input_socket.close();

        } catch (SocketException e  ) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void listDistribumonsInZone(){
        System.out.println("******");
        for(Map.Entry<Integer,Distribumon> entry : dmon_inzone.entrySet()){
            System.out.print("id: " + entry.getValue().getId() + ", ");
            System.out.print("nombre: " + entry.getValue().getName() + ", ");
            System.out.println("nivel: " + entry.getValue().getLevel());
        }
        System.out.println("******");
    }

    private void listDistribumonsCaptured(){
        System.out.println("******");
        for(Distribumon entry : dmon_captured){
            System.out.print("id: " + entry.getId() + ", ");
            System.out.print("nombre: " + entry.getName() + ", ");
            System.out.println("nivel: " + entry.getLevel());
        }
        System.out.println("******");
    }

    private void captureDistribumon(Scanner scanner){
        System.out.println("******");
        for(Map.Entry<Integer,Distribumon> entry : dmon_inzone.entrySet()){
            System.out.print("id: " + entry.getValue().getId() + ", ");
            System.out.print("nombre: " + entry.getValue().getName() + ", ");
            System.out.println("nivel: " + entry.getValue().getLevel());
        }
        System.out.println("******");
        System.out.println("[Cliente]: Seleccionar el id de un Distribumon:");
        int id = scanner.nextInt();

        try {
            ClientToZoneMessage message = new ClientToZoneMessage(id,this.client_id);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutput oo = new ObjectOutputStream(stream);
            oo.writeObject(message);
            oo.close();

            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(stream.toByteArray(),stream.toByteArray().length,InetAddress.getByName(connected_server.getServerIp()),Integer.parseInt(connected_server.getServerPort()));
            socket.send(packet);
            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void SubscribeToZoneServer(){
        Thread subscriber = new Thread(this);
        subscriber.start();
        subscribed = true;
        Scanner scanner = new Scanner(System.in);
        while(subscribed){
            System.out.println("[Cliente]: Consola");
            System.out.println("[Cliente]: (1) Listar Distribumones en Zona");
            System.out.println("[Cliente]: (2) Cambiar Zona");
            System.out.println("[Cliente]: (3) Capturar Distribumon");
            System.out.println("[Cliente]: (4) Listar Distribumones Capturados");
            System.out.println("[Cliente]: Seleccionar una opción:");
            int option = scanner.nextInt();
            System.out.println("[Cliente]: Opcion " + option);
            if(option == 1){
                this.listDistribumonsInZone();
            }else if(option == 2){
                subscribed = false;
            }else if(option == 3){
                this.captureDistribumon(scanner);
            }else if(option == 4){
                this.listDistribumonsCaptured();
            }
        }
        multicast_socket.close();
        subscriber.interrupt();
    }

    @Override
    public void run() {
        try {
            multicast_socket = new MulticastSocket(4445);
            multicast_socket.joinGroup(InetAddress.getByName(connected_server.getServerMCastIp()));
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while(subscribed){
                multicast_socket.receive(packet);

                ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
                ObjectInputStream oos = new ObjectInputStream(stream);
                Distribumon new_dmon = (Distribumon) oos.readObject();

                if(new_dmon.isCaptured()){
                    if(this.client_id.equals(new_dmon.getOwner())){
                        System.out.println("[Cliente]: Se atrapo un Distribumon!: " + new_dmon.getName());
                        this.dmon_captured.add(new_dmon);
                        this.dmon_inzone.remove(new_dmon.getId());
                    }else{
                        System.out.println("[Cliente]: Fue atrapado un Distribumon!: " + new_dmon.getName());
                        this.dmon_inzone.remove(new_dmon.getId());
                    }
                }else{
                    System.out.println("[Cliente]: Aparece nuevo Distribumon!: " + new_dmon.getName());
                    this.dmon_inzone.put(new_dmon.getId(),new_dmon);
                }


            }
        } catch (IOException e) {
            subscribed = false;
            //System.out.println("[Cliente]: Se desinscribio de la Zona: " + this.connected_server.getServerName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            subscribed = false;
        }
    }

    public static void main(String [ ] args) {

        Scanner scanner = new Scanner(System.in);
        Client client = new Client();

        while(true) {
            System.out.println("[Cliente]: Ingresar IP Servidor Central");
            String server_ip = scanner.nextLine();
            System.out.println("[Cliente]: Introducir Nombre de Zona a explorar, Ej: Casa Central, San Joaquin");
            String server_zone = scanner.nextLine();

            client.setServerIp(server_ip);
            client.setServerZone(server_zone);
            client.SendMessageToCentralServer();
            client.SubscribeToZoneServer();

        }

    }
}
