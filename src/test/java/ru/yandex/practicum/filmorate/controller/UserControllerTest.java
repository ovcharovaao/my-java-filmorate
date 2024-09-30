package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class UserControllerTest {
    UserController uc = new UserController();

    @Autowired
    Validator validator;

    User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("user@email.com");
        user.setLogin("Login");
        user.setName("User");
        user.setBirthday(LocalDate.of(2000, 2, 10));
    }

    @Test
    @DisplayName("Создание пользователя с некорректным имейлом")
    void addUserWithIncorrectEmail() {
        user.setEmail("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Электронная почта не может быть пустой", violations.iterator().next().getMessage());

        user.setEmail("email");
        violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Электронная почта должна содержать символ @", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Создание пользователя с некорректным логином")
    void addUserWithEmptyLogin() {
        user.setLogin("");
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        List<String> messages = violations.stream().map(ConstraintViolation::getMessage).toList();
        assertTrue(messages.contains("Логин не может быть пустым"));
        assertTrue(messages.contains("Логин не может содержать пробелы"));
    }

    @Test
    @DisplayName("Создание пользователя с пустым именем")
    void addUserWithEmptyName() {
        user.setName("");
        uc.addUser(user);

        assertEquals(user.getName(), user.getLogin(),
                "Вместо имени пользователя должен быть использован логин");
    }

    @Test
    @DisplayName("Создание пользователя с датой рождения в будущем")
    void addUserWithIncorrectBirthday() {
        user.setBirthday(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Дата рождения не может быть в будущем", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Получение списка пользователей")
    void getFilms() {
        uc.addUser(user);
        List<User> users = uc.getUsers();

        assertEquals(1, users.size());
    }
}