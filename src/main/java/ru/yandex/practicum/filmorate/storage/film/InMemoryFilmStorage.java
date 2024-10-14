package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film addFilm(Film film) {
        film.setId(getNextIdForFilm());
        filmValidator(film);
        films.put(film.getId(), film);
        log.info("Фильм {} успешно добавлен", film);
        return film;
    }

    @Override
    public Film updateFilm(Film updatedFilm) {
        filmValidator(updatedFilm);

        if (!films.containsKey(updatedFilm.getId())) {
            log.warn("Фильм с id {} не найден", updatedFilm.getId());
            throw new NotFoundException("Фильм не найден");
        }

        films.put(updatedFilm.getId(), updatedFilm);
        log.info("Обновлен фильм с id {}: {}", updatedFilm.getId(), updatedFilm);

        return updatedFilm;
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilmById(long id) {
        Film film = films.get(id);

        if (film == null) {
            log.warn("Фильм с id {} не найден", id);
            throw new NotFoundException("Фильм не найден");
        }

        return film;
    }

    @Override
    public void deleteFilmById(Long filmId) {

        if (films.remove(filmId) == null) {
            log.warn("Фильм с id {} не найден", filmId);
            throw new NotFoundException("Фильм не найден");
        }

        log.info("Удален фильм с id {}", filmId);
    }

    private void filmValidator(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Дата релиза фильма ранее 28.12.1895");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    private long getNextIdForFilm() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}