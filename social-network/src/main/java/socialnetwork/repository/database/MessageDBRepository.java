package socialnetwork.repository.database;

import socialnetwork.domain.dto.MessageDTO;
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

public class MessageDBRepository implements PagingRepository<Long, MessageDTO> {
    private final String url;
    private final String username;
    private final String password;
    private final Validator<MessageDTO> validator;

    public MessageDBRepository(String url, String username, String password, Validator<MessageDTO> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    private MessageDTO extractEntity(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        Long from = resultSet.getLong("fromuser");
        Long to = resultSet.getLong("togroup");
        String message = resultSet.getString("message");
        LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();
        Long idReply = resultSet.getLong("reply");

        MessageDTO messageDTO = new MessageDTO(from, to, message, date);
        if (idReply != 0)
            messageDTO.setReply(idReply);
        messageDTO.setId(id);
        return messageDTO;
    }

    @Override
    public MessageDTO findOne(Long aLong) {
        if (aLong == null)
            throw new ValidationException("id must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM messages WHERE id = ?");
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
    public Iterable<MessageDTO> findAll() {
        Set<MessageDTO> messages = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM messages");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                messages.add(extractEntity(resultSet));
            }
            return messages;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    @Override
    public MessageDTO save(MessageDTO entity) {
        validator.validate(entity);

        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            PreparedStatement statement = connection.prepareStatement("INSERT INTO messages (fromuser, togroup, message, date, reply) VALUES (?, ?, ?, ?, ?)");
            statement.setLong(1, entity.getFromUser());
            statement.setLong(2, entity.getToGroup());
            statement.setString(3, entity.getMessage());
            statement.setTimestamp(4, Timestamp.valueOf(entity.getDate()));
            if (entity.getReply() != null)
                statement.setLong(5, entity.getReply());
            else statement.setNull(5, Types.BIGINT);

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
    public MessageDTO delete(Long aLong) {
        if (aLong == null)
            throw new ValidationException("id must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            MessageDTO msg = findOne(aLong);
            if (msg == null)
                return null;
            PreparedStatement statement = connection.prepareStatement("DELETE FROM messages WHERE id = ?");
            statement.setLong(1, aLong);
            statement.execute();

            return msg;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public MessageDTO update(MessageDTO entity) {
        validator.validate(entity);

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("UPDATE messages SET message = ?, date = ? WHERE id = ?");
            statement.setString(1, entity.getMessage());
            statement.setTimestamp(2, Timestamp.valueOf(entity.getDate()));
            statement.setLong(3, entity.getId());

            int nrRows = statement.executeUpdate();
            if (nrRows == 0)
                return entity;
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Iterable<MessageDTO> getAll(Long idGroup) {
        Set<MessageDTO> messages = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM messages WHERE togroup = ? ORDER BY date");
            statement.setLong(1, idGroup);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                messages.add(extractEntity(resultSet));
            }
            return messages;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    @Override
    public Page<MessageDTO> findAll(Pageable pageable, Long id, String type) {
        Paginator<MessageDTO> paginator = new Paginator<>(pageable, this.getAll(id));
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
