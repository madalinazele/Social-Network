package socialnetwork.utils;

import socialnetwork.domain.Entity;

public class ChangeEvent<E extends Entity> {
    private ChangeEventType type;
    private E data;

    public ChangeEvent(ChangeEventType type) {
        this.type = type;
    }

    public ChangeEvent(ChangeEventType type, E data) {
        this.type = type;
        this.data = data;
    }

    public ChangeEventType getType() {
        return type;
    }

    public E getData() {
        return data;
    }

    public void setData(E data) {
        this.data = data;
    }
}
