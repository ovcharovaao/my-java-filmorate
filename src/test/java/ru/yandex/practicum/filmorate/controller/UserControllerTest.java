package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private final User user = new User();
    UserController uc = new UserController();

    @BeforeEach
    void setFilm() {
        user.setEmail("user@email.com");
        user.setLogin("Login");
        user.setName("User");
        user.setBirthday(LocalDate.of(2000, 2, 10));
    }

    @Test
    void addUserWithIncorrectEmail() {
        user.setEmail("");

        assertThrows(ValidationException.class, () -> uc.addUser(user));

        user.setEmail("email");

        assertThrows(ValidationException.class, () -> uc.addUser(user));
        assertTrue(uc.getUsers().isEmpty());
    }

    @Test
    void addUserWithIncorrectLogin() {
        user.setLogin("");

        assertThrows(ValidationException.class, () -> uc.addUser(user));

        user.setLogin("L ogin");

        assertThrows(ValidationException.class, () -> uc.addUser(user));
        assertTrue(uc.getUsers().isEmpty());
    }

    @Test
    void addUserWithEmptyName() {
        user.setName("");
        uc.addUser(user);

        assertEquals(user.getName(), user.getLogin(),
                "Вместо имени пользователя должен быть использован логин");
    }

    @Test
    void addUserWithIncorrectBirthday() {
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> uc.addUser(user));
        assertTrue(uc.getUsers().isEmpty());
    }

    @Test
    void updateFilm() {
        uc.addUser(user);
        user.setName("UpdateName");
        User updatedUser = uc.updateUser(user);

        assertEquals("UpdateName", updatedUser.getName());

        User user1 = uc.getUsers().get(user.getId());
        assertEquals("UpdateName", user1.getName());
    }

    @Test
    void getFilms() {
        uc.addUser(user);
        Map<Long, User> usersMap = uc.getUsers();

        assertEquals(1, usersMap.size());
    }
}