package Messages;

import java.io.Serializable;

/**
 * Created by teodorosaavedra on 20-09-16.
 */
public class ClientToServerResponse implements Serializable {

    private String server_name;
    private String server_mcastip;
    private String server_ip;
    private String server_port;

    public ClientToServerResponse(String server_name, String server_mcastip, String server_ip, String server_port){
        this.server_name = server_name;
        this.server_mcastip = server_mcastip;
        this.server_ip = server_ip;
        this.server_port = server_port;
    }

    public String getServerName() {
        return this.server_name;
    }

    public String getServerMCastIp() {
        return this.server_mcastip;
    }

    public String getServerIp() {
        return this.server_ip;
    }

    public String getServerPort() {
        return this.server_port;
    }
}
