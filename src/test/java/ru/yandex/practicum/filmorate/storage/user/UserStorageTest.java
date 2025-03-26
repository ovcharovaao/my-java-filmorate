package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserStorageTest {
    private final UserStorage userStorage;

    @Autowired
    public UserStorageTest(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Test
    @Order(1)
    void shouldGetUserById() {
        User user = new User();
        user.setLogin("user1_" + System.currentTimeMillis());
        user.setName("User One");
        user.setEmail("user1_" + System.currentTimeMillis() + "@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 10));
        User savedUser = userStorage.addUser(user);

        Optional<User> userOptional = Optional.ofNullable(userStorage.getUserById(savedUser.getId()));
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u).hasFieldOrPropertyWithValue("id", savedUser.getId());
                    assertThat(u).hasFieldOrPropertyWithValue("login", user.getLogin());
                    assertThat(u).hasFieldOrPropertyWithValue("name", "User One");
                    assertThat(u).hasFieldOrPropertyWithValue("email", user.getEmail());
                    assertThat(u).hasFieldOrPropertyWithValue("birthday",
                            LocalDate.of(1990, 1, 10));
                });
    }

    @Test
    @Order(2)
    void shouldUpdateUser() {
        User user = new User();
        user.setLogin("user1");
        user.setName("User One");
        user.setEmail("user1@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 10));
        User savedUser = userStorage.addUser(user);

        User updatedUser = new User();
        updatedUser.setId(savedUser.getId());
        updatedUser.setLogin("updatedUser1");
        updatedUser.setName("Updated User One");
        updatedUser.setEmail("updated_user1@example.com");
        updatedUser.setBirthday(LocalDate.of(1985, 12, 12));

        User userFromDb = userStorage.updateUser(updatedUser);

        assertThat(userFromDb)
                .hasFieldOrPropertyWithValue("id", savedUser.getId())
                .hasFieldOrPropertyWithValue("login", "updatedUser1")
                .hasFieldOrPropertyWithValue("name", "Updated User One")
                .hasFieldOrPropertyWithValue("email", "updated_user1@example.com")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1985, 12, 12));
    }

    @Test
    @Order(3)
    void shouldAddFriend() {
        User user1 = new User();
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setEmail("user1@example.com");
        user1.setBirthday(LocalDate.of(1990, 1, 10));
        User savedUser1 = userStorage.addUser(user1);

        User user2 = new User();
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setEmail("user2@example.com");
        user2.setBirthday(LocalDate.of(2000, 2, 10));
        User savedUser2 = userStorage.addUser(user2);

        userStorage.addFriend(savedUser1.getId(), savedUser2.getId());

        Set<User> friends = userStorage.getFriends(savedUser1.getId());
        assertThat(friends)
                .isNotEmpty()
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("id", savedUser2.getId())
                .hasFieldOrPropertyWithValue("name", "User Two");
    }
}