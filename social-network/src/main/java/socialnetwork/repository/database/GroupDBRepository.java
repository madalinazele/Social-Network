package socialnetwork.repository.database;

import socialnetwork.domain.mainDom.Group;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.repository.paging.Page;
import socialnetwork.repository.paging.Pageable;
import socialnetwork.repository.paging.Paginator;
import socialnetwork.repository.paging.PagingRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

public class GroupDBRepository implements PagingRepository<Long, Group> {
    private final String url;
    private final String username;
    private final String password;

    public GroupDBRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    private Group extractEntity(ResultSet resultSet) throws SQLException {
        Long idGroup = resultSet.getLong("id");
        String groupName = resultSet.getString("groupname");
        Group group = new Group(groupName);
        group.setId(idGroup);
        return group;
    }

    @Override
    public Group findOne(Long aLong) {
        if (aLong == null)
            throw new ValidationException("id must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement
                    ("SELECT G.*, GM.iduser  FROM groups G, groupmembers GM WHERE G.id = GM.idGroup AND G.id = ?");
            statement.setLong(1, aLong);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Group group = extractEntity(resultSet);
                List<Long> usersID = new ArrayList<>();
                long idUser = resultSet.getLong("iduser");
                usersID.add(idUser);

                while (resultSet.next()) {
                    idUser = resultSet.getLong("iduser");
                    usersID.add(idUser);
                }
                group.setMembers(usersID);
                return group;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterable<Group> findAll() {
        Set<Group> allGroups = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement
                    ("SELECT G.*, GM.iduser  FROM groups G INNER JOIN groupmembers GM ON G.id = GM.idGroup ORDER BY G.id, GM.iduser");
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Group newGroup = extractEntity(resultSet);
                long idUser = resultSet.getLong("iduser");
                List<Long> users = new ArrayList<>();
                users.add(idUser);

                while (resultSet.next()) {
                    if (resultSet.getLong("id") != newGroup.getId()) {
                        newGroup.setMembers(users);
                        allGroups.add(newGroup);
                        newGroup = extractEntity(resultSet);
                        users = new ArrayList<>();
                    }
                    idUser = resultSet.getLong("iduser");
                    users.add(idUser);
                }

                newGroup.setMembers(users);
                allGroups.add(newGroup);
                return allGroups;
            }
            return allGroups;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Group save(Group entity) {
        if (entity == null)
            throw new ValidationException("Entity must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO groups (groupname) VALUES (?)");
            statement.setString(1, entity.getGroupName());
            statement.executeUpdate();

            statement = connection.prepareStatement("SELECT MAX(id) FROM groups");
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                long idGroup = resultSet.getLong(1);

                for (Long idUser : entity.getMembers()) {
                    PreparedStatement statement2 = connection.prepareStatement("INSERT INTO groupmembers (idGroup, iduser) VALUES (?, ?)");
                    statement2.setLong(1, idGroup);
                    statement2.setLong(2, idUser);
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
    public Group delete(Long aLong) {
        if (aLong == null)
            throw new ValidationException("id must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            Group group = findOne(aLong);
            if (group == null)
                return null;

            PreparedStatement statement = connection.prepareStatement("DELETE FROM groups WHERE id = ?");
            statement.setLong(1, aLong);
            statement.executeUpdate();
            return group;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Group update(Group entity) {
        if (entity == null)
            throw new ValidationException("Entity must be not null");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement("UPDATE groups SET groupname = ? WHERE id = ?");
            statement.setString(1, entity.getGroupName());
            statement.setLong(2, entity.getId());

            int nrRows = statement.executeUpdate();
            if (nrRows == 0)
                return entity;
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Long> findMembers(Long idGroup) {

        List<Long> members = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT GM.iduser FROM groups G INNER JOIN groupmembers GM on G.id =GM.idgroup WHERE G.id = ?"
            );
            statement.setLong(1, idGroup);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Long idUser = resultSet.getLong("iduser");
                members.add(idUser);
            }
            return members;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    private Iterable<Group> getAll(Long aLong, String type) {
        Set<Group> allGroups = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            PreparedStatement statement = connection.prepareStatement
                    ("SELECT G.* FROM groups G INNER JOIN groupmembers GM ON G.id = GM.idGroup WHERE iduser = ? ORDER BY G.id");
            statement.setLong(1, aLong);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Group newGroup = extractEntity(resultSet);
                List<Long> users = findMembers(newGroup.getId());
                newGroup.setMembers(users);

                switch (type) {
                    case "privateChats":
                        if (users.size() == 2)
                            allGroups.add(newGroup);
                        break;
                    case "groups":
                        if (users.size() > 2)
                            allGroups.add(newGroup);
                }
            }
            return allGroups;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allGroups;
    }

    @Override
    public Page<Group> findAll(Pageable pageable, Long aLong, String type) {
        Paginator<Group> paginator = new Paginator<>(pageable, this.getAll(aLong, type));
        return paginator.paginate();
    }

    @Override
    public int getNrPages(Pageable pageable, Long id, String type) {
        int nrEl = (int) StreamSupport.stream(getAll(id, type).spliterator(), false)
                .count();
        if (nrEl == 0) return 1;

        if (nrEl % pageable.getPageSize() == 0)
            return nrEl / pageable.getPageSize();
        else return nrEl / pageable.getPageSize() + 1;
    }
}
