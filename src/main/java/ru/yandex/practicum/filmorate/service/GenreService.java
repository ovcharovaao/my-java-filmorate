package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;

    public List<Genre> getAllGenres() {
        List<Genre> genres = genreStorage.getAllGenres();
        log.info("Список жанров успешно получен. Количество: {}", genres.size());
        return genres;
    }

    public Genre getGenreById(long id) {
        Genre genre = genreStorage.getGenreById(id);
        log.info("Жанр с ID {} успешно найден: {}", id, genre.getName());
        return genre;
    }
}