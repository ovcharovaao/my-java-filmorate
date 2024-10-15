# java-filmorate
Template repository for Filmorate project.


### Проект базы данных
![Проект Базы данных](https://github.com/ovcharovaao/java-filmorate/blob/main/%D0%91%D0%94.svg)

### Описание:
1. Таблица users — хранит информацию о пользователях - id, электронную почту, логины, имена и даты рождения.
2. Таблица films — содержит информацию о фильмах - id, названия, описания, даты релиза, продолжительность и рейтинг MPA.
3. Таблица likes — хранит лайки пользователей к фильмам.
4. Таблица film_genres — реализует связь многие-ко-многим между фильмами и жанрами.
5. Таблица genres — хранит список жанров фильмов.
6. Таблица friendships —  хранит id пользователей и статус их дружбы (подтверждённая или неподтверждённая).
7. Таблица mpa_ratings — содержит перечень рейтингов фильмов.

### Примеры запросов в БД:
<details>
  <summary>Получить пользователя с id=1</summary>

```sql
    SELECT *
    FROM users
    WHERE user_id = 1;
```

</details><details>
  <summary>Получить все фильмы, которые не имеют описания</summary>

```sql
    SELECT *
    FROM films
    WHERE description IS NULL OR description = '';
```

</details><details>
  <summary>Получить фильмы с рейтингом MPA PG-13</summary>

```sql
    SELECT f.name, f.release_date
    FROM films f
    JOIN mpa_ratings m ON f.mpa_rating_id = m.mpa_rating_id
    WHERE m.rating = 'PG-13';
```

</details>
