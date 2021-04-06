package socialnetwork.ui;

import socialnetwork.domain.mainDom.User;

public abstract class UIMain {

    public static User currentUser = null;

    public abstract void run();
}
