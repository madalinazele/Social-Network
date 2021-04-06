package socialnetwork.ui.menu;

import socialnetwork.domain.Tuple;
import socialnetwork.domain.dto.FriendsDTO;
import socialnetwork.domain.mainDom.User;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.service.ServiceException;
import socialnetwork.service.UserService;
import socialnetwork.ui.UIMain;

import java.util.List;
import java.util.Scanner;

public class RelationUI extends UIMain {

    private UserService userService;


    public RelationUI(UserService userService) {
        this.userService = userService;
    }

    private String input(String msg) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(msg);
        return scanner.nextLine();
    }

    public void menu() {
        System.out.println("\n----------");

        System.out.println("1. Show all friends");
        System.out.println("2. Show all friendships filtered by month");
        System.out.println("3. Delete friendship");
        System.out.println("4. Number of communities");
        System.out.println("5. Show community");
        System.out.println("6. The largest community");
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
                            this.getFriends();
                            break;
                        case 2:
                            this.getFriendsByDate();
                            break;
                        case 3:
                            this.deleteRelation();
                            break;
                        case 4:
                            this.getNumberOfCommunities();
                            break;
                        case 5:
                            this.getCommunity();
                            break;
                        case 6:
                            this.getLargestComponent();
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

    public void deleteRelation() {
        Long id1 = currentUser.getRole().equalsIgnoreCase("admin") ? Long.parseLong(input("Enter user id: ")) : currentUser.getId();
        Long id2 = Long.parseLong(input("Enter user id: "));

        if (userService.deleteRelation(new Tuple<>(id1, id2)) == null) {
            System.out.println("Friendship does not exists!");
        } else System.out.println("Friendship deleted!");

    }

    public void getNumberOfCommunities() {
        System.out.println("Number of communities: " + userService.getNrOfComponents());
    }

    public void getLargestComponent() {
        List<User> users = userService.getLargestComponent();
        System.out.println("Number of users: " + users.size());
        users.forEach(System.out::println);
    }

    public void getCommunity() {
        Long id = currentUser.getRole().equalsIgnoreCase("admin") ? Long.parseLong(input("Enter id: ")) : currentUser.getId();
        List<User> community = userService.findCommunity(id);
        if (community.size() == 0)
            System.out.println("No friends were found!");
        else
            community.forEach(System.out::println);
    }

    public void getFriends() {
        Long id = currentUser.getRole().equalsIgnoreCase("admin") ? Long.parseLong(input("Enter id: ")) : currentUser.getId();
        List<FriendsDTO> friends = userService.findFriends(id);

        System.out.println("There are " + friends.size() + " friends");
        printFriends(friends);
    }

    public void getFriendsByDate() {
        Long id = currentUser.getRole().equalsIgnoreCase("admin") ? Long.parseLong(input("Enter id: ")) : currentUser.getId();
        int month = Integer.parseInt(input("Enter month (number between 1 and 12): "));
        List<FriendsDTO> friends = userService.getFriendsDate(id, month);

        System.out.println("There are " + friends.size() + " friendships starting from the specified month");
        printFriends(friends);
    }

    private void printFriends(List<FriendsDTO> friends) {
        friends.forEach(x -> System.out.println(x.getUser().getFirstName()
                + " | " + x.getUser().getLastName()
                + " | " + x.getDate()));
    }
}
