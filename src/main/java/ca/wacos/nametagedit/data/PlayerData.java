package ca.wacos.nametagedit.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class PlayerData {

    private String name;
    private UUID uuid;
    private String prefix;
    private String suffix;

    public PlayerData() {

    }
}