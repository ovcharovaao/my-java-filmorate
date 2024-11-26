package ru.yandex.practicum.filmorate.storage.MpaRating;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ImportResource;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ImportResource
@AutoConfigureTestDatabase
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MpaRatingStorageTest {
    private final MpaRatingStorage mpaRatingStorage;

    @Autowired
    public MpaRatingStorageTest(@Qualifier("mpaRatingDbStorage") MpaRatingStorage mpaRatingStorage) {
        this.mpaRatingStorage = mpaRatingStorage;
    }

    @Test
    @Order(1)
    void shouldGetAllMpaRatingsTest() {
        List<MpaRating> mpaRatings = mpaRatingStorage.getAllMpaRatings();
        assertThat(mpaRatings)
                .isNotEmpty()
                .hasSize(5);
    }

    @Test
    @Order(2)
    void shouldGetMpaRatingByIdTest() {
        MpaRating mpaRating = mpaRatingStorage.getMpaRatingById(1);
        assertThat(mpaRating)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "G");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenMpaRatingNotFoundTest() {
        NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () -> {
            mpaRatingStorage.getMpaRatingById(100);
        });

        assertThat(exception.getMessage()).isEqualTo("Рейтинг с id 100 не найден");
    }

    @Test
    void shouldThrowValidationExceptionWhenInvalidIdTest() {
        ValidationException exception = Assertions.assertThrows(ValidationException.class, () -> {
            mpaRatingStorage.getMpaRatingById(-1);
        });

        assertThat(exception.getMessage()).isEqualTo("Передан некорректный id MPA рейтинга");
    }
}