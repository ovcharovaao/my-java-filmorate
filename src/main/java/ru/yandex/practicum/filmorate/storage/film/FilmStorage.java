package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film getFilmById(long id);

    List<Film> getAllFilms();

    List<Film> getPopularFilms(int id);

    void deleteFilmById(long filmId);

    void addLike(long filmId, long userId);

    void deleteLike(long filmId, long userId);
}