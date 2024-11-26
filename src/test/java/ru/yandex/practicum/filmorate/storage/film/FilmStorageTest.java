package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FilmStorageTest {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private Film film;
    private User user;

    @Autowired
    public FilmStorageTest(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    @BeforeAll
    public void setUp() {
        // Создаем пользователя с корректными значениями
        user = new User();
        user.setEmail("testuser@example.com");
        user.setLogin("testuser");
        user.setName("Test User"); // Если имя не задано, нужно убедиться, что приложение использует логин как имя
        user.setBirthday(LocalDate.of(1990, 1, 1)); // Дата рождения в прошлом

        // Добавляем пользователя в хранилище
        userStorage.addUser(user);

        // Создаем рейтинг MPA для фильма
        MpaRating mpaRating = new MpaRating(1, "G");

        // Создаем фильм с корректными значениями
        film = new Film();
        film.setName("film1");
        film.setDescription("description1");
        film.setReleaseDate(LocalDate.of(2011, 1, 1));
        film.setDuration(150);
        film.setMpaRating(mpaRating);

        // Добавляем фильм в хранилище
        filmStorage.addFilm(film);
    }

    @Test
    @Order(1)
    void shouldCreateFilmAndGetByIdTest() {
        Optional<Film> filmOptional = Optional.ofNullable(filmStorage.getFilmById(film.getId()));
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film).hasFieldOrPropertyWithValue("id", this.film.getId());
                    assertThat(film).hasFieldOrPropertyWithValue("name", "film1");
                    assertThat(film).hasFieldOrPropertyWithValue("description", "description1");
                    assertThat(film).hasFieldOrPropertyWithValue("duration", 150);
                    assertThat(film).hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2011, 1, 1));
                    assertThat(film.getMpaRating()).hasFieldOrPropertyWithValue("id", 1L);
                    assertThat(film.getMpaRating()).hasFieldOrPropertyWithValue("name", "G");
                });
    }

    @Test
    @Order(2)
    void shouldGetAllFilmsTest() {
        Optional<Collection<Film>> filmsOptional = Optional.ofNullable(filmStorage.getAllFilms());
        assertThat(filmsOptional)
                .isPresent()
                .hasValueSatisfying(films -> {
                    assertThat(films).isNotEmpty();
                    assertThat(films).hasSize(1);
                });
    }

    @Test
    void shouldAddAndDeleteLikeTest() {
        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.deleteLike(film.getId(), user.getId());

        assertThat(filmStorage.getFilmById(film.getId()).getLikes()).doesNotContain(user.getId());
    }
}