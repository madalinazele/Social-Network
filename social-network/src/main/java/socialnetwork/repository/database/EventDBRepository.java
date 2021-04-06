package socialnetwork.repository.database;

import javafx.util.Pair;
import socialnetwork.domain.mainDom.Event;
import socialnetwork.domain.mainDom.User;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.repository.paging.Page;
import socialnetwork.repository.paging.Pageable;
import socialnetwork.repository.paging.Paginator;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

public class EventDBRepository implements MembersRepository<Long, Event, Pair<Long, Boolean>> {
    public String url;
    public String username;
    public String password;

    public EventDBRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private Event extractEntity(ResultSet resultSet) throws SQLException {
        Long idEvent = resultSet.getLong("id");
        String title = resultSet.getString("title");
        String description = resultSet.getString("description");
        LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();
        Long organizer = resultSet.getLong("organizer");

        Event event = new Event(title, description, date, organizer, "");
        event.setId(idEvent);

        return event;
    }

    public User getUser(Long idUser, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE id= ?");
        statement.setLong(1, idUser);

        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            String firstName = resultSet.getString("first_name");
            String lastName = resultSet.getString("last_name");
            String email = resultSet.getString("email");
            String username = resultSet.getString("username");
            String password = resultSet.getString("password");
            String image = resultSet.getString("imageURL");
            //String image = resultSet.getString("imageURL");
            User user = new User(firstName, lastName, email, username, password, image);
            user.setId(idUser);
            return user;
        }
        return null;
    }

    @Override
    public Event findOne(Long aLong) {
        if (aLong == null)
            throw new ValidationException("id must not be null!");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT E.* FROM events E WHERE E.id = ?");
            preparedStatement.setLong(1, aLong);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Event event = extractEntity(resultSet);
                HashMap<Long, Boolean> participants = new HashMap<>();

                preparedStatement = connection.prepareStatement("SELECT id_user, sendnotifications FROM participants WHERE id_event = ?");
                preparedStatement.setLong(1, event.getId());
                ResultSet results = preparedStatement.executeQuery();

                while (results.next()) {
                    Long idUser = results.getLong("id_user");
                    Boolean notifiable = results.getBoolean("sendnotifications");
                    participants.put(idUser, notifiable);
                }
                event.setParticipants(participants);
                return event;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterable<Event> findAll() {
        Set<Event> allEvents = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement
                    ("SELECT E.* FROM events E ORDER BY E.id");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Event newEvent = extractEntity(resultSet);

                statement = connection.prepareStatement
                        ("SELECT id_user, sendnotifications  FROM participants WHERE id_event = ? ORDER BY id_user");
                statement.setLong(1, newEvent.getId());
                ResultSet results = statement.executeQuery();

                HashMap<Long, Boolean> participants = new HashMap<>();
                while (results.next()) {
                    Long idUser = results.getLong("id_user");
                    Boolean notifiable = results.getBoolean("sendnotifications");
                    participants.put(idUser, notifiable);
                }
                newEvent.setParticipants(participants);
                allEvents.add(newEvent);
            }
            return allEvents;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Event save(Event entity) {
        if (entity == null)
            throw new ValidationException("Entity must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO events (title, description, date, organizer) VALUES (?, ?, ?, ?)");
            statement.setString(1, entity.getTitle());
            statement.setString(2, entity.getDescription());
            statement.setTimestamp(3, Timestamp.valueOf(entity.getDate()));
            statement.setLong(4, entity.getOrganizer());
            statement.execute();

            statement = connection.prepareStatement("SELECT MAX(id) FROM events");
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                long idEvent = resultSet.getLong(1);


                for (Map.Entry<Long, Boolean> participant : entity.getParticipants().entrySet()) {
                    PreparedStatement statement2 = connection.prepareStatement("INSERT INTO participants (id_event, id_user, sendnotifications) VALUES (?, ?, ?)");
                    statement2.setLong(1, idEvent);
                    statement2.setLong(2, participant.getKey());
                    statement2.setBoolean(3, participant.getValue());
                    statement2.execute();
                }
            }
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
    public Event delete(Long aLong) {
        if (aLong == null)
            throw new ValidationException("id must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            Event event = findOne(aLong);
            if (event == null)
                return null;

            PreparedStatement statement = connection.prepareStatement("DELETE FROM events WHERE id = ?");
            statement.setLong(1, aLong);
            statement.executeUpdate();
            return event;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Event update(Event entity) {
        if (entity == null)
            throw new ValidationException("Entity must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("UPDATE events SET title = ?, description = ?, date = ?, organizer = ? WHERE id = ?");
            statement.setString(1, entity.getTitle());
            statement.setString(2, entity.getDescription());
            statement.setTimestamp(3, Timestamp.valueOf(entity.getDate()));
            statement.setLong(4, entity.getOrganizer());
            statement.setLong(5, entity.getId());

            int nrRows = statement.executeUpdate();
            if (nrRows == 0)
                return entity;
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterable<Pair<Long, Boolean>> findAll(Event entity) {
        return null;
    }

    @Override
    public Event addMember(Event entity, Pair<Long, Boolean> member) {
        if (entity == null)
            throw new ValidationException("Entity must be not null");

        if (member == null)
            throw new ValidationException("Member must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO participants (id_user, id_event, sendNotifications) VALUES (?, ?, ?)");
            statement.setLong(1, member.getKey());
            statement.setLong(2, entity.getId());
            statement.setBoolean(3, member.getValue());
            statement.execute();

            entity.addParticipant(member);
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
    public Event updateMember(Event entity, Pair<Long, Boolean> member) {
        if (entity == null)
            throw new ValidationException("Entity must be not null");

        if (member == null)
            throw new ValidationException("Member must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("UPDATE participants SET sendNotifications = ? WHERE id_event = ? AND id_user = ?");
            statement.setBoolean(1, member.getValue());
            statement.setLong(2, entity.getId());
            statement.setLong(3, member.getKey());
            statement.execute();

            entity.addParticipant(member);
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
    public Event deleteMember(Event entity, Pair<Long, Boolean> member) {
        if (entity == null)
            throw new ValidationException("Entity must be not null");

        if (member == null)
            throw new ValidationException("Member must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM participants WHERE id_event =? AND id_user = ?");
            statement.setLong(1, entity.getId());
            statement.setLong(2, member.getKey());
            statement.execute();

            entity.removeParticipant(member.getKey());
            return entity;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Iterable<Event> getAll(Long idUser) {
        Set<Event> allEvents = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement
                    ("SELECT E.*  FROM events E INNER JOIN participants p on E.id = p.id_event WHERE p.id_user = ? ORDER BY E.date DESC");
            statement.setLong(1, idUser);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Event newEvent = extractEntity(resultSet);

                statement = connection.prepareStatement
                        ("SELECT id_user, sendnotifications  FROM participants WHERE id_event = ? ORDER BY id_user");
                statement.setLong(1, newEvent.getId());
                ResultSet results = statement.executeQuery();

                HashMap<Long, Boolean> participants = new HashMap<>();
                while (results.next()) {
                    Long idU = results.getLong("id_user");
                    Boolean notifiable = results.getBoolean("sendnotifications");
                    participants.put(idU, notifiable);
                }
                newEvent.setParticipants(participants);
                allEvents.add(newEvent);
            }
            return allEvents;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Page<Event> findAll(Pageable pageable, Long id, String type) {
        Paginator<Event> paginator;
        switch (type) {
            case "allEvents":
                paginator = new Paginator<>(pageable, this.findAll());
                break;
            case "myEvents":
            default:
                paginator = new Paginator<>(pageable, this.getAll(id));
        }

        return paginator.paginate();
    }

    @Override
    public int getNrPages(Pageable pageable, Long aLong, String type) {
        int nrEl;
        switch (type) {
            case "allEvents":
                nrEl = (int) StreamSupport.stream(this.findAll().spliterator(), false)
                        .count();
                break;
            case "myEvents":
            default:
                nrEl = (int) StreamSupport.stream(this.getAll(aLong).spliterator(), false)
                        .count();
        }
        if (nrEl == 0) return 1;
        if (nrEl % pageable.getPageSize() == 0)
            return nrEl / pageable.getPageSize();
        else return nrEl / pageable.getPageSize() + 1;
    }
}
