package ru.yandex.practicum.filmorate.storage.genre;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GenreStorageTest {

    private final GenreStorage genreStorage;

    @Autowired
    public GenreStorageTest(@Qualifier("genreDbStorage") GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    @Test
    @Order(1)
    void shouldGetAllGenresTest() {
        List<Genre> genres = genreStorage.getAllGenres();
        assertThat(genres)
                .isNotEmpty()
                .hasSize(6);
    }

    @Test
    @Order(2)
    void shouldGetGenreByIdTest() {
        Genre genre = genreStorage.getGenreById(1);
        assertThat(genre)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "Комедия");
    }

    @Test
    @Order(3)
    void shouldThrowNotFoundExceptionForInvalidIdTest() {
        long invalidId = 10;
        assertThatThrownBy(() -> genreStorage.getGenreById(invalidId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Жанр с id " + invalidId + " не найден.");
    }
}