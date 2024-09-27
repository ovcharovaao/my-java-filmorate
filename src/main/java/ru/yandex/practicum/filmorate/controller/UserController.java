package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Map<Long, User> getUsers() {
        return users;
    }

    @PostMapping
    public void addUser(@RequestBody User user) {
        try {
            user.setId(getNextIdForUser());
            userValidator(user);
            users.put(user.getId(), user);
            log.info("Добавлен пользователь {}", user);
        } catch (ValidationException e) {
            log.error("Ошибка валидации при добавлении пользователя: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping
    public User updateUser(@RequestBody User updatedUser) {
        try {
            if (!users.containsKey(updatedUser.getId())) {
                log.warn("Пользователь с id {} не найден, добавление нового пользователя", updatedUser.getId());
                updatedUser.setId(getNextIdForUser());
                userValidator(updatedUser);
                users.put(updatedUser.getId(), updatedUser);
                log.info("Добавлен новый пользователь {}", updatedUser);
            } else {
                users.put(updatedUser.getId(), updatedUser);
                log.info("Обновлен пользователь с id {}: {}", updatedUser.getId(), updatedUser);
            }

            return updatedUser;
        } catch (ValidationException e) {
            log.error("Ошибка валидации при обновлении пользователя: {}", e.getMessage());
            throw e;
        }
    }

    private void userValidator(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.error("Электронная почта или не модержит символ @");
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