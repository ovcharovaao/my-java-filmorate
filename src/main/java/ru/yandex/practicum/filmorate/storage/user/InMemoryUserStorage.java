package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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