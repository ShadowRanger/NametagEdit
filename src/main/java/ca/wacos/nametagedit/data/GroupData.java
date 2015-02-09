package ca.wacos.nametagedit.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GroupData {

    private String groupName;
    private String prefix;
    private String suffix;
    private String permission;
    
    public GroupData() {
        
    }
}