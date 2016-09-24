package Utils;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by teodorosaavedra on 23-09-16.
 */
public class Distribumon implements Serializable{

    private int id;
    private String name;
    private int level;
    private boolean captured;
    private UUID owner_id;

    public Distribumon(int id, String name, int level) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.captured = false;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public boolean isCaptured(){
        return captured;
    }

    public UUID getOwner(){
        return owner_id;
    }

    public void setCaptured(boolean flag){
        this.captured = flag;
    }

    public void setOwner(UUID id){
        this.owner_id = id;
    }

}
