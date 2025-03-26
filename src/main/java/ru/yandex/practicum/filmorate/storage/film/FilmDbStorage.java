package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film addFilm(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Дата релиза фильма ранее 28.12.1895");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        String checkMpaQuery = "SELECT COUNT(*) FROM mpa_ratings WHERE mpa_rating_id = ?";
        int mpaCount = jdbcTemplate.queryForObject(checkMpaQuery, Integer.class, film.getMpaRating().getId());

        if (mpaCount == 0) {
            log.error("Неверный MPA рейтинг для фильма {}", film.getName());
            throw new ValidationException("Неверный MPA рейтинг.");
        }

        String sqlQuery = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setLong(5, film.getMpaRating().getId());
            return stmt;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Genre> requestGenres = new ArrayList<>(film.getGenres());
            for (Genre genre : requestGenres) {
                String checkGenreQuery = "SELECT COUNT(*) FROM genres WHERE id = ?";
                int genreCount = jdbcTemplate.queryForObject(checkGenreQuery, Integer.class, genre.getId());
                if (genreCount == 0) {
                    log.error("Жанр с id {} не найден.", genre.getId());
                    throw new ValidationException("Жанр с id " + genre.getId() + " не найден.");
                }
            }

            String sqlGenreQuery = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            List<Object[]> batchParams = new ArrayList<>();

            for (Genre genre : requestGenres) {
                batchParams.add(new Object[]{film.getId(), genre.getId()});
            }

            jdbcTemplate.batchUpdate(sqlGenreQuery, batchParams);
        } else {
            film.setGenres(new HashSet<>());
        }

        setMpaToFilm(film);
        setGenresToFilm(film);

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (getFilmById(film.getId()) != null) {
            String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                    "duration = ?, mpa_rating_id = ? WHERE film_id = ?";
            jdbcTemplate.update(
                    sql,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpaRating().getId(),
                    film.getId()
            );
            return film;
        } else {
            throw new NotFoundException("Фильм не найден");
        }
    }

    @Override
    public Film getFilmById(long id) {
        String sqlQuery = "SELECT f.*, m.rating AS mpa_rating_name FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.mpa_rating_id " +
                "WHERE f.film_id = ?";

        try {
            Film film = jdbcTemplate.queryForObject(sqlQuery, new FilmMapper(), id);
            film.setGenres(getGenresForFilm(id));
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм не найден");
        }
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT f.*, m.rating AS mpa_rating_name FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_rating_id = m.mpa_rating_id";
        return jdbcTemplate.query(sql, new FilmMapper());
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        if (count <= 0) {
            throw new ValidationException("Количество популярных фильмов должно быть положительным числом");
        }

        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                "m.rating AS mpa_rating_name, COUNT(l.user_id) AS likes_count " +
                "FROM films f " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "JOIN mpa_ratings m ON f.mpa_rating_id = m.mpa_rating_id " +
                "GROUP BY f.film_id, m.mpa_rating_id, m.rating " +
                "ORDER BY likes_count DESC, f.film_id ASC " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("film_id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            MpaRating mpa = new MpaRating();
            mpa.setId(rs.getLong("mpa_rating_id"));
            mpa.setName(rs.getString("mpa_rating_name"));
            film.setMpaRating(mpa);

            return film;
        }, count);
    }

    @Override
    public void deleteFilmById(long filmId) {
        String sql = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    @Override
    public void addLike(long filmId, long userId) {
        String checkSql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        int count = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId, userId);

        if (count == 0) {
            String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, filmId, userId);
        } else {
            log.warn("Лайк уже существует для фильма {} от пользователя {}", filmId, userId);
        }
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    private static class FilmMapper implements RowMapper<Film> {
        @Override
        public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
            Film film = new Film();
            film.setId(rs.getLong("film_id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));
            film.setMpaRating(new MpaRating(rs.getLong("mpa_rating_id"), rs.getString("mpa_rating_name")));
            return film;
        }
    }

    private Set<Genre> getGenresForFilm(long filmId) {
        String sql = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ?";

        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getLong("id"), rs.getString("name")), filmId);

        return new HashSet<>(genres);
    }

    private void setMpaToFilm(Film film) {
        String sqlMpaQuery = "SELECT " +
                "mpa_rating_id, " +
                "rating " +
                "FROM mpa_ratings " +
                "WHERE mpa_rating_id = ?";

        film.setMpaRating(
                jdbcTemplate.queryForObject(
                        sqlMpaQuery,
                        (rs, rowNum) -> new MpaRating(
                                rs.getLong("mpa_rating_id"),
                                rs.getString("rating")
                        ),
                        film.getMpaRating().getId()
                )
        );
    }

    private void setGenresToFilm(Film film) {
        String sqlGenresQuery = "SELECT " +
                "g.id AS genre_id, " +
                "g.name AS genre_name " +
                "FROM film_genres fg " +
                "JOIN genres g ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ?";

        film.setGenres(new LinkedHashSet<>(
                jdbcTemplate.query(
                        sqlGenresQuery,
                        (rs, rowNum) -> new Genre(
                                rs.getLong("genre_id"),
                                rs.getString("genre_name")
                        ),
                        film.getId()
                )
        ));
    }
}