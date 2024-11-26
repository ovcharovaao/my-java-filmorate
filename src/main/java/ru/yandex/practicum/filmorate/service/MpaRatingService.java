package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaRating.MpaRatingDbStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaRatingService {
    private final MpaRatingDbStorage mpaRatingDbStorage;

     public MpaRating getMpaRatingById(long id) {
        MpaRating mpaRating = mpaRatingDbStorage.getMpaRatingById(id);
        log.info("Рейтинг с ID {} успешно найден: {}", id, mpaRating.getName());
        return mpaRating;
    }

    public List<MpaRating> getAllMpaRatings() {
        log.info("Запрос на получение всех MPA рейтингов");
        return mpaRatingDbStorage.getAllMpaRatings();
    }
}