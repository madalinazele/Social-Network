package socialnetwork.service;

import socialnetwork.domain.Status;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.dto.FriendRequestDTO;
import socialnetwork.domain.dto.FriendsDTO;
import socialnetwork.domain.mainDom.FriendRequest;
import socialnetwork.domain.mainDom.Relation;
import socialnetwork.domain.mainDom.User;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.repository.paging.*;
import socialnetwork.utils.ChangeEvent;
import socialnetwork.utils.ChangeEventType;
import socialnetwork.utils.Constants;
import socialnetwork.utils.Graph;
import socialnetwork.utils.observer.Observable;
import socialnetwork.utils.observer.Observer;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class UserService implements Observable<ChangeEvent<User>> {
    private final PagingRepository<Long, User> userRepository;
    private final PagingRepository<Tuple<Long, Long>, Relation> relationRepository;
    private final PagingRepository<Tuple<Long, Long>, FriendRequest> friendRequestRepository;
    private final NotificationService notificationService;

    private int page = 0;
    private int size = 10;

    public void setPagedSize(int size) {
        this.size = size;
    }

    public UserService(PagingRepository<Long, User> repoUser, PagingRepository<Tuple<Long, Long>, Relation> repoRel, PagingRepository<Tuple<Long, Long>, FriendRequest> frRepo, NotificationService notificationService) {
        this.userRepository = repoUser;
        this.relationRepository = repoRel;
        this.friendRequestRepository = frRepo;
        this.notificationService = notificationService;
    }

    public int getNumberOfPages(Long idUser, String type) {
        return userRepository.getNrPages(new PageableImplementation(page, size), idUser, type);
    }

    public int getNrUserPagesFiltered(List<User> users) {
        int nrEl = users.size();
        if (nrEl % size == 0)
            return nrEl / size;
        else return nrEl / size + 1;
    }

    public int getNrFriendsPages(Long idUser, String type) {
        return relationRepository.getNrPages(new PageableImplementation(page, size), idUser, type);
    }

    public int getNrFriendsPagesFiltered(List<FriendsDTO> users) {
        int nrEl = users.size();
        if (nrEl % size == 0)
            return nrEl / size;
        else return nrEl / size + 1;
    }

    public List<User> getRepoUsersPage(int pageIdx, Long idUser, String type) {
        this.page = pageIdx;
        Pageable pageable = new PageableImplementation(pageIdx, size);
        Page<User> userPage = userRepository.findAll(pageable, idUser, type);
        return userPage.getContent().collect(Collectors.toList());
    }

    public List<User> getUsersPageFiltered(int pageIdx, List<User> users) {
        Paginator<User> paginator = new Paginator<>(new PageableImplementation(pageIdx, size), users);
        Page<User> userPage = paginator.paginate();
        return userPage.getContent().collect(Collectors.toList());
    }

    public List<FriendsDTO> getFriendsPageFiltered(int pageIdx, List<FriendsDTO> users) {
        Paginator<FriendsDTO> paginator = new Paginator<>(new PageableImplementation(pageIdx, size), users);
        Page<FriendsDTO> friendsPage = paginator.paginate();
        return friendsPage.getContent().collect(Collectors.toList());
    }

    public List<FriendsDTO> getRepoFriendsPage(int pageIdx, Long idUser, String type) {
        this.page = pageIdx;
        Pageable pageable = new PageableImplementation(pageIdx, size);
        List<FriendsDTO> friendsList = relationRepository.findAll(pageable, idUser, type).getContent()
                .map(x -> {
                    if (x.getId().getLeft().equals(idUser))
                        return new FriendsDTO(getUser(x.getId().getRight()), x.getDate());
                    else return new FriendsDTO(getUser(x.getId().getLeft()), x.getDate());
                })
                .collect(Collectors.toList());

        Paginator<FriendsDTO> paginator = new Paginator<>(pageable, friendsList);
        Page<FriendsDTO> friendsPage = paginator.paginate();
        return friendsPage.getContent().collect(Collectors.toList());
    }

    /**
     * add User
     *
     * @param firstName - String
     * @param lastName  - String
     * @return null- if the given user is saved
     * otherwise returns the user (id already exists)
     * @throws ValidationException if the user has invalid data
     */
    public User addUser(String firstName, String lastName, String email, String username, String password, String image) {
        User newUser = new User(firstName, lastName, email, username, password, image);
        User u = userRepository.save(newUser);
        if (u == null)
            notifyObservers(new ChangeEvent<>(ChangeEventType.ADD));
        return u;
    }

    /**
     * removes the user with the specified id
     * remove users relationships
     *
     * @param id - must be not null
     * @return the removed user
     * or null if there is no user with the given id
     * @throws ValidationException if the given id is null.
     */
    public User deleteUser(Long id) {
        User u = userRepository.delete(id);
        if (u != null)
            notifyObservers(new ChangeEvent<>(ChangeEventType.REMOVE));
        return u;
    }

    /**
     * @param id        - must be not null
     * @param firstName - must be not null
     * @param lastName  - must not be null
     * @return null - if the user is updated,
     * the user - otherwise (e.g id does not exist).
     * @throws ValidationException if the user is null or not valid.
     */
    public User updateUser(Long id, String firstName, String lastName, String email, String username, String password, String image) {
        User newUser = new User(firstName, lastName, email, username, password, image);
        newUser.setId(id);

        User u = userRepository.update(newUser);
        if (u == null)
            notifyObservers(new ChangeEvent<>(ChangeEventType.UPDATE, newUser));
        return u;
    }

    /**
     * @param id -the id of the user to be returned
     *           id must not be null
     * @return the user with the specified id
     * or null - if there is no user with the given id
     * @throws ValidationException if id is null.
     */
    public User getUser(Long id) {
        return userRepository.findOne(id);
    }

    /**
     * @return all users
     */
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        for (User user : userRepository.findAll())
            users.add(user);
        return users;
    }

    /**
     * @param username - String, unique identifier for a user
     * @return the user with the given username
     * null if there is no user with the given username
     */
    public User getUserByUsername(String username) {
        for (User user : getAll()) {
            if (user.getUsername().equals(username))
                return user;
        }
        return null;
    }

    /**
     * @param id1 - must be not null
     * @param id2 - id2 must be not null
     * @return null- if the given relation is saved
     * otherwise returns the relation (id already exists)
     * @throws ValidationException if the relation is null or not valid
     */
    public Relation addRelation(Long id1, Long id2) {

        Relation relation = new Relation(LocalDateTime.now());
        relation.setId((id1 < id2) ? new Tuple<>(id1, id2) : new Tuple<>(id2, id1));

        if (getUser(id1) == null || getUser(id2) == null)
            throw new ServiceException("Invalid users");

        Relation r = relationRepository.save(relation);
        if (r == null)
            notifyObservers(new ChangeEvent<>(ChangeEventType.ADD, getUser(id2)));
        return r;
    }

    /**
     * removes the relation with the specified id
     *
     * @param id - must be not null
     * @return the removed relation
     * or null if there is no relation with the given id
     * @throws ValidationException if the given id is null.
     */
    public Relation deleteRelation(Tuple<Long, Long> id) {
        User user2 = getUser(id.getRight());
        if (id.getRight() < id.getLeft()) {
            long aux = id.getRight();
            id.setRight(id.getLeft());
            id.setLeft(aux);
        }
        Relation r = relationRepository.delete(id);
        if (r != null)
            notifyObservers(new ChangeEvent<>(ChangeEventType.REMOVE, user2));
        return r;
    }

    /**
     * @return a list with all friendships
     */
    public List<Relation> getAllFriendships() {
        List<Relation> relations = new ArrayList<>();
        relationRepository.findAll().forEach(relations::add);
        return relations;
    }

    /**
     * @param id1 - must be not null
     * @param id2 - must be not null
     * @return -null if the given users are not friends
     * the relation (FriendDTO) correspondent to their relationship otherwise
     */
    public FriendsDTO findFriendship(Long id1, Long id2) {
        for (FriendsDTO fr : findFriends(id1)) {
            if (fr.getUser().getId().equals(id2))
                return fr;
        }
        return null;
    }

    /**
     * @param idUser1 - must be not null
     * @param idUser2 - must be not null
     * @return null if the friend request was sent successfully
     * otherwise return the friend request
     * @throws ServiceException if user does not exist
     */
    public FriendRequest sendFriendRequest(Long idUser1, Long idUser2) {
        if (idUser1.equals(idUser2))
            throw new ServiceException("You can't send a friend request to yourself!");

        User user2 = getUser(idUser2);
        User user1 = getUser(idUser1);
        if (user2 == null) {
            throw new ServiceException("User does not exist");
        }
        if (findFriendRequest(idUser1, idUser2) != null)
            throw new ServiceException("You already sent a request to this user!");

        LocalDateTime date = LocalDateTime.now();
        FriendRequest frR = new FriendRequest(Status.PENDING, date);
        frR.setId(new Tuple<>(idUser1, idUser2));
        FriendRequest fr = friendRequestRepository.save(frR);
        if (fr == null) {
            notificationService.createNotification(idUser2, date,
                    user1.getFirstName() + " " + user1.getLastName() + " sent you a friend request");
            notifyObservers(new ChangeEvent<>(ChangeEventType.FRIENDREQUEST, user1));
        }
        return fr;
    }

    /**
     * @param idUser1 - must be not null
     * @param idUser2 - must be not null
     * @return null if there is no friend request from user1 to user2
     * otherwise returns the friendships
     */
    public FriendRequest findFriendRequest(Long idUser1, Long idUser2) {
        return friendRequestRepository.findOne(new Tuple<>(idUser1, idUser2));
    }

    /**
     * @param idUser1 - must be not null
     * @param idUser2 - must be not null
     * @param answer  - the answer to the friend request
     * @return the deleted friend request
     * @throws ServiceException if the data is invalid,
     *                          user1 didn't send any friend request to user2
     */
    public FriendRequest respondFriendRequest(Long idUser1, Long idUser2, String answer) {
        FriendRequest friendRequest = findFriendRequest(idUser2, idUser1);
        if (friendRequest == null)
            throw new ServiceException("Invalid friend request!");

        User user1 = getUser(idUser1);

        if (answer.equalsIgnoreCase("yes")) {
            this.addRelation(idUser2, idUser1);
            notificationService.createNotification(idUser2, LocalDateTime.now(), user1.getFirstName() + " " + user1.getLastName() + " accepted your friend request");
        }
        FriendRequest fr = friendRequestRepository.delete(new Tuple<>(idUser2, idUser1));
        if (fr != null) {
            notificationService.createNotification(idUser2, LocalDateTime.now(), user1.getFirstName() + " " + user1.getLastName() + " rejected your friend request");
            notifyObservers(new ChangeEvent<>(ChangeEventType.FRIENDREQUEST, user1));
        }
        return fr;
    }

    /**
     * @param idUser1 - must be not null
     * @param idUser2 - must be not null
     * @return the deleted friend request
     * @throws ServiceException - if the friend request does not exists
     *                          or the friend request already has a response
     */
    public FriendRequest cancelFriendRequest(Long idUser1, Long idUser2) {
        FriendRequest fr = friendRequestRepository.findOne(new Tuple<>(idUser1, idUser2));
        if (fr == null)
            throw new ServiceException("Friend request does not exist!");
        if (fr.getStatus() == Status.PENDING) {
            FriendRequest fR = friendRequestRepository.delete(new Tuple<>(idUser1, idUser2));
            if (fR != null) {
                //--------------------!!!!!!!!!!
                notifyObservers(new ChangeEvent<>(ChangeEventType.FRIENDREQUEST, getUser(idUser2)));
            }
            return fr;
        }
        throw new ServiceException("The user has already responded to the friend request!");
    }

    /**
     * @param idUser - must not be null
     * @return all friend requests of a user
     */
    public List<FriendRequestDTO> getAllFrRequests(Long idUser) {
        List<FriendRequestDTO> friendRequests = new ArrayList<>();
        for (FriendRequest friendRequest : friendRequestRepository.findAll()) {
            if (friendRequest.getId().getRight().equals(idUser) && friendRequest.getStatus() == Status.PENDING) {
                FriendRequestDTO newFr = new FriendRequestDTO(friendRequest.getStatus(), friendRequest.getDate());
                newFr.setId(new Tuple<>(getUser(friendRequest.getId().getLeft()), getUser(idUser)));
                friendRequests.add(newFr);
            }
        }
        return friendRequests;
    }

    public int getNrFrPages(Long idUser, String type) {
        return friendRequestRepository.getNrPages(new PageableImplementation(page, size), idUser, type);
    }

    public List<FriendRequestDTO> getRepoFRPage(int pageIdx, Long idUser, String type) {
        this.page = pageIdx;
        Pageable pageable = new PageableImplementation(pageIdx, size);
        Page<FriendRequest> frPage = friendRequestRepository.findAll(pageable, idUser, type);
        return frPage.getContent()
                .map(x -> {
                    FriendRequestDTO fr = new FriendRequestDTO(x.getStatus(), x.getDate());
                    fr.setId(new Tuple<>(getUser(x.getId().getLeft()), getUser(x.getId().getRight())));
                    return fr;
                })
                .collect(Collectors.toList());
    }

    /**
     * @param idUser - must be not null
     * @return all friend requests sent by the given user
     */
    public List<FriendRequestDTO> getAllSentFrRequests(Long idUser) {
        List<FriendRequestDTO> friendRequests = new ArrayList<>();
        for (FriendRequest friendRequest : friendRequestRepository.findAll()) {
            if (friendRequest.getId().getLeft().equals(idUser) && friendRequest.getStatus() == Status.PENDING) {
                FriendRequestDTO newFr = new FriendRequestDTO(friendRequest.getStatus(), friendRequest.getDate());
                newFr.setId(new Tuple<>(getUser(friendRequest.getId().getLeft()), getUser(friendRequest.getId().getRight())));
                friendRequests.add(newFr);
            }
        }
        return friendRequests;
    }

    /**
     * @param idList - list of user ids
     * @return the correspondent list of users
     */
    public List<User> getUsersByIds(List<Long> idList) {
        return idList
                .stream()
                .map(this::getUser)
                .collect(Collectors.toList());
    }

    /**
     * create a graph where the users are the vertices and the friendships are the edges
     *
     * @return the graph
     */
    public Graph createGraph() {
        Graph grp = new Graph(getAll().size());
        for (User i : userRepository.findAll())
            grp.addVertex(i.getId());
        for (Relation rel : relationRepository.findAll()) {
            grp.addEdge(rel.getId().getLeft(), rel.getId().getRight());
        }
        return grp;
    }

    /**
     * @param idUser - must be not null
     * @return a list with all users from the community that contains the specified user
     */
    public List<User> findCommunity(Long idUser) {
        Graph grp = createGraph();
        return getUsersByIds(grp.getComponent(idUser));
    }

    /**
     * finds the number of communities
     * creates the associated graph and finds the number of connected components
     *
     * @return the number of communities
     */
    public int getNrOfComponents() {
        Graph grp = new Graph(getAll().size());
        for (Relation rel : relationRepository.findAll()) {
            grp.addEdge(rel.getId().getLeft(), rel.getId().getRight());
        }
        return grp.nrOfComponents();
    }

    /**
     * determines the most sociable community
     * creates the associated graph and returns the largest connected component
     *
     * @return all users from the most sociable community
     */
    public List<User> getLargestComponent() {
        Graph grp = createGraph();
        return getUsersByIds(grp.getLargestComponent());
    }

    /**
     * @param id - must not be null
     * @return a list with all friends of a user
     * @throws ValidationException if the given id is null
     * @throws ServiceException    if the given id does not correspond to a valid user
     */
    public List<FriendsDTO> findFriends(Long id) {
        if (getUser(id) == null)
            throw new ServiceException("Invalid id");

        return getAllFriendships()
                .stream()
                .filter(x -> x.getId().getLeft().equals(id) || x.getId().getRight().equals(id))
                .map(x -> (x.getId().getLeft().equals(id))
                        ? new FriendsDTO(getUser(x.getId().getRight()), x.getDate())
                        : new FriendsDTO(getUser(x.getId().getLeft()), x.getDate()))
                .collect(Collectors.toList());
    }

    /**
     * @param id    - must be not null
     * @param month - must be not null
     * @return - all friendships started from the specified month
     * @throws ValidationException if the given id or month are null
     */
    public List<FriendsDTO> getFriendsDate(Long id, int month) {
        if (month < 1 || month > 12)
            throw new ServiceException("Invalid month");
        if (getUser(id) == null)
            throw new ServiceException("Invalid id");

        return getAllFriendships()
                .stream()
                .filter(x -> x.getDate().getMonth().getValue() == month && (x.getId().getLeft().equals(id) || x.getId().getRight().equals(id)))
                .map(x -> (x.getId().getLeft().equals(id))
                        ? new FriendsDTO(getUser(x.getId().getRight()), x.getDate())
                        : new FriendsDTO(getUser(x.getId().getLeft()), x.getDate()))
                .collect(Collectors.toList());
    }

    /**
     * @param startDate - start date
     * @param endDate   - end date
     * @param user      - must be not null
     * @return a list of strings with all the friends added in the given interval
     */
    public List<String> relationsReport(LocalDate startDate, LocalDate endDate, User user) {
        List<String> friendActivity = findFriends(user.getId())
                .stream()
                .filter(x -> x.getDate().toLocalDate().isAfter(startDate) && x.getDate().toLocalDate().isBefore(endDate))
                .map(x -> "New friend: " + x.getUser().getFirstName() + " " + x.getUser().getLastName() + "    From: " + x.getDate().format(Constants.DATE_TIME_FORMATTER) + "\n")
                .collect(Collectors.toList());
        friendActivity.add(0, "                 New friends: \n");
        return friendActivity;
    }

    private static String generateHashFromPassword() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Random random = new Random();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        KeySpec spec = new PBEKeySpec("pass123".toCharArray(), salt, 65536, 128);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        byte[] hash = f.generateSecret(spec).getEncoded();
        Base64.Encoder enc = Base64.getEncoder();

        return enc.encodeToString(salt) + "$" + enc.encodeToString(hash);
    }

    public User validateLogin(String username, String password, String email) {
        String hashPw = password;
        try {
            hashPw = generateHashFromPassword();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        System.out.println(hashPw);

        for (User user : getAll()) {
            System.out.println(user.getPassword());
            if (user.getUsername().equals(username) && user.getPassword().equals(password) && user.getEmail().equals(email))
                return user;
        }
        return null;
    }

    private final List<Observer<ChangeEvent<User>>> observers = new ArrayList<>();

    @Override
    public void addObserver(Observer<ChangeEvent<User>> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<ChangeEvent<User>> e) {
        observers.remove(e);
    }

    @Override
    public void notifyObservers(ChangeEvent<User> t) {
        observers.forEach(x -> x.update(t));
    }
}
