package socialnetwork.repository.database;

import socialnetwork.domain.mainDom.User;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.domain.validators.Validator;
import socialnetwork.repository.paging.Page;
import socialnetwork.repository.paging.Pageable;
import socialnetwork.repository.paging.Paginator;
import socialnetwork.repository.paging.PagingRepository;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.StreamSupport;

public class UserDBRepository implements PagingRepository<Long, User> {
    private final String url;
    private final String username;
    private final String password;
    private final Validator<User> validator;

    public UserDBRepository(String url, String username, String password, Validator<User> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    public User extractEntity(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        String firstName = resultSet.getString("first_name");
        String lastName = resultSet.getString("last_name");
        String email = resultSet.getString("email");
        String username = resultSet.getString("username");
        String password = resultSet.getString("password");
        String role = resultSet.getString("role");
        String image = resultSet.getString("imageurl");
        User user = new User(firstName, lastName, email, username, password, image);
        user.setId(id);
        user.setRole(role);
        return user;
    }

    public PreparedStatement createEntity(PreparedStatement statement, User entity) throws SQLException {
        statement.setString(1, entity.getFirstName());
        statement.setString(2, entity.getLastName());
        statement.setString(3, entity.getEmail());
        statement.setString(4, entity.getUsername());
        statement.setString(5, entity.getPassword());
        statement.setString(6, entity.getImage());

        return statement;
    }

    @Override
    public User findOne(Long aLong) {

        if (aLong == null)
            throw new ValidationException("id must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            PreparedStatement statement = connection.prepareStatement("SELECT * from users where id = ?");
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
    public Iterable<User> findAll() {
        Set<User> users = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from users ORDER BY id");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(extractEntity(resultSet));
            }
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public User save(User entity) {
        if (entity == null)
            throw new ValidationException("entity must be not null");
        validator.validate(entity);

        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            String query = " INSERT INTO users (first_name, last_name, email, username, password, imageurl)" + " values (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement = createEntity(statement, entity);
            statement.execute();
            return null;

        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate")) {
                throw new ValidationException("Username taken! ");
            }
            e.printStackTrace();
        }
        return entity;
    }

    @Override
    public User delete(Long aLong) {

        if (aLong == null)
            throw new ValidationException("id must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            User user = findOne(aLong);
            if (user == null)
                return null;

            String query = " DELETE FROM users WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, aLong);
            statement.execute();

            return user;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User update(User entity) {
        if (entity == null)
            throw new ValidationException("entity must be not null");
        validator.validate(entity);

        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            String query = " UPDATE users SET first_name = ?, last_name = ?, email = ?, username =?, password = ?, imageurl = ? where id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement = createEntity(statement, entity);
            statement.setLong(7, entity.getId());

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
    public Page<User> findAll(Pageable pageable, Long id, String type) {
        Paginator<User> paginator = new Paginator<>(pageable, this.findAll());
        return paginator.paginate();
    }

    @Override
    public int getNrPages(Pageable pageable, Long aLong, String type) {
        int nrEl = (int) StreamSupport.stream(this.findAll().spliterator(), false)
                .count();
        if (nrEl == 0) return 1;

        if (nrEl % pageable.getPageSize() == 0)
            return nrEl / pageable.getPageSize();
        else return nrEl / pageable.getPageSize() + 1;
    }
}
