package socialnetwork.ui.menu;

import socialnetwork.domain.validators.ValidationException;
import socialnetwork.domain.mainDom.Group;
import socialnetwork.service.GroupService;
import socialnetwork.service.ServiceException;
import socialnetwork.ui.UIMain;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GroupsUI extends UIMain {
    private GroupService groupService;
    private ChatUI chatUI;

    public GroupsUI(GroupService groupService) {
        this.groupService = groupService;
        chatUI = new ChatUI(groupService);
    }

    private String input(String msg) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(msg);
        return scanner.nextLine();
    }

    public void menu() {
        System.out.println("\n----------");
        System.out.println("1. Create group");
        System.out.println("2. Show all groups");
        System.out.println("3. Open group");
        System.out.println("4. Open private chat");
        System.out.println("5. Delete group");
        System.out.println("0. Back");
        System.out.println("----------\n");
    }

    public void run() {
        int idx = -1;

        while (idx != 0) {
            try {
                menu();
                idx = Integer.parseInt(input("Enter option: "));

                try {
                    switch (idx) {
                        case 0:
                            break;
                        case 1:
                            createGroupChat();
                            break;
                        case 2:
                            showAllGroups();
                            break;
                        case 3:
                            openGroup();
                            break;
                        case 4:
                            openPrivateChat();
                            break;
                        case 5:
                            deleteGroupChat();
                            break;
                        default:
                            System.out.println("Invalid option!");
                    }
                } catch (ValidationException | ServiceException e) {
                    System.out.println(e.getMessage());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid data!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid option!");
            }
        }
    }

    private void createGroupChat() {
        String answer = "yes";

        List<Long> members = new ArrayList<>();
        members.add(currentUser.getId());

        String groupName = input("Enter group name: ");
        while (answer.toLowerCase().equals("yes")) {
            Long user = Long.parseLong(input("Enter user id: "));
            members.add(user);
            answer = input("Do you want to add another user to the group: [yes/no] ? ");
        }

        if (members.size() > 2) {
            Group group = groupService.createGroup(groupName, members);
            System.out.println("Group chat created!");
        } else {
            System.out.println("A group must have at least 3 users!");
        }
    }

    private void showAllGroups() {
        List<Group> groups = groupService.getAllUserGroups(currentUser.getId());
        if (groups.size() != 0) {
            groups.forEach(System.out::println);
        } else System.out.println("There are no groups!");
    }

    private void openGroup() {
        Long idGroup = Long.parseLong(input("Enter group id: "));

        Group group = groupService.openGroup(idGroup);
        if (group != null) {
            ChatUI.setCurrentGroup(group);
            System.out.println(ChatUI.getCurrentGroup());
            chatUI.run();
        } else System.out.println("Invalid group!");
    }

    private void openPrivateChat() {
        Long user = Long.parseLong(input("Enter user id: "));
        Group group = groupService.openPrivateChat(currentUser.getId(), user);
        if (group != null) {
            ChatUI.setCurrentGroup(group);
            chatUI.run();
        }
    }

    private void deleteGroupChat() {
        Long idGroup = Long.parseLong(input("Enter group id: "));

        if (groupService.deleteUserGroup(idGroup, currentUser.getId()) == null)
            System.out.println("Invalid id!");
        else {
            System.out.println("Group deleted!");
        }
    }
}
