package socialnetwork.domain.mainDom;

import socialnetwork.domain.Entity;

import java.util.List;

public class Group extends Entity<Long> {

    private String groupName;
    private List<Long> members;

    public Group(String groupName) {
        this.groupName = groupName;
    }

    public List<Long> getMembers() {
        return members;
    }

    public void setMembers(List<Long> members) {
        this.members = members;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "Id: " + this.getId() +
                " |GroupName: " + groupName;
    }
}
