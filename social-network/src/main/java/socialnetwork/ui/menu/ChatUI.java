package socialnetwork.ui.menu;

import socialnetwork.domain.validators.ValidationException;
import socialnetwork.domain.mainDom.Group;
import socialnetwork.domain.mainDom.Message;
import socialnetwork.service.GroupService;
import socialnetwork.service.ServiceException;
import socialnetwork.ui.UIMain;

import java.util.List;
import java.util.Scanner;

public class ChatUI extends UIMain {

    private static Group currentGroup;
    private GroupService groupService;

    public ChatUI(GroupService groupService) {
        this.groupService = groupService;
    }

    public static void setCurrentGroup(Group currentGroup) {
        ChatUI.currentGroup = currentGroup;
    }

    public static Group getCurrentGroup() {
        return currentGroup;
    }

    private String input(String msg) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(msg);
        return scanner.nextLine();
    }

    public void menu() {
        System.out.println("\n----------");
        System.out.println("1. Show all messages from group");
        System.out.println("2. Show group members");
        System.out.println("3. Send message");
        System.out.println("4. Reply message");
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
                            showAllMessages();
                            break;
                        case 2:
                            showMembers();
                            break;
                        case 3:
                            sendMessage();
                            break;
                        case 4:
                            replyMessage();
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

    private void showAllMessages() {
        List<Message> messages = groupService.getAllMessagesFromGroup(currentGroup);
        if (messages.size() == 0)
            System.out.println("There are no messages!");
        else {
            for (Message msg : messages) {
                System.out.println(msg);
                if (msg.getReply() != null) {
                    Message replyM = groupService.findMessageInGroup(msg.getReply(), currentGroup);
                    System.out.println("        Replied to: " + replyM);
                }
            }
        }
    }

    private void showMembers() {
        groupService.getAllMembersFromGroup(currentGroup).forEach(System.out::println);
    }

    private void sendMessage() {
        String message = input("Enter text: ");
        if (groupService.sendMessageToGroup(currentUser, currentGroup, message) == null)
            System.out.println("Message sent!");
        else System.out.println("Invalid data!");
    }

    private void replyMessage() {
        Long idMsg = Long.parseLong(input("Enter message id: "));
        String message = input("Enter text message: ");
        if (groupService.replyMessage(currentUser, idMsg, currentGroup, message) == null)
            System.out.println("Message reply sent!");
        else System.out.println("Invalid data!");
    }
}
