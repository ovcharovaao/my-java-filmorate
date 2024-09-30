package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Validated
@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        if (film == null) {
            log.error("Запрос на добавление фильма пустой");
            throw new ValidationException("Запрос на добавление фильма не может быть пустым");
        }
        filmValidator(film);
        film.setId(getNextIdForFilm());
        films.put(film.getId(), film);
        log.info("Добавлен фильм {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film updatedFilm) {
        filmValidator(updatedFilm);

        if (!films.containsKey(updatedFilm.getId())) {
            log.warn("Фильм с id {} не найден", updatedFilm.getId());
            throw new ValidationException("Фильм не найден");
        }

        films.put(updatedFilm.getId(), updatedFilm);
        log.info("Обновлен фильм с id {}: {}", updatedFilm.getId(), updatedFilm);

        return updatedFilm;
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