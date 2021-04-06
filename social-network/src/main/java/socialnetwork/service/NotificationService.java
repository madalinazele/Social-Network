package socialnetwork.service;

import socialnetwork.domain.dto.NotificationDTO;
import socialnetwork.domain.mainDom.Notification;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationService implements Observable<ChangeEvent<NotificationDTO>> {

    PagingRepository<Long, NotificationDTO> notificationDBRepository;
    Repository<Long, User> userDBRepository;

    public NotificationService(PagingRepository<Long, NotificationDTO> notificationDBRepository, Repository<Long, User> userDBRepository) {
        this.notificationDBRepository = notificationDBRepository;
        this.userDBRepository = userDBRepository;
    }

    private int page = 0;
    private int size = 10;

    public void setPageSize(int size) {
        this.size = size;
    }

    public List<NotificationDTO> getNotificationsOnPage(int page, Long idUser, String type) {
        this.page = page;
        Pageable pageable = new PageableImplementation(page, size);
        Page<NotificationDTO> notificationPage = notificationDBRepository.findAll(pageable, idUser, type);
        return notificationPage.getContent().collect(Collectors.toList());
    }

    public int getNumberOfPages(Long idUser, String type) {
        return notificationDBRepository.getNrPages(new PageableImplementation(page, size), idUser, type);
    }

    /**
     * @param toId - the user to whom the notification will be sent
     * @param date - the date
     * @param type - the text message of the notification
     * @return null if the notification is created
     * otherwise null
     * @throws ValidationException if the data is invalid
     */
    public NotificationDTO createNotification(Long toId, LocalDateTime date, String type) {
        NotificationDTO n = notificationDBRepository.save(new NotificationDTO(toId, date, type, false));
        if (n == null)
            notifyObservers(new ChangeEvent<>(ChangeEventType.ADD));
        return n;
    }

    /**
     * @param id - id the the notification that will be deleted
     * @return the deleted notification
     * null if the notification does not exists
     */
    public NotificationDTO deleteNotification(Long id) {
        NotificationDTO n = notificationDBRepository.delete(id);
        if (n != null)
            notifyObservers(new ChangeEvent<>(ChangeEventType.REMOVE));
        return n;
    }

    /**
     * @param id - must not be null
     * @return return the notification with the given id
     * ot null if there is no notification with the given id
     */
    public NotificationDTO findNotification(Long id) {
        return notificationDBRepository.findOne(id);
    }

    /**
     * @return a list with all notifications
     */
    public List<NotificationDTO> getAllNotifications() {
        List<NotificationDTO> notificationsDTO = new ArrayList<>();
        notificationDBRepository.findAll().forEach(notificationsDTO::add);
        return notificationsDTO;
    }

    /**
     * @param user - must be not null
     * @return the list with all notifications received by the given user
     */
    public List<Notification> getAllUserNotifications(User user) {
        return getAllNotifications()
                .stream()
                .filter(x -> x.getTo().equals(user.getId()))
                .map(x -> new Notification(userDBRepository.findOne(x.getTo()), x.getDate(), x.getType(), x.getSeen()))
                .collect(Collectors.toList());
    }

    /**
     * @param user - must be not null
     * @return the list with all notifications received by the given user
     */
    public List<NotificationDTO> getAllUserNotificationsDTO(User user) {
        return getAllNotifications()
                .stream()
                .filter(x -> x.getTo().equals(user.getId()))
                .collect(Collectors.toList());
    }

    /**
     * @param notification - must be not null
     * @return return the text message of the given notification
     */
    public String getNotificationMessage(NotificationDTO notification) {
        return "[" + notification.getDate().format(Constants.DATE_TIME_FORMATTER2) + "] " + notification.getType();
    }

    /**
     * delete all notifications received by the given user
     *
     * @param user - must be not null
     */
    public void deleteAllUserNotifications(User user) {
        for (NotificationDTO n : getAllUserNotificationsDTO(user))
            notificationDBRepository.delete(n.getId());
        notifyObservers(new ChangeEvent<>(ChangeEventType.REMOVE));
    }

    private List<Observer<ChangeEvent<NotificationDTO>>> observers = new ArrayList<>();

    @Override
    public void addObserver(Observer<ChangeEvent<NotificationDTO>> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<ChangeEvent<NotificationDTO>> e) {
        observers.remove(e);
    }

    @Override
    public void notifyObservers(ChangeEvent<NotificationDTO> t) {
        observers.forEach(x -> x.update(t));
    }
}
