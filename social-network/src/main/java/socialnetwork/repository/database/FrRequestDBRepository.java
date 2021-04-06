package socialnetwork.repository.database;

import socialnetwork.domain.Status;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.mainDom.FriendRequest;
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

public class FrRequestDBRepository implements PagingRepository<Tuple<Long, Long>, FriendRequest> {

    private final String url;
    private final String username;
    private final String password;

    public FrRequestDBRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private FriendRequest extractEntity(ResultSet resultSet) throws SQLException {
        Long id1 = resultSet.getLong("sender");
        Long id2 = resultSet.getLong("receiver");
        String st = resultSet.getString("status");
        LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();

        Status status = switch (st) {
            case "approved" -> Status.APPROVED;
            case "pending" -> Status.PENDING;
            default -> Status.REJECTED;
        };
        FriendRequest frR = new FriendRequest(status, date);
        frR.setId(new Tuple<>(id1, id2));
        return frR;
    }

    private PreparedStatement createEntity(PreparedStatement statement, FriendRequest entity) throws SQLException {
        statement.setString(1, entity.getStatus().toString().toLowerCase());
        statement.setTimestamp(2, Timestamp.valueOf(entity.getDate()));
        statement.setLong(3, entity.getId().getLeft());
        statement.setLong(4, entity.getId().getRight());
        return statement;
    }

    @Override
    public FriendRequest findOne(Tuple<Long, Long> longLongTuple) {
        if (longLongTuple == null)
            throw new ValidationException("id must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM friendrequests WHERE sender = ? and receiver = ?");
            statement.setLong(1, longLongTuple.getLeft());
            statement.setLong(2, longLongTuple.getRight());

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
    public Iterable<FriendRequest> findAll() {
        Set<FriendRequest> friendRequests = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM friendrequests");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                friendRequests.add(extractEntity(resultSet));
            }
            return friendRequests;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public FriendRequest save(FriendRequest entity) {
        if (entity == null)
            throw new ValidationException("Entity must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO friendrequests (status, date, sender, receiver) VALUES (?, ?, ?, ?)");
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
    public FriendRequest delete(Tuple<Long, Long> longLongTuple) {
        if (longLongTuple == null)
            throw new ValidationException("id must not be null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            FriendRequest frR = findOne(longLongTuple);
            if (frR == null)
                return null;

            PreparedStatement statement = connection.prepareStatement("DELETE FROM friendrequests WHERE sender = ? AND receiver = ?");
            statement.setLong(1, longLongTuple.getLeft());
            statement.setLong(2, longLongTuple.getRight());
            statement.execute();

            return frR;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public FriendRequest update(FriendRequest entity) {
        if (entity == null)
            throw new ValidationException("Entity must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("UPDATE friendrequests SET status = ?, date = ? WHERE sender = ? AND receiver = ?");
            statement = createEntity(statement, entity);

            int nrRows = statement.executeUpdate();
            if (nrRows == 0)
                return entity;
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Iterable<FriendRequest> getAll(Long idUser, String type) {
        Set<FriendRequest> friendRequests = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement;
            switch (type) {
                case "sent" -> {
                    statement = connection.prepareStatement("SELECT * FROM friendrequests WHERE sender = ?");
                    statement.setLong(1, idUser);
                }
                default -> {
                    statement = connection.prepareStatement("SELECT * FROM friendrequests WHERE receiver = ?");
                    statement.setLong(1, idUser);
                }
            }
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                friendRequests.add(extractEntity(resultSet));
            }
            return friendRequests;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Page<FriendRequest> findAll(Pageable pageable, Long id, String type) {
        Paginator<FriendRequest> paginator = new Paginator<>(pageable, this.getAll(id, type));
        return paginator.paginate();
    }

    @Override
    public int getNrPages(Pageable pageable, Long aLong, String type) {
        int nrEl = (int) StreamSupport.stream(getAll(aLong, type).spliterator(), false)
                .count();
        if (nrEl == 0) return 1;

        if (nrEl % pageable.getPageSize() == 0)
            return nrEl / pageable.getPageSize();
        else return nrEl / pageable.getPageSize() + 1;
    }
}
