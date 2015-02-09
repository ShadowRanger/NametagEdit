package ca.wacos.nametagedit.core;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamHandler {

    private String name;
    private String prefix;
    private String suffix;

    public TeamHandler(String name) {
        this.name = name;
    }
}