package ca.wacos.nametagedit.core;

public class TeamHandler {

    private final String name;
    private String prefix;
    private String suffix;

    public TeamHandler(String name) {
        this.name = name;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getName() {
        return name;
    }
}
