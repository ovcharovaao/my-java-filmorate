package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.FriendNotFoundException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User addUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"USER_ID"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );

        if (rowsUpdated == 0) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден.");
        }

        return getUserById(user.getId());
    }

    @Override
    public User getUserById(long id) {
        try {
            String sql = "SELECT * FROM users WHERE user_id = ?";
            return jdbcTemplate.queryForObject(sql, new UserMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(e.getMessage());
        }

    }

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, new UserMapper());
    }

    @Override
    public void deleteUserById(Long userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        String checkUserExists = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer userExists = jdbcTemplate.queryForObject(checkUserExists, Integer.class, userId);
        Integer friendExists = jdbcTemplate.queryForObject(checkUserExists, Integer.class, friendId);

        if (userExists == 0 || friendExists == 0) {
            throw new NotFoundException("Один из пользователей не существует.");
        }

        String sqlAddFriendship = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlAddFriendship, userId, friendId);

        log.info("Добавлена дружба между пользователем {} и {}", userId, friendId);
    }

    @Override
    public void confirmFriendship(long userId, long friendId) {
        String sql = "UPDATE friendships SET status = true WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        String sqlDeleteFriendship = "DELETE FROM friendships WHERE (user_id = ? AND friend_id = ?)";
        int rowsDeleted = jdbcTemplate.update(sqlDeleteFriendship, userId, friendId);

        if (getUserById(userId) == null || getUserById(friendId) == null) {
            throw new NotFoundException("User of Friend not found!");
        }

        if (rowsDeleted == 0) {
            log.warn("Дружба между пользователями с ID {} и {} не найдена.", userId, friendId);
            throw new FriendNotFoundException("Дружба между пользователями с ID " + userId + " и " + friendId + " не найдена.");
        }

        log.info("Удалена дружба между пользователем {} и {}", userId, friendId);
    }

    @Override
    public Set<User> getFriends(long userId) {
        String sqlCheckUser = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer userCount = jdbcTemplate.queryForObject(sqlCheckUser, Integer.class, userId);

        if (userCount == null || userCount == 0) {
            log.warn("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }

        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ?";

        return new HashSet<>(jdbcTemplate.query(sql, new UserMapper(), userId));
    }

    @Override
    public Set<User> getCommonFriends(long userId, long otherId) {
        String sqlCheckUser = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer userCount1 = jdbcTemplate.queryForObject(sqlCheckUser, Integer.class, userId);
        Integer userCount2 = jdbcTemplate.queryForObject(sqlCheckUser, Integer.class, otherId);

        if (userCount1 == null || userCount1 == 0) {
            log.warn("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }
        if (userCount2 == null || userCount2 == 0) {
            log.warn("Пользователь с id {} не найден", otherId);
            throw new NotFoundException("Пользователь с id " + otherId + " не найден.");
        }

        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.user_id = f1.friend_id " +
                "JOIN friendships f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return new HashSet<>(jdbcTemplate.query(sql, new UserMapper(), userId, otherId));
    }

    private static class UserMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("user_id"));
            user.setEmail(rs.getString("email"));
            user.setName(rs.getString("name"));
            user.setLogin(rs.getString("login"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        }
    }
}