package Servers;

import Messages.ClientToServerResponse;
import Messages.ClientToZoneMessage;
import Utils.Distribumon;

import java.io.*;
import java.net.*;
import java.util.LinkedHashMap;
import java.util.Scanner;

/**
 * Created by teodorosaavedra on 23-09-16.
 */
public class ZoneServer implements Runnable{

    private MulticastSocket multicast_socket;
    private String server_name;
    private String server_mcastip;
    private int server_mcastport;
    private String server_ip;
    private int server_port;
    private int id_count;
    private DatagramSocket socket;
    private byte[] buffer;

    LinkedHashMap<Integer,Distribumon> dmon_list;

    public ZoneServer(String server_name, String server_mcastip, int server_mcastport, String server_ip, int server_port) {
        this.server_name = server_name;
        this.server_mcastip = server_mcastip;
        this.server_mcastport = server_mcastport;
        this.server_ip = server_ip;
        this.server_port = server_port;
        this.id_count = 0;
        this.dmon_list = new LinkedHashMap<Integer,Distribumon>();
        try{
            multicast_socket = new MulticastSocket(server_mcastport);
            multicast_socket.joinGroup(InetAddress.getByName(server_mcastip));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private int getNewId(){
        id_count = id_count + 1;
        return id_count;
    }

    private void publishDistribumon(String dmon_name, int dmon_level) {
        Distribumon new_dmon = new Distribumon(this.getNewId(),dmon_name,dmon_level);
        dmon_list.put(new_dmon.getId(),new_dmon);

        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutput oo = new ObjectOutputStream(stream);
            oo.writeObject(dmon_list);
            oo.close();

            DatagramPacket packet = new DatagramPacket(stream.toByteArray(),stream.toByteArray().length,InetAddress.getByName(server_mcastip),server_mcastport);
            multicast_socket.send(packet);

            System.out.println("[Servidor Zona:" + this.getServerName() + "]: Se ha publicado al Distribumon: " + new_dmon.getName());
            System.out.println("******");
            System.out.println("id: " + new_dmon.getId());
            System.out.println("nombre: " + new_dmon.getName());
            System.out.println("nivel: " + new_dmon.getLevel());


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getServerName() {
        return server_name;
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(server_port);
            buffer = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while(true){
                socket.receive(packet);

                ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
                ObjectInputStream oos = new ObjectInputStream(stream);
                ClientToZoneMessage message = (ClientToZoneMessage) oos.readObject();

                Distribumon dmon = dmon_list.get(message.getId());
                if(!dmon.isCaptured()) {
                    dmon.setCaptured(true);
                    dmon.setOwner(message.getUserUUID());
                }

                ByteArrayOutputStream response = new ByteArrayOutputStream();
                ObjectOutput oo = new ObjectOutputStream(response);
                oo.writeObject(dmon_list);
                oo.close();

                DatagramPacket rpacket = new DatagramPacket(response.toByteArray(),response.toByteArray().length,InetAddress.getByName(server_mcastip),server_mcastport);
                multicast_socket.send(rpacket);

            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String [ ] args)
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("[Servidor Zona]: Nombre Servidor:");
        String server_name = scanner.nextLine();
        System.out.println("[Servidor Zona:" + server_name + "]: IP Multicast:");
        String server_mcastip = scanner.nextLine();
        //System.out.println("[Servidor Zona:" + server_name + "]: Puerto Multicast:");
        //int server_mcastport = Integer.parseInt(scanner.nextLine());
        System.out.println("[Servidor Zona:" + server_name + "]: IP Peticiones");
        String server_ip = scanner.nextLine();
        System.out.println("[Servidor Zona:" + server_name + "]: Puerto Peticiones");
        int server_port = Integer.parseInt(scanner.nextLine());

        ZoneServer zone_server = new ZoneServer(server_name,server_mcastip,4445,server_ip,server_port);
        Thread receptor = new Thread(zone_server);
        receptor.start();

        while(true){
            System.out.println("[Servidor Zona:" + zone_server.getServerName() + "]: Publicar Distribumon");
            System.out.println("[Servidor Zona:" + zone_server.getServerName() + "]: Introducir nombre");
            String dmon_name = scanner.nextLine();
            System.out.println("[Servidor Zona:" + zone_server.getServerName() + "]: Introducir nivel");
            int dmon_level = Integer.parseInt(String.valueOf(scanner.nextLine()));
            zone_server.publishDistribumon(dmon_name,dmon_level);
        }
    }

}
