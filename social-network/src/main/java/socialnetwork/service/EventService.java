package socialnetwork.service;

import javafx.util.Pair;
import socialnetwork.domain.mainDom.Event;
import socialnetwork.domain.mainDom.EventNotificationType;
import socialnetwork.domain.mainDom.User;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.repository.Repository;
import socialnetwork.repository.database.MembersRepository;
import socialnetwork.repository.paging.Page;
import socialnetwork.repository.paging.Pageable;
import socialnetwork.repository.paging.PageableImplementation;
import socialnetwork.utils.ChangeEvent;
import socialnetwork.utils.ChangeEventType;
import socialnetwork.utils.observer.Observable;
import socialnetwork.utils.observer.Observer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class EventService implements Observable<ChangeEvent<Event>> {
    private MembersRepository<Long, Event, Pair<Long, Boolean>> eventRepository;
    private Repository<Long, User> userRepository;
    private NotificationService notificationService;

    private int page = 0;
    private int size = 10;

    public void setPagedSize(int size) {
        this.size = size;
    }

    public EventService(MembersRepository<Long, Event, Pair<Long, Boolean>> eventRepository, Repository<Long, User> userRepository, NotificationService notificationService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public List<Event> getEventsOnPage(int page, Long idUser, String type) {
        this.page = page;
        Pageable pageable = new PageableImplementation(page, size);
        Page<Event> eventPage = eventRepository.findAll(pageable, idUser, type);
        return eventPage.getContent().collect(Collectors.toList());
    }

    public int getNumberOfPages(Long idUser, String type) {
        return eventRepository.getNrPages(new PageableImplementation(page, size), idUser, type);
    }

    /**
     * @param title       - title of the event
     * @param description - description of the event
     * @param date        - start date of the event
     * @param organizer   - the user that created the event
     * @return - null if the event is created
     * - otherwise returns the event
     * @throws ValidationException - if the data is invalid
     */
    public Event addEvent(String title, String description, LocalDateTime date, User organizer) {

        Event event = new Event(title, description, date, organizer.getId(), "");
        HashMap<Long, Boolean> participants = new HashMap<>();
        participants.put(organizer.getId(), true);
        event.setParticipants(participants);

        Event ev = eventRepository.save(event);
        if (ev == null)
            notifyObservers(new ChangeEvent<>(ChangeEventType.ADD));
        return ev;
    }

    /**
     * @param id          - the id of the event that will be updated
     * @param title       - title of the event
     * @param description - description of the event
     * @param date        - start date of the event
     * @param organizer   - the user that created the event
     * @return - null if the event is updated
     * - otherwise returns the event
     * @throws ValidationException - if the data is invalid
     */
    public Event updateEvent(Long id, String title, String description, LocalDateTime date, User organizer) {
        Event event = new Event(title, description, date, organizer.getId(), "");
        event.setId(id);
        Event ev = eventRepository.update(event);

        if (ev == null)
            notifyObservers(new ChangeEvent<>(ChangeEventType.UPDATE));
        return ev;
    }

    /**
     * @return - a list with all events
     */
    public List<Event> getAllEvents() {
        List<Event> allEvents = new ArrayList<>();
        eventRepository.findAll().forEach(allEvents::add);
        return allEvents;
    }

    /**
     * @param idUser - must be not null
     * @return - a list with all
     */
    public List<Event> getAllUserEvents(Long idUser) {
        return getAllEvents().stream()
                .filter(x -> x.getParticipants().containsKey(idUser))
                .collect(Collectors.toList());
    }

    /**
     * @param idUser - must be not null
     * @return a list of events that user is subscribed to
     */
    public List<Event> getAllNotifiableEvents(Long idUser) {
        return getAllEvents().stream()
                .filter(x -> {
                    if (x.getParticipants().containsKey(idUser))
                        return x.getParticipants().get(idUser);
                    return false;
                })
                .collect(Collectors.toList());
    }

    /**
     * @param event      - the event the user will be notified about
     * @param type       - type of notification
     * @param loggedUser - the user to whom the notification will be sent
     */
    public void createEventNotification(Event event, EventNotificationType type, User loggedUser) {
        String notificationMsg = "";
        switch (type.getChronoUnit()) {
            case MINUTES:
                if (type.getUnit() == 1)
                    notificationMsg = "There is 1 minute left until event " + event.getTitle() + " starts";
                else if (type.getUnit() == 5)
                    notificationMsg = "There are 5 minutes left until event " + event.getTitle() + " starts";
                else {
                    notificationMsg = "Event " + event.getTitle() + " has started!";
                    notifyObservers(new ChangeEvent<>(ChangeEventType.UPDATE, event));
                }
                break;
            case HOURS:
                notificationMsg = "There is 1 hour left until event " + event.getTitle() + " starts";
                break;
            case DAYS:
            default:
                notificationMsg = "There is 1 day left until event " + event.getTitle() + " starts";
        }
//        for ( Long userId : event.getParticipants())
//            notificationService.createNotification(userId, LocalDateTime.now(), notificationMsg);
        notificationService.createNotification(loggedUser.getId(), LocalDateTime.now(), notificationMsg);
    }

    /**
     * add the given user to the specified event
     *
     * @param event - must be not null
     * @param user  - must be not null
     * @return null if the user is added to the list of participants of the given event
     * otherwise returns the event
     */
    public Event addParticipant(Event event, User user) {
        Event ev = eventRepository.addMember(event, new Pair<>(user.getId(), true));
        if (ev == null)
            notifyObservers(new ChangeEvent<>(ChangeEventType.JOINEVENT));
        return ev;
    }

    /**
     * removes the user from the specified event
     *
     * @param event - must be not null
     * @param user  - must be not null
     * @return - the event if the user is removed
     * otherwise returns null
     */
    public Event removeParticipant(Event event, User user) {
        Event ev = eventRepository.deleteMember(event, new Pair<>(user.getId(), false));
        if (ev != null)
            notifyObservers(new ChangeEvent<>(ChangeEventType.LEAVEEVENT));
        return ev;
    }

    public Event subscribeToNotifications(Event e, User user) {
        Event ev = eventRepository.updateMember(e, new Pair<>(user.getId(), true));
        if (ev == null)
            notifyObservers(new ChangeEvent<>(ChangeEventType.UPDATE, e));
        return ev;
    }

    public Event unsubscribeToNotifications(Event e, User user) {
        Event ev = eventRepository.updateMember(e, new Pair<>(user.getId(), false));
        if (ev == null)
            notifyObservers(new ChangeEvent<>(ChangeEventType.UPDATE, e));
        return ev;
    }

    private List<Observer<ChangeEvent<Event>>> observers = new ArrayList<>();

    @Override
    public void addObserver(Observer<ChangeEvent<Event>> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<ChangeEvent<Event>> e) {
        observers.remove(e);
    }

    @Override
    public void notifyObservers(ChangeEvent<Event> t) {
        observers.forEach(x -> x.update(t));
    }
}
