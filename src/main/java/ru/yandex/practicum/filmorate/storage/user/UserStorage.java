package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;

public interface UserStorage {

    User addUser(User user);

    User updateUser(User user);

    User getUserById(long id);

    List<User> getAllUsers();

    void deleteUserById(Long userId);

    void addFriend(long userId, long friendId);

    void confirmFriendship(long userId, long friendId);

    void deleteFriend(long userId, long friendId);

    Set<User> getFriends(long userId);

    Set<User> getCommonFriends(long userId, long otherId);
}