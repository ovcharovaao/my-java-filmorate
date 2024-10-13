package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {
    @Autowired
    private FilmController filmController;

    @Autowired
    private Validator validator;

    private Film film;

    @BeforeEach
    void setUp() {
        film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2004, 1, 1));
        film.setDuration(120);
    }

    @Test
    @DisplayName("Создание фильма без названия")
    void addFilmWithEmptyName() {
        film.setName(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Название фильма не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Создание фильма с превышающим допустимую длину описанием")
    void addFilmWithIncorrectDescription() {
        film.setDescription("a".repeat(201));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Длина описания должна составлять от 1 до 200 символов", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Создание фильма с датой релиза ранее 28.12.1895")
    void addFilmWithIncorrectReleaseDate() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    @DisplayName("Создание фильма с отрицательной продолжительностью")
    void addFilmWithIncorrectDuration() {
        film.setDuration(-1);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Продолжительность фильма должна быть положительным числом", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Создание фильма с правильными данными")
    void addFilmWithCorrectData() {
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
        Film createdFilm = filmController.addFilm(film);
        assertNotNull(createdFilm.getId());
        assertEquals(film.getName(), createdFilm.getName());
    }

    @Test
    @DisplayName("Удаление фильма")
    void deleteFilm() {
        Film createdFilm = filmController.addFilm(film);
        filmController.deleteFilm(createdFilm.getId());
        assertThrows(NotFoundException.class, () -> filmController.getFilm(createdFilm.getId()));
    }

    @Test
    @DisplayName("Получение популярных фильмов")
    void getPopularFilms() {
        filmController.addFilm(film);
        List<Film> popularFilms = filmController.getPopularFilms(5);
        assertEquals(1, popularFilms.size());
        assertEquals(film.getName(), popularFilms.get(0).getName());
    }
}