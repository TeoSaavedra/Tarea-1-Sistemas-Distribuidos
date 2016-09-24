package Messages;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by teodorosaavedra on 23-09-16.
 */
public class ClientToZoneMessage implements Serializable{

    private int id;
    private UUID user_UUID;

    public ClientToZoneMessage(int id,UUID user_UUID){
        this.id = id;
        this.user_UUID = user_UUID;
    }


    public int getId() {
        return id;
    }

    public UUID getUserUUID() {
        return user_UUID;
    }
}
