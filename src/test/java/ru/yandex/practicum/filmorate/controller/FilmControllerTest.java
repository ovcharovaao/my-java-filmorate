package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private final Film film = new Film();
    FilmController fc = new FilmController();

    @BeforeEach
    void setFilm() {
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2004, 1, 1));
        film.setDuration(120);
    }

    @Test
    void addFilmWithEmptyName() {
        film.setName(null);

        assertThrows(ValidationException.class, () -> fc.addFilm(film));
        assertTrue(fc.getFilms().isEmpty());
    }

    @Test
    void addFilmWithIncorrectDescription() {
        film.setDescription("A very very long description, a very looooooooooooooooooooooooooong description of " +
                "a very interesting and loooooooooooooooooooooooooooooong movie, this description has more than " +
                "200 characters for this test");

        assertThrows(ValidationException.class, () -> fc.addFilm(film));
        assertTrue(fc.getFilms().isEmpty());
    }

    @Test
    void addFilmWithIncorrectReleaseDate() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ValidationException.class, () -> fc.addFilm(film));
        assertTrue(fc.getFilms().isEmpty());
    }

    @Test
    void addFilmWithIncorrectDuration() {
        film.setDuration(-1);

        assertThrows(ValidationException.class, () -> fc.addFilm(film));
        assertTrue(fc.getFilms().isEmpty());
    }

    @Test
    void getFilms() {
        fc.addFilm(film);
        List<Film> films = fc.getFilms();

        assertEquals(1, films.size());
    }
}