package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User addUser(User user) {
        userValidator(user);
        user.setId(getNextIdForUser());
        users.put(user.getId(), user);
        log.info("Добавлен пользователь {}", user);
        return user;
    }

    @Override
    public User updateUser(User updatedUser) {
        userValidator(updatedUser);

        if (!users.containsKey(updatedUser.getId())) {
            log.warn("Пользователь с id {} не найден", updatedUser.getId());
            throw new NotFoundException("Пользователь не найден");
        }

        users.put(updatedUser.getId(), updatedUser);
        log.info("Обновлен пользователь с id {}: {}", updatedUser.getId(), updatedUser);
        return updatedUser;
    }

    @Override
    public User getUserById(long id) {
        User user = users.get(id);

        if (user == null) {
            log.warn("Пользователь с id {} не найден", id);
            throw new NotFoundException("Пользователь не найден");
        }

        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteUserById(Long userId) {

        if (users.remove(userId) == null) {
            log.warn("Пользователь с id {} не найден", userId);
            throw new ValidationException("Пользователь не найден");
        }

        log.info("Пользователь с id {} удален", userId);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);

        if (user == null || friend == null) {
            log.warn("Один из пользователей не существует: userId={}, friendId={}", userId, friendId);
            throw new NotFoundException("Один из пользователей не существует.");
        }

        if (user.getFriends().contains(friendId)) {
            log.warn("Пользователь {} уже является другом пользователя {}", userId, friendId);
            return;
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        log.info("Добавлена дружба между пользователем {} и {}", userId, friendId);
    }

    @Override
    public void confirmFriendship(long userId, long friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);

        if (user == null || friend == null) {
            log.warn("Один из пользователей не существует: userId={}, friendId={}", userId, friendId);
            throw new NotFoundException("Один из пользователей не существует.");
        }

        if (!user.getFriends().contains(friendId) || !friend.getFriends().contains(userId)) {
            log.warn("Дружба между пользователями {} и {} еще не подтверждена", userId, friendId);
            throw new ValidationException("Дружба еще не подтверждена.");
        }

        log.info("Дружба между пользователями {} и {} подтверждена", userId, friendId);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);

        if (user == null || friend == null) {
            log.warn("Один из пользователей не существует: userId={}, friendId={}", userId, friendId);
            throw new NotFoundException("Один из пользователей не существует.");
        }

        if (!user.getFriends().remove(friendId)) {
            log.warn("Дружба между пользователями {} и {} не найдена.", userId, friendId);
            throw new ValidationException("Дружба не найдена.");
        }

        if (!friend.getFriends().remove(userId)) {
            log.warn("Дружба между пользователями {} и {} не найдена.", userId, friendId);
            throw new ValidationException("Дружба не найдена.");
        }

        log.info("Удалена дружба между пользователем {} и {}", userId, friendId);
    }

    @Override
    public Set<User> getFriends(long userId) {
        User user = users.get(userId);

        if (user == null) {
            log.warn("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь не найден.");
        }

        Set<User> friends = new HashSet<>();
        for (Long friendId : user.getFriends()) {
            User friend = users.get(friendId);
            if (friend != null) {
                friends.add(friend);
            }
        }

        return friends;
    }


    @Override
    public Set<User> getCommonFriends(long userId, long otherId) {
        User user = users.get(userId);
        User otherUser = users.get(otherId);

        if (user == null || otherUser == null) {
            log.warn("Один из пользователей не существует: userId={}, otherId={}", userId, otherId);
            throw new NotFoundException("Один из пользователей не существует.");
        }

        Set<User> commonFriends = new HashSet<>();

        for (Long friendId : user.getFriends()) {
            if (otherUser.getFriends().contains(friendId)) {
                User commonFriend = users.get(friendId);
                if (commonFriend != null) {
                    commonFriends.add(commonFriend);
                }
            }
        }

        return commonFriends;
    }

    private void userValidator(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Вместо имени пользователя использован логин");
        }
    }

    private long getNextIdForUser() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}