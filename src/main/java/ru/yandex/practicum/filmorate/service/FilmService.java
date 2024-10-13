package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public List<Film> getFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(long id) {
        return filmStorage.getFilmById(id);
    }

    public void deleteFilmById(long filmId) {
        filmStorage.deleteFilmById(filmId);
    }

    public void addLike(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId);

        if (film != null) {
            if (userStorage.getUserById(userId) != null) {
                film.getLikes().add(userId);
                log.info("Пользователь {} добавил лайк к фильму {}", userId, filmId);
            } else {
                log.warn("Пользователь с id {} не найден", userId);
                throw new NotFoundException("Пользователь c id " + userId + " не найден");
            }
        } else {
            log.warn("Фильм с id {} не найден", filmId);
            throw new NotFoundException("Фильм c id " + filmId + " не найден");
        }
    }

    public void deleteLike(long filmId, long userId) {
        Film film = getFilmById(filmId);

        if (film == null) {
            log.warn("Фильм с id {} не найден", filmId);
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }

        User user = userStorage.getUserById(userId);

        if (user == null) {
            log.warn("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        if (!film.getLikes().remove(userId)) {
            log.warn("Лайк от пользователя с id {} не найден у фильма с id {}", userId, filmId);
            throw new NotFoundException("Лайк от пользователя с id " + userId + " не найден у фильма с id " + filmId);
        } else {
            log.info("У фильма с id {} удален лайк пользователя с id {}", filmId, userId);
        }
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted((film1, film2) -> Integer.compare(film2.getLikes().size(), film1.getLikes().size()))
                .limit(count)
                .toList();
    }
}