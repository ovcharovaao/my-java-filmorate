package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User addUser(@RequestBody User user) {
        if (user == null) {
            log.error("Запрос на добавление пользователя пустой");
            throw new ValidationException("Запрос на добавление пользователя не может быть пустым");
        }
        userValidator(user);
        user.setId(getNextIdForUser());
        users.put(user.getId(), user);
        log.info("Добавлен пользователь {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User updatedUser) {
        if (updatedUser == null) {
            log.error("Запрос на обновление пользователя пустой");
            throw new ValidationException("Запрос на обновление пользователя не может быть пустым");
        }

        userValidator(updatedUser);

        if (!users.containsKey(updatedUser.getId())) {
            log.warn("Пользователь с id {} не найден", updatedUser.getId());
            throw new ValidationException("Пользователь не найден");
        }

        users.put(updatedUser.getId(), updatedUser);
        log.info("Обновлен пользователь с id {}: {}", updatedUser.getId(), updatedUser);

        return updatedUser;
    }

    private void userValidator(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Электронная почта пустая или не содержит символ @");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin().contains(" ") || user.getLogin().isBlank()) {
            log.error("Логин пустой или содержит пробелы");
            throw new ValidationException("Логин не может быть пустым и не может содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Вместо имени пользователя использован логин");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения указана будущей датой");
            throw new ValidationException("Дата рождения не может быть в будущем");
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