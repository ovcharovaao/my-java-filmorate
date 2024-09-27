package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Map<Long, Film> getFilms() {
        return films;
    }

    @PostMapping
    public void addFilm(@RequestBody Film film) {
        try {
            film.setId(getNextIdForFilm());
            filmValidator(film);
            films.put(film.getId(), film);
            log.info("Добавлен фильм {}", film);
        } catch (ValidationException e) {
            log.error("Ошибка валидации при добавлении фильма: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film updatedFilm) {
        try {
            filmValidator(updatedFilm);
            if (!films.containsKey(updatedFilm.getId())) {
                log.warn("Фильм с id {} не найден, добавление нового фильма", updatedFilm.getId());
                updatedFilm.setId(getNextIdForFilm());
                films.put(updatedFilm.getId(), updatedFilm);
                log.info("Добавлен новый фильм {}", updatedFilm);
            } else {
                films.put(updatedFilm.getId(), updatedFilm);
                log.info("Обновлен фильм с id {}: {}", updatedFilm.getId(), updatedFilm);
            }
            return updatedFilm;
        } catch (ValidationException e) {
            log.error("Ошибка валидации при обновлении фильма: {}", e.getMessage());
            throw e;
        }
    }

    private void filmValidator(@RequestBody Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Имя фильма пустое");
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            log.error("Описание фильма содержит более 200 символов");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Дата релиза фильма ранее 28.12.1895");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() < 0) {
            log.error("Длительность фильма меньше 0");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
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