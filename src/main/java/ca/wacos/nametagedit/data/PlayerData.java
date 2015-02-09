package ca.wacos.nametagedit.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PlayerData {

    private String name;
    private String uuid;
    private String prefix;
    private String suffix;

    public PlayerData() {

    }
}