package ru.yandex.practicum.filmorate.storage.MpaRating;

import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

public interface MpaRatingStorage {
    MpaRating getMpaRatingById(long id);

    List<MpaRating> getAllMpaRatings();
}