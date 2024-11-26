package ru.yandex.practicum.filmorate.storage.MpaRating;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MpaRatingDbStorage implements MpaRatingStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public MpaRating getMpaRatingById(long id) {
        if (id <= 0) {
            throw new ValidationException("Передан некорректный id MPA рейтинга");
        }

        MpaRating mpaRating;
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT mpa_rating_id, rating FROM mpa_ratings WHERE mpa_rating_id = ?", id);

        if (mpaRows.first()) {
            mpaRating = new MpaRating(
                    mpaRows.getLong("mpa_rating_id"),
                    mpaRows.getString("rating")
            );
        } else {
            throw new NotFoundException("Рейтинг с id " + id + " не найден");
        }

        return mpaRating;
    }


    @Override
    public List<MpaRating> getAllMpaRatings() {
        String sql = "SELECT mpa_rating_id, rating FROM mpa_ratings ORDER BY mpa_rating_id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new MpaRating(
                rs.getLong("mpa_rating_id"),
                rs.getString("rating")
        ));
    }
}