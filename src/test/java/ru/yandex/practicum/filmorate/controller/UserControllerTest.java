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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {
    @Autowired
    private UserController userController;

    @Autowired
    private Validator validator;

    private User user;

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
        userController.addUser(user);

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
    @DisplayName("Добавление друга")
    void addFriend() {
        User friend = new User();
        friend.setEmail("friend@email.com");
        friend.setLogin("FriendLogin");
        friend.setName("Friend");
        friend.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userController.addUser(user);
        User createdFriend = userController.addUser(friend);

        userController.addFriend(createdUser.getId(), createdFriend.getId());

        List<User> friends = userController.getFriends(createdUser.getId());
        assertTrue(friends.contains(createdFriend), "Друг не был добавлен");
    }

    @Test
    @DisplayName("Удаление друга")
    void removeFriend() {
        User friend = new User();
        friend.setEmail("friend@email.com");
        friend.setLogin("FriendLogin");
        friend.setName("Friend");
        friend.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userController.addUser(user);
        User createdFriend = userController.addUser(friend);

        userController.addFriend(createdUser.getId(), createdFriend.getId());
        userController.deleteFriend(createdUser.getId(), createdFriend.getId());

        List<User> friends = userController.getFriends(createdUser.getId());
        assertFalse(friends.contains(createdFriend), "Друг не был удален");
    }

    @Test
    @DisplayName("Получение списка друзей")
    void getFriends() {
        User friend1 = new User();
        friend1.setEmail("friend1@email.com");
        friend1.setLogin("FriendLogin1");
        friend1.setName("Friend1");
        friend1.setBirthday(LocalDate.of(2000, 1, 1));

        User createdUser = userController.addUser(user);
        User createdFriend1 = userController.addUser(friend1);
        userController.addFriend(createdUser.getId(), createdFriend1.getId());

        List<User> friends = userController.getFriends(createdUser.getId());
        assertEquals(1, friends.size(), "Список друзей должен содержать 1 пользователя");
    }

    @Test
    @DisplayName("Получение общих друзей")
    void getCommonFriends() {
        User user1 = userController.addUser(user);

        User user2 = new User();
        user2.setEmail("user2@email.com");
        user2.setLogin("User2Login");
        user2.setName("User2");
        user2.setBirthday(LocalDate.of(2000, 1, 1));
        User createdUser2 = userController.addUser(user2);

        User friend = new User();
        friend.setEmail("friend@email.com");
        friend.setLogin("FriendLogin");
        friend.setName("Friend");
        friend.setBirthday(LocalDate.of(2000, 1, 1));
        User createdFriend = userController.addUser(friend);

        userController.addFriend(user1.getId(), createdFriend.getId());
        userController.addFriend(createdUser2.getId(), createdFriend.getId());

        List<User> commonFriends = userController.getCommonFriends(user1.getId(), createdUser2.getId());
        assertEquals(1, commonFriends.size(), "Должен быть 1 общий друг");
        assertTrue(commonFriends.contains(createdFriend), "Общий друг не найден");
    }
}