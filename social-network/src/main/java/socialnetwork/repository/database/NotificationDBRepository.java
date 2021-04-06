package socialnetwork.repository.database;

import socialnetwork.domain.dto.NotificationDTO;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.repository.paging.Page;
import socialnetwork.repository.paging.Pageable;
import socialnetwork.repository.paging.Paginator;
import socialnetwork.repository.paging.PagingRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.StreamSupport;

public class NotificationDBRepository implements PagingRepository<Long, NotificationDTO> {

    private final String url;
    private final String username;
    private final String password;

    public NotificationDBRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private NotificationDTO extractEntity(ResultSet resultSet) throws SQLException {
        Long receivedId = resultSet.getLong("sendto");
        LocalDateTime date = resultSet.getTimestamp("datesent").toLocalDateTime();
        String typeStr = resultSet.getString("notificationmsg");
        boolean seen = resultSet.getBoolean("seen");

        NotificationDTO notification = new NotificationDTO(receivedId, date, typeStr, seen);
        notification.setId(resultSet.getLong("id"));
        return notification;
    }

    private PreparedStatement createEntity(PreparedStatement statement, NotificationDTO notification) throws SQLException {
        statement.setLong(1, notification.getTo());
        statement.setTimestamp(2, Timestamp.valueOf(notification.getDate()));
        statement.setString(3, notification.getType());
        statement.setBoolean(4, notification.getSeen());
        return statement;
    }

    @Override
    public NotificationDTO findOne(Long aLong) {

        if (aLong == null)
            throw new ValidationException("id must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            PreparedStatement statement = connection.prepareStatement("SELECT * from notifications where id = ?");
            statement.setLong(1, aLong);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next())
                return null;

            return extractEntity(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterable<NotificationDTO> findAll() {
        Set<NotificationDTO> notifications = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from notifications");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                notifications.add(extractEntity(resultSet));
            }
            return notifications;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    @Override
    public NotificationDTO save(NotificationDTO entity) {
        if (entity == null)
            throw new ValidationException("Entity must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO notifications (sendto, datesent, notificationmsg, seen) VALUES (?, ?, ?, ?)");
            statement = createEntity(statement, entity);
            statement.execute();
            return null;
        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate")) {
                return entity;
            }
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public NotificationDTO delete(Long aLong) {
        if (aLong == null)
            throw new ValidationException("id must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            NotificationDTO notification = findOne(aLong);
            if (notification == null)
                return null;

            String query = " DELETE FROM notifications WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, aLong);
            statement.execute();

            return notification;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public NotificationDTO update(NotificationDTO entity) {
        return null;
    }


    private Iterable<NotificationDTO> getAll(Long aLong) {

        Set<NotificationDTO> notifications = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("SELECT * from notifications where sendto = ? ORDER BY datesent DESC");
            statement.setLong(1, aLong);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                notifications.add(extractEntity(resultSet));
            }
            return notifications;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    @Override
    public Page<NotificationDTO> findAll(Pageable pageable, Long aLong, String type) {
        Paginator<NotificationDTO> paginator = new Paginator<>(pageable, getAll(aLong));
        return paginator.paginate();
    }


    @Override
    public int getNrPages(Pageable pageable, Long aLong, String type) {
        int nrEl = (int) StreamSupport.stream(getAll(aLong).spliterator(), false)
                .count();
        if (nrEl == 0) return 1;

        if (nrEl % pageable.getPageSize() == 0)
            return nrEl / pageable.getPageSize();
        else return nrEl / pageable.getPageSize() + 1;
    }
}
