package ca.wacos.nametagedit.data;

public class PlayerData {

    private String name;
    private String uuid;
    private String prefix;
    private String suffix;
    
    public PlayerData() {
        
    }
    
    public PlayerData(String name, String uuid, String prefix, String suffix) {
        this.name = name;
        this.uuid = uuid;
        this.prefix = prefix;
        this.suffix = suffix;
    }
    
    public String getName() {
        return name;
    }
    
    public String getUUID() {
        return uuid;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public String getSuffix() {
        return suffix;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}