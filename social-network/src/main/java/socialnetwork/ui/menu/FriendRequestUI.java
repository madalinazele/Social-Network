package socialnetwork.ui.menu;

import socialnetwork.domain.dto.FriendRequestDTO;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.service.ServiceException;
import socialnetwork.service.UserService;
import socialnetwork.ui.UIMain;

import java.util.List;
import java.util.Scanner;

public class FriendRequestUI extends UIMain {

    private UserService userService;

    public FriendRequestUI(UserService userService) {
        this.userService = userService;
    }

    private String input(String msg) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(msg);
        return scanner.nextLine();
    }

    private void menu() {
        System.out.println("\n----------");
        System.out.println("1. Request friendship");
        System.out.println("2. Cancel friend request");
        System.out.println("3. Respond friend request");
        System.out.println("4. Show all friend requests");
        System.out.println("5. Show all sent friend requests");
        System.out.println("0. Back");
        System.out.println("----------\n");
    }

    @Override
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
                            this.requestFriendship();
                            break;
                        case 2:
                            this.cancelFriendRequest();
                            break;
                        case 3:
                            this.respondFriendRequest();
                            break;
                        case 4:
                            this.showAllFriendRequest();
                            break;
                        case 5:
                            this.showAllSentRequests();
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

    public void requestFriendship() {
        Long id1 = currentUser.getRole().equalsIgnoreCase("admin") ? Long.parseLong(input("Enter user id: ")) : currentUser.getId();
        Long id2 = Long.parseLong(input("Enter user id: "));

        if (userService.sendFriendRequest(id1, id2) == null) {
            System.out.println("Friend request sent!");
        } else System.out.println("Friend request not sent");
    }

    private void cancelFriendRequest() {
        Long id1 = currentUser.getRole().equalsIgnoreCase("admin") ? Long.parseLong(input("Enter user id: ")) : currentUser.getId();
        Long id2 = Long.parseLong(input("Enter user id: "));

        if (userService.cancelFriendRequest(id1, id2) != null)
            System.out.println("Friend request canceled!");
    }

    private void respondFriendRequest() {
        Long id1 = currentUser.getRole().equalsIgnoreCase("admin") ? Long.parseLong(input("Enter user id: ")) : currentUser.getId();
        Long id2 = Long.parseLong(input("Enter user id: "));
        String answer = input("Do you want to accept the friend request? [yes/no]");

        if (userService.respondFriendRequest(id1, id2, answer) != null) {
            System.out.println("Responded to friend request!");
        }
    }

    private void showAllFriendRequest() {
        Long id1 = currentUser.getRole().equalsIgnoreCase("admin") ? Long.parseLong(input("Enter user id: ")) : currentUser.getId();

        List<FriendRequestDTO> requests = userService.getAllFrRequests(id1);
        if (requests.size() > 0) {
            requests.forEach(x -> System.out.println(x.getId().getLeft().getId() +
                    " | " + x.getId().getLeft().getFirstName() +
                    " " + x.getId().getLeft().getLastName() +
                    " | Date " + x.getDate()));
        } else {
            System.out.println("There are no friend requests!");
        }
    }

    private void showAllSentRequests() {
        Long id1 = currentUser.getRole().equalsIgnoreCase("admin") ? Long.parseLong(input("Enter user id: ")) : currentUser.getId();
        List<FriendRequestDTO> requests = userService.getAllSentFrRequests(id1);

        if (requests.size() > 0) {
            requests.forEach(x -> System.out.println(x.getId().getRight().getId() +
                    " | " + x.getId().getRight().getFirstName() +
                    " " + x.getId().getRight().getLastName() +
                    " | Date " + x.getDate()));
        } else {
            System.out.println("There are no friend requests!");
        }
    }
}
