package socialnetwork.ui.menu;

import socialnetwork.service.GroupService;
import socialnetwork.service.UserService;
import socialnetwork.ui.UIMain;

import java.util.Scanner;

public class MenuUI extends UIMain {
    private RelationUI relUI;
    private UserUI userUI;
    private GroupsUI groupsUI;
    private FriendRequestUI friendRequestUI;

    public MenuUI(UserService srv, GroupService groupService) {
        relUI = new RelationUI(srv);
        userUI = new UserUI(srv);
        groupsUI = new GroupsUI(groupService);
        friendRequestUI = new FriendRequestUI(srv);
    }

    private String input(String msg) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(msg);
        return scanner.nextLine();
    }

    public void menu() {
        System.out.println("\n----------");
        System.out.println("1. Settings");
        System.out.println("2. Chat");
        System.out.println("3. Friendships");
        if (!currentUser.getRole().equals("admin"))
            System.out.println("4. Friend requests");
        System.out.println("0. Back");
        System.out.println("----------\n");
    }

    public void run() {
        int idx = -1;

        while (idx != 0) {
            try {
                menu();
                idx = Integer.parseInt(input("Enter option: "));

                switch (idx) {
                    case 0:
                        currentUser = null;
                        break;
                    case 1:
                        userUI.run();
                        if (currentUser == null)
                            return;
                        break;
                    case 2:
                        groupsUI.run();
                        break;
                    case 3:
                        relUI.run();
                        break;
                    case 4:
                        if (!currentUser.getRole().equals("admin")) {
                            friendRequestUI.run();
                            break;
                        }
                    default:
                        System.out.println("Invalid option!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid option!");
            }
        }
    }

}
