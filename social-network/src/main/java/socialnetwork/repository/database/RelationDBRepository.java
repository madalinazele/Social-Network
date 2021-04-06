package socialnetwork.repository.database;

import socialnetwork.domain.Tuple;
import socialnetwork.domain.mainDom.Relation;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.domain.validators.Validator;
import socialnetwork.repository.paging.Page;
import socialnetwork.repository.paging.Pageable;
import socialnetwork.repository.paging.Paginator;
import socialnetwork.repository.paging.PagingRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.StreamSupport;

public class RelationDBRepository implements PagingRepository<Tuple<Long, Long>, Relation> {
    private final String url;
    private final String username;
    private final String password;
    private final Validator<Relation> validator;

    public RelationDBRepository(String url, String username, String password, Validator<Relation> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    private Relation extractEntity(ResultSet resultSet) throws SQLException {
        Long id1 = resultSet.getLong("user1");
        Long id2 = resultSet.getLong("user2");
        LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();

        Relation rel = new Relation(date);
        rel.setId(new Tuple<>(id1, id2));
        return rel;
    }

    private PreparedStatement createEntity(PreparedStatement statement, Relation entity) throws SQLException {
        statement.setTimestamp(1, Timestamp.valueOf(entity.getDate()));
        statement.setLong(2, entity.getId().getLeft());
        statement.setLong(3, entity.getId().getRight());
        return statement;
    }

    @Override
    public Relation findOne(Tuple<Long, Long> longLongTuple) {
        if (longLongTuple == null)
            throw new ValidationException("id must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM relations WHERE user1 = ? and user2 = ?");
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
    public Iterable<Relation> findAll() {
        Set<Relation> relations = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM relations");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                relations.add(extractEntity(resultSet));
            }
            return relations;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return relations;
    }

    @Override
    public Relation save(Relation entity) {
        if (entity == null)
            throw new ValidationException("entity must not be null");
        validator.validate(entity);

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO relations (date, user1, user2) VALUES (?, ?, ?)");
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
    public Relation delete(Tuple<Long, Long> longLongTuple) {
        if (longLongTuple == null)
            throw new ValidationException("id must not be null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            Relation rel = findOne(longLongTuple);
            if (rel == null)
                return null;

            PreparedStatement statement = connection.prepareStatement("DELETE FROM relations WHERE user1 = ? AND user2 = ?");
            statement.setLong(1, longLongTuple.getLeft());
            statement.setLong(2, longLongTuple.getRight());
            statement.execute();

            return rel;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Relation update(Relation entity) {
        if (entity == null)
            throw new ValidationException("entity must not be null");
        validator.validate(entity);

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("UPDATE relations SET date = ? WHERE user1 = ? AND user2 = ?");
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

    private Iterable<Relation> getAll(Long aLong) {
        Set<Relation> relations = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM relations where user1 = ? OR user2 = ?");
            statement.setLong(1, aLong);
            statement.setLong(2, aLong);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                relations.add(extractEntity(resultSet));
            }
            return relations;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return relations;
    }

    @Override
    public Page<Relation> findAll(Pageable pageable, Long aLong, String type) {
        Paginator<Relation> paginator = new Paginator<>(pageable, this.getAll(aLong));
        return paginator.paginate();
    }

    @Override
    public int getNrPages(Pageable pageable, Long aLong, String type) {
        int nrEl = (int) StreamSupport.stream(this.getAll(aLong).spliterator(), false)
                .count();
        if (nrEl == 0) return 1;
        if (nrEl % pageable.getPageSize() == 0)
            return nrEl / pageable.getPageSize();
        else return nrEl / pageable.getPageSize() + 1;
    }
}

