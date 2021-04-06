package socialnetwork.service;

import socialnetwork.domain.dto.MessageDTO;
import socialnetwork.domain.mainDom.Group;
import socialnetwork.domain.mainDom.Message;
import socialnetwork.domain.mainDom.User;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.repository.Repository;
import socialnetwork.repository.paging.Page;
import socialnetwork.repository.paging.Pageable;
import socialnetwork.repository.paging.PageableImplementation;
import socialnetwork.repository.paging.PagingRepository;
import socialnetwork.utils.ChangeEvent;
import socialnetwork.utils.ChangeEventType;
import socialnetwork.utils.Constants;
import socialnetwork.utils.observer.Observable;
import socialnetwork.utils.observer.Observer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GroupService implements Observable<ChangeEvent<User>> {
    private PagingRepository<Long, Group> groupRepository;
    private PagingRepository<Long, MessageDTO> messageRepository;
    private Repository<Long, User> userRepository;
    private NotificationService notificationService;

    public GroupService(PagingRepository<Long, Group> groupRepository, PagingRepository<Long, MessageDTO> messageRepository, Repository<Long, User> userRepository, NotificationService notificationService) {
        this.groupRepository = groupRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    private int pageGroup = 0;
    private int sizeGroup = 10;
    private int pageMsg = 0;
    private int sizeMsg = 10;

    public void setPageSizeGroup(int size) {
        this.sizeGroup = size;
    }

    public void setPageSizeMsg(int size) {
        this.sizeMsg = size;
    }

    public List<Group> getGroupsOnPage(int page, Long idUser, String type) {
        this.pageGroup = page;
        Pageable pageable = new PageableImplementation(page, sizeGroup);
        Page<Group> groupPage = groupRepository.findAll(pageable, idUser, type);
        return groupPage.getContent().collect(Collectors.toList());
    }

    public int getNrGroupsPages(Long idUser, String type) {
        return groupRepository.getNrPages(new PageableImplementation(pageGroup, sizeGroup), idUser, type);
    }

    public List<Message> getMessagesOnPage(int page, Long idGroup, String type) {
        this.pageMsg = page;
        Pageable pageable = new PageableImplementation(page, sizeMsg);
        Page<MessageDTO> msgPage = messageRepository.findAll(pageable, idGroup, type);
        return msgPage.getContent().map(this::messageDTOtoMessage).collect(Collectors.toList());
    }

    public int getNrMsgPages(Long idGroup, String type) {
        return groupRepository.getNrPages(new PageableImplementation(pageMsg, sizeMsg), idGroup, type);
    }

    /**
     * @param groupName - the name of the group to be created
     * @param members   - ids of the users that will be part of the group
     * @return null if the given group is created
     * otherwise returns the group
     * @throws ValidationException if the group data is invalid
     * @throws ServiceException    if any of the given users does not exists
     *                             if a group that contains just the given users already exists
     */
    public Group createGroup(String groupName, List<Long> members) {
        for (Long user : members) {
            if (userRepository.findOne(user) == null)
                throw new ServiceException("Invalid users!");
        }

        if (findGroupByMembers(members) != null)
            throw new ServiceException("There's already a group with the specified users!");

        Group newGroup = new Group(groupName);
        newGroup.setMembers(members);
        Group g = groupRepository.save(newGroup);
        if (g == null)
            notifyObservers(new ChangeEvent<>(ChangeEventType.ADD));
        return g;
    }

    /**
     * return the group that contains just the 2 users (private group)
     * if the group does not exist then
     *
     * @param from - must not be null
     * @param to   - must not be null
     * @return - a group that contains just the 2 users
     * @throws ServiceException - if the user to does not exist
     */
    public Group openPrivateChat(Long from, Long to) {
        User user2 = userRepository.findOne(to);
        if (user2 == null)
            throw new ServiceException("User does not exists!");

        List<Long> members = new ArrayList<>();
        members.add(from);
        members.add(to);

        Group group = findGroupByMembers(members);
        if (group == null) {
            User user = userRepository.findOne(to);
            createGroup(user.getFirstName() + user.getLastName(), members);
            group = findGroupByMembers(members);
        }
        return group;
    }

    /**
     * @param groupName - the name of the group to be created
     * @param members   - the users that will be part of the group
     * @return null if the given group is created
     * otherwise returns the group
     * @throws ValidationException if the group data is invalid
     * @throws ServiceException    if any of the given users does not exists
     */
    public Group createGroupWithUsernames(String groupName, List<String> members) {
        List<Long> membersIds = new ArrayList<>();
        for (String username : members) {
            User user = getUserByUsername(username);
            if (user == null)
                throw new ServiceException("Invalid users!");
            membersIds.add(user.getId());
        }

        Group newGroup = new Group(groupName);
        newGroup.setMembers(membersIds);
        Group g = groupRepository.save(newGroup);
        if (g == null)
            notifyObservers(new ChangeEvent<>(ChangeEventType.ADD));
        return g;
    }

    /**
     * @param idGroup - must be not null
     * @return - the group with the given id (which contains the specified user)
     * null if the group does not exist
     */
    public Group openGroup(Long idGroup) {
        return groupRepository.findOne(idGroup);
    }

    /**
     * @param username - String - must not be null
     * @return - the user with the given username
     * - null if there is no user with the given username
     */
    public User getUserByUsername(String username) {
        for (User user : userRepository.findAll()) {
            if (user.getUsername().equals(username))
                return user;
        }
        return null;
    }

    /**
     * @param idGroup - must be not null
     * @param idUser  - must be not null
     * @return the removed group
     * or null if the given id does not correspond to a valid group
     * @throws ValidationException if the id is null
     */
    public Group deleteUserGroup(Long idGroup, Long idUser) {
        if (!findGroupById(idGroup).getMembers().contains(idUser))
            throw new ServiceException("Invalid group!");
        Group g = groupRepository.delete(idGroup);
        if (g != null)
            notifyObservers(new ChangeEvent<>(ChangeEventType.REMOVE));
        return g;
    }

    /**
     * @return a list with all the groups stored
     */
    public List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<>();
        groupRepository.findAll().forEach(groups::add);
        return groups;
    }

    /**
     * @param idUser - must not be null
     * @return - a list with all the groups that contains the specified user
     */
    public List<Group> getAllUserGroups(Long idUser) {

        return getAllGroups()
                .stream()
                .filter(x -> x.getMembers().contains(idUser) && (x.getMembers().size() > 2))
                .collect(Collectors.toList());
    }

    /**
     * @param idUser - must not be null
     * @return - a list with all private chats of a user
     */
    public List<Group> getAllPrivateChats(Long idUser) {

        return getAllGroups()
                .stream()
                .filter(x -> x.getMembers().contains(idUser) && (x.getMembers().size() == 2))
                .collect(Collectors.toList());
    }

    /**
     * @param group - must not be null
     * @return - a list with all the users from the specified group
     */
    public List<User> getAllMembersFromGroup(Group group) {
        return group.getMembers()
                .stream()
                .map(x -> userRepository.findOne(x))
                .collect(Collectors.toList());
    }

    /**
     * @param to - a list with user Ids
     * @return - the group that contains just the given users
     * null if there is no such group
     */
    public Group findGroupByMembers(List<Long> to) {
        for (Group group : getAllGroups()) {
            if (group.getMembers().containsAll(to) && to.containsAll(group.getMembers()))
                return group;
        }
        return null;
    }

    /**
     * @param idGroup - must be not null
     * @return - the group with the specified id
     */
    public Group findGroupById(Long idGroup) {
        return groupRepository.findOne(idGroup);
    }

    /**
     * @param group - must not be null
     * @return a list with all messages from the specified group
     */
    public List<Message> getAllMessagesFromGroup(Group group) {
        List<Message> messages = new ArrayList<>();
        for (MessageDTO msg : messageRepository.findAll()) {
            if (msg.getToGroup() == group.getId())
                messages.add(messageDTOtoMessage(msg));
        }
        messages.sort(Comparator.comparing(Message::getDate));
        return messages;
    }


    /**
     * @param idMessage - must be not null
     * @param group     - must be not null
     * @return the message with the specified id (from the given group)
     * or null if there is no message with the specified id in group
     */
    public Message findMessageInGroup(Long idMessage, Group group) {
        for (Message message : getAllMessagesFromGroup(group)) {
            if (message.getId().equals(idMessage))
                return message;
        }
        return null;
    }

    /**
     * @param from    - the user that wants to send the message
     * @param to      - the group to which the message would be sent
     * @param message - the text of the message
     * @return null - if the message is sent
     * the message if the given data is invalid
     * @throws ValidationException if the given data is invalid
     */
    public MessageDTO sendMessageToGroup(User from, Group to, String message) {
        LocalDateTime date = LocalDateTime.now();
        MessageDTO messageDTO = new MessageDTO(from.getId(), to.getId(), message, date);
        MessageDTO m = messageRepository.save(messageDTO);
        if (m == null) {
            if (to.getMembers().size() == 2) {
                to.getMembers().stream()
                        .filter(x -> (!x.equals(from.getId())))
                        .forEach(x -> notificationService.createNotification(x, date, from.getFirstName() + " " + from.getLastName() + " sent you a message " + " : " + message));
            } else {
                to.getMembers().stream()
                        .filter(x -> (!x.equals(from.getId())))
                        .forEach(x -> notificationService.createNotification(x, date, from.getFirstName() + " " + from.getLastName() + " sent a message in group " + to.getGroupName() + " : " + message));
            }
            notifyObservers(new ChangeEvent<>(ChangeEventType.ADD));
        }
        return m;
    }

    /**
     * @param from        - the user that wants to send the message
     * @param idMessage   - the message to be replied to
     * @param group       - the group to which the message would be sent
     * @param textMessage - the text of the message
     * @return null if the reply is sent
     * otherwise returns the message
     * @throws ValidationException if the data is invalid
     * @throws ServiceException    if the given message does not exits
     */
    public MessageDTO replyMessage(User from, Long idMessage, Group group, String textMessage) {
        Message message = findMessageInGroup(idMessage, group);
        if (message == null)
            throw new ServiceException("Invalid id message!");

        LocalDateTime date = LocalDateTime.now();
        MessageDTO replyMessage = new MessageDTO(from.getId(), group.getId(), textMessage, date, idMessage);
        MessageDTO m = messageRepository.save(replyMessage);
        if (m == null) {
            if (group.getMembers().size() == 2) {
                group.getMembers().stream()
                        .filter(x -> (!x.equals(from.getId())))
                        .forEach(x -> notificationService.createNotification(x, date, from.getFirstName() + " " + from.getLastName() + " replied to a message " + " : " + message));
            } else {
                group.getMembers().stream()
                        .filter(x -> (!x.equals(from.getId())))
                        .forEach(x -> notificationService.createNotification(x, date, from.getFirstName() + " " + from.getLastName() + " replied to a message in group " + group.getGroupName() + " : " + textMessage));
            }
            notifyObservers(new ChangeEvent<>(ChangeEventType.ADD));
        }
        return m;
    }

    /**
     * transforms a MessageDTO object to the correspondent Message object
     *
     * @param msg - MessageDTO, must not be null
     * @return the correspondent message
     */
    private Message messageDTOtoMessage(MessageDTO msg) {
        Message message = new Message(userRepository.findOne(msg.getFromUser()), findGroupById(msg.getToGroup()), msg.getMessage(), msg.getDate(), msg.getReply());
        message.setId(msg.getId());
        return message;
    }

    /**
     * @param startDate - date
     * @param endDate   - date
     * @param user      - must not be null
     * @return - a list of strings that contains  the messages received by the given user
     * (from all private chats and groups)
     */
    public List<String> messagesReport(LocalDate startDate, LocalDate endDate, User user) {
        List<String> messages = new ArrayList<>();
        messages.add("                 Messages: \n");
        for (Group g : getAllUserGroups(user.getId())) {
            getAllMessagesFromGroup(g).stream()
                    .filter(x -> x.getDate().toLocalDate().isAfter(startDate) && x.getDate().toLocalDate().isBefore(endDate) && !x.getFrom().getId().equals(user.getId()))
                    .forEach(x -> messages.add("From: " + x.getFrom().getFirstName() + " " + x.getFrom().getLastName() + "    In group: " + x.getTo().getGroupName() + "    Message: " + x.getMessage() + "    Date: " + x.getDate().format(Constants.DATE_TIME_FORMATTER) + "\n"));
        }
        for (Group g : getAllPrivateChats(user.getId())) {
            getAllMessagesFromGroup(g).stream()
                    .filter(x -> x.getDate().toLocalDate().isAfter(startDate) && x.getDate().toLocalDate().isBefore(endDate) && !x.getFrom().getId().equals(user.getId()))
                    .forEach(x -> messages.add("From: " + x.getFrom().getFirstName() + " " + x.getFrom().getLastName() + "    Message: " + x.getMessage() + "    Date: " + x.getDate().format(Constants.DATE_TIME_FORMATTER) + "\n"));
        }
        return messages;
    }

    /**
     * @param startDate  -date
     * @param endDate    - date
     * @param loggerUser - the receiver of the messages (must be not null)
     * @param friend     - the sender of the messages (must be not null)
     * @return - a list of strings with all the messages sent by friend to loggerUser in the given time interval
     */
    public List<String> conversationsReport(LocalDate startDate, LocalDate endDate, User loggerUser, User friend) {
        List<String> messages = new ArrayList<>();
        messages.add("          Messages: \n");
        for (Group g : getAllUserGroups(loggerUser.getId())) {
            getAllMessagesFromGroup(g).stream()
                    .filter(x -> x.getDate().toLocalDate().isAfter(startDate) && x.getDate().toLocalDate().isBefore(endDate) && x.getFrom().getId().equals(friend.getId()))
                    .forEach(x -> messages.add("From: " + x.getFrom().getFirstName() + " " + x.getFrom().getLastName() + "    In group: " + x.getTo().getGroupName() + "     Message: " + x.getMessage() + "     Date: " + x.getDate().format(Constants.DATE_TIME_FORMATTER) + "\n"));
        }
        for (Group g : getAllPrivateChats(loggerUser.getId())) {
            getAllMessagesFromGroup(g).stream()
                    .filter(x -> x.getDate().toLocalDate().isAfter(startDate) && x.getDate().toLocalDate().isBefore(endDate) && x.getFrom().getId().equals(friend.getId()))
                    .forEach(x -> messages.add("From: " + x.getFrom().getFirstName() + " " + x.getFrom().getLastName() + "    Message: " + x.getMessage() + "     Date: " + x.getDate().format(Constants.DATE_TIME_FORMATTER) + "\n"));
        }
        return messages;
    }

    private List<Observer<ChangeEvent<User>>> observers = new ArrayList<>();

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
