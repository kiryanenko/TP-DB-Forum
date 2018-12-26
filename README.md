# TP-DB-Forum
Тестовое задание для реализации проекта “Форум” на курсе по базам данных в Технопарке Mail.ru

Суть задания заключается в реализации API к базе данных проекта «Форумы» по документации к этому API.

Таким образом, на входе:

 * документация к API;

На выходе:

 * репозиторий, содержащий все необходимое для разворачивания сервиса в Docker-контейнере.

## Документация к API
Документация к API предоставлена в виде спецификации [OpenAPI](https://ru.wikipedia.org/wiki/OpenAPI_%28%D1%81%D0%BF%D0%B5%D1%86%D0%B8%D1%84%D0%B8%D0%BA%D0%B0%D1%86%D0%B8%D1%8F%29): swagger.yml

Документацию можно читать как собственно в файле swagger.yml, так и через Swagger UI (там же есть возможность поиграться с запросами): https://tech-db-forum.bozaro.ru/

## Требования к проекту
Проект должен включать в себя все необходимое для разворачивания сервиса в Docker-контейнере.

При этом:

 * файл для сборки Docker-контейнера должен называться Dockerfile и располагаться в корне репозитория;
 * реализуемое API должно быть доступно на 5000-ом порту по протоколу http;
 * допускается использовать любой язык программирования;
 * крайне не рекомендуется использовать ORM.

Контейнер будет собираться из запускаться командами вида:
```
docker build -t a.navrotskiy https://github.com/bozaro/tech-db-forum-server.git
docker run -p 5000:5000 --name a.navrotskiy -t a.navrotskiy
```

В качестве отправной точки можно посмотреть на примеры реализации более простого API на различных языках программирования: https://github.com/bozaro/tech-db-hello/

## Функциональное тестирование
Корректность API будет проверяться при помощи автоматического функционального тестирования.

Методика тестирования:

 * собирается Docker-контейнер из репозитория;
 * запускается Docker-контейнер;
 * запускается скрипт на Go, который будет проводить тестирование;
 * останавливается Docker-контейнер.

Скомпилированные программы для тестирования можно скачать по ссылкам:

 * [linux_386.zip](/linux_386.zip)
 * [linux_amd64.zip](/linux_amd64.zip)
 * [windows_386.zip](/windows_386.zip)
 * [windows_amd64.zip](/windows_amd64.zip)

Для локальной сборки Go-скрипта достаточно выполнить команду:
```
go get -u -v github.com/bozaro/tech-db-forum
go build github.com/bozaro/tech-db-forum
```
После этого в текущем каталоге будет создан исполняемый файл `tech-db-forum`.

### Запуск функционального тестирования

Для запуска функционального тестирования нужно выполнить команду вида:
```
./tech-db-forum func -u http://localhost:5000/api -r report.html
```

Поддерживаются следующие параметры:

Параметр                              | Описание
---                                   | ---
-h, --help                            | Вывод списка поддерживаемых параметров
-u, --url[=http://localhost:5000/api] | Указание базовой URL тестируемого приложения
-k, --keep                            | Продолжить тестирование после первого упавшего теста
-t, --tests[=.*]                      | Маска запускаемых тестов (регулярное выражение)
-r, --report[=report.html]            | Имя файла для детального отчета о функциональном тестировании


## Нагрузочное тестирование

### Особенности проведения тестов:

- Docker-контейнеру установлено ограничение по количеству используемой оперативной памяти в 1Gb;
- Виртуальная машина с Docker-ом имеет лимит в 1.5Gb и установлена на отдельный HDD;
- На сборку контейнера отводится не более 15-ти минут;
- На заполнение базы отводится не более 15-ти минут;
- Нагрузка идёт 10 раз в течение 1-ой минуты в 8 потоков. Учитывается лучший результат.

### Технические характеристики стенда:

- HDD: WDC WD10PURZ-85U
- CPU: Intel® Core™ i5-7400

### Результаты

RPS состоавило **1583 запросов/сек**


--------------------------------------------------------------------------------------------------------

Лабораторная работа №1 «Создание и заполнение БД»
=================================================

# Выбор технологий

В рамках лабораторных работ была реализована база данных **"Форум"**.

Для выполнения лабораторной работы была выбрана СУБД **PostgreSQL**. 
Данная СУБД является одной из наиболее часто использующихся при разработке. 
Это была одна из первых разработанных систем управления базами данных, 
поэтому в настоящее время она хорошо развита, и позволяет пользователям управлять как структурированными, 
так и неструктурированными данными.

Для реализации API использовался фреймворк: **Java Spring Framework**. 
Это универсальный фреймворк с открытым исходным кодом для Java-платформы. 

# Проектирование структуры БД

Для форума были выявлено 5 сущностей (Рисунок 1):
- Пользователь.
- Форум.
- Тема, в которой публикуются посты (сообщения).
- Пост (сообщение). Посты могут быть ответами на другие посты.
- Голос за тему (нравится / не нравится). Голосовать можно только один раз за одну тему.

![Рисунок 1 — Инфологическая модель сущность-связь](/docs/infological_model.svg)

Рисунок 1 — Инфологическая модель сущность-связь

В БД имеется 5 связей один-ко-многим:
1. Созданные пользователем форумы.
2. Созданные пользователем темы.
3. Темы в форуме.
4. Посты в теме.
5. Дочерние посты

А также 2 связи многие-ко-многим:
1. Пользователи форума
2. Голос пользователя за тему.

Была спроектирована схема данных, которая представлена на рисунке 2.

![Рисунок 2 — Схема данных](/docs/DB_scheme_before.jpg)

Рисунок 2 — Схема данных

# Заполнение БД тестовыми данными

Скрипт на языке Go заполняет БД тестовыми данными.
- 1000 записей в таблице пользователи,
- 100 записей в таблице форумы,
- 10 тыс. записей в таблице ветки,
- 100 тыс. записей в таблице голоса,
- 1,5 млн. записей в таблице посты.

![Рисунок 3 — Заполнение БД](/docs/fill_data.jpg)

Рисунок 3 — Заполнение БД

Для ускорения заполнения БД в конфигурационный файл PostgreSQL были добавление записи (Листинг 1).

Листинг 1 — Файл "/etc/postgresql/9.5/main/postgresql.conf"
```
synchronous_commit = off
fsync = off
```
Параметр `synchronous_commit` включает / выключает синхронную запись в лог-файлы после каждой транзакции. 
Включение синхронной записи защищает от возможной потери данных. 
Но, накладывает ограничение на пропускную способность сервера. 

Параметр `fsync` отвечает за сброс данных из кэша на диск при завершении транзакций. 
Если установить в этом параметре значение off, то данные не будут записываться на дисковые накопители 
сразу после завершения операций. Это может существенно повысить скорость операций `insert` и `update`, 
но есть риск повредить базу, если произойдет сбой (неожиданное отключение питания, сбой ОС, 
сбой дисковой подсистемы).

Таким образом заполнение БД занимает 10 минут.

**Вывод:**
в ходе данной работы мы ознакомились с интерфейсом базы данных PostgreSQL, 
спроектировали, создали и заполнили базу данных согласно заданию.


--------------------------------------------------------------------------------------------------------

Лабораторная работа №2 «Выборка данных и работа с представлениями»
===================================================================

# Реализация функционала

В рамках лабораторных работ была реализована база данных **"Форум"**.

Для БД форума было реализовано REST API прилодение c помощью фреймворка **Java Spring Framework**.
API включает в себя 17 запросов. Данное API было задокументировано в спецификации **Swagger** (Рисунок 1).
Swagger — это фреймворк и спецификация для определения REST APIs в формате, 
дружественном к пользователю и компьютеру (JSON или YAML). 

![Рисунок 1 — Реализованное API](/docs/swagger.jpg)

Рисунок 1 — Реализованное API

Для реализованного API было проведено тестирование (Рисунок 2), включающее в себя 258 автоматических тестов. 
По окончании тестирования генерируется отчет (Рисунок 3).

![Рисунок 2 — Вывод фунционального тестирования](/docs/func_test.jpg)

Рисунок 2 — Вывод фунционального тестирования

![Рисунок 3 — Сгенерированный отчет по тестированию](/docs/tests_report.jpg)

Рисунок 3 — Сгенерированный отчет по тестированию


# Денормализация данных

Для оптимизации производительности была прозведена денормолизация данных. 
Таким образом, итоговая схема данных привидена на рисунке 4. 

Денормализация — намеренное приведение структуры базы данных в состояние, 
не соответствующее критериям нормализации, обычно проводимое с целью ускорения операций 
чтения из базы за счет добавления избыточных данных.

![Рисунок 4 — Схема данных](/docs/DB_scheme.jpg)

Рисунок 4 — Схема данных

Входе денормализации были добавлены счетчики количества тем `threads`, постов `posts` и счетчик голосования `votes`.
Также в таблицы был добавлен внешний ключ `author` типа `TEXT` ссылающиеся на никнейм пользователя.
В таблице `post` добавилось поле `path`, являющееся массивом идентификатов постов, 
который указывает полный путь до корневого сообщения (подробнее см. ниже).


# Реализованные запросы

Рассмотрим некоторые реализованные запросы к БД:

## Дерево постов с пагинацией по родительским

Запрос на древовидный вывод постов внутри ветки `:thread_id`.
Сообщения выводятся отсортированные по дате создания.
Пагинация осуществляется по родительским сообщениям `parent`, 
на странице `:limit` родительских комментов и все комментарии прикрепленные к ним, 
в древовидном отображение.

Листинг 1 — Рекурсивный запрос на древовидный вывод постов
```sql
WITH RECURSIVE recursetree (id, author, author_id, created, forum, is_edited, message, parent, thread_id, path) AS (
  SELECT P.*, array_append('{}'::int[], P.id) path
  FROM post P JOIN person U ON P.author_id = U.id
  WHERE parent isnull AND P.thread_id = :thread_id
  UNION ALL
  SELECT P.*, array_append('{}'::int[], P.id) path
  FROM post P JOIN person U ON P.author_id = U.id JOIN recursetree RT ON RT.id = P.parent
)
SELECT *
FROM recursetree
ORDER BY path
LIMIT :limit
```

Реализация с помощью рекурсивного запроса оказалась достаточно медленной. 
Поэтому была применена денормолизация — было дабавлено поле `path`, 
являющееся массивом идентификатов постов, который указывает полный путь до корневого сообщения.
Теперь отпала необходимость рекурсивного обхода сообщений, 
что существенно уменьшило время выполнения запроса (Листинг 2) до 740 мс. 

Листинг 2 — Запрос на древовидный вывод постов
```sql
WITH roots AS (
  SELECT id FROM post
  WHERE thread_id = :thread_id AND parent IS NULL AND 
    root > (SELECT root FROM post WHERE id = :since)
  ORDER BY id
  LIMIT :limit
)
SELECT P.* 
FROM post P JOIN roots ON roots.id = P.root
ORDER BY P.root, P.path
```

![Рисунок 4 — Результат запроса на древовидный вывод постов](/docs/query0.jpg)

Рисунок 4 — Результат запроса на древовидный вывод постов


Для оптимизации запроса были добавлены соответствующие индексы (Листинг 3), 
что также сильно умельшило время выполнения запроса до **6 мс**. 
Ниже на рисунках показаны выводы `EXPLAIN ANALYZE` до и после добавления индексов.

Листинг 2 — Добавленные индексы
```sql
CREATE INDEX idx_post_roots ON post (thread_id, path, id) WHERE parent IS NULL;
CREATE INDEX idx_post_roots_desc ON post (thread_id, path, id DESC) WHERE parent IS NULL;
CREATE INDEX idx_post_root ON post (root, path);
CREATE INDEX idx_post_root_desc ON post (root, path DESC);
```

![Рисунок 5 — Вывод `EXPLAIN ANALYZE` до добавления индексов](/docs/explain_before.jpg)

Рисунок 5 — Вывод `EXPLAIN ANALYZE` до добавления индексов

![Рисунок 6 — Вывод `EXPLAIN ANALYZE` после добавления индексов](/docs/explain.jpg)

Рисунок 6 — Вывод `EXPLAIN ANALYZE` **после** добавления индексов


## Пользователи форума

Выбрать `:limit` пользователей форума `:forum_id`, начиная с пользовтеля `:since`
и отсортировать по убыванию.

Листинг 3 — Запрос на пользователей форума
```sql
SELECT U.* 
FROM person U JOIN forum_person FP ON U.id = FP.person_id
WHERE FP.forum_id = :forum_id AND LOWER(U.nickname) < LOWER(:since)
ORDER BY LOWER(U.nickname) DESC
LIMIT :limit
```

![Рисунок 7 — Результат запроса на пользователей форума](/docs/query1.jpg)


## Изменение пользователя

Запрос на изменение пользователя `:nickname`:

Листинг 4 — Запрос на изменение пользователя
```sql
UPDATE person SET fullname = :fullname, email = :email, about = :about
WHERE LOWER(nickname) = LOWER(:nickname) 
RETURNING *
```
![Рисунок 8 — Результат запроса на изменение пользователя](/docs/query2.jpg)

Рисунок 8 — Результат запроса на изменение пользователя

## Проверка наличия родительских постов

Посты к теме `:thread` добавляются сразу пачкой. И перед добавлением постов,
являющиесями отвтетами, необходимо проверить наличие родительского поста.
Эту проверку выполняет следующий запрос:

Листинг 5 — Запрос на проверку наличия родительских постов
```sql
SELECT BodyTable.parent
FROM (
  SELECT 1 AS parent UNION
  SELECT 2 AS parent UNION
  SELECT 42 AS parent UNION
  SELECT 123 AS parent UNION
  SELECT 404 AS parent
) AS BodyTable LEFT JOIN post P ON BodyTable.parent = P.id "
WHERE P.id IS NULL OR P.thread_id != :thread 
LIMIT 1
```

Данный запрос использует временную таблицу для хранения `id` родителей,
добавляемых постов. Если запрос не вернёт ничего, то проверка пройдена успешно,
иначе нет.


## Получение полной информации о сообщении

Запрос на получение полной информации о сообщении, включая связанные объекты.

Листинг 6 — Запрос на получение полной информации о сообщении
```sql
SELECT *
FROM post P 
  JOIN person U ON P.author_id = U.id 
  JOIN thread T ON P.thread_id = T.id 
  JOIN forum F ON T.forum_id = F.id "
  JOIN person T_author ON T.author_id = T_author.id 
  JOIN person F_user ON F.person_id = F_user.id
WHERE P.id = :id
```


## Изменение сообщения на форуме

Запрос на изменение сообщения на форуме. Если сообщение поменяло текст, 
то оно должно получить отметку `is_edited`.

Листинг 7 — Запрос на изменение сообщения на форуме
```sql
UPDATE post SET message = :message, is_edited = is_edited OR message != :message
WHERE id = :id RETURNING *, (
  SELECT P.nickname 
  FROM person P 
  WHERE P.id = author_id
) author, (
  SELECT F.slug 
  FROM thread T JOIN forum F ON T.forum_id = F.id 
  WHERE T.id = thread_id
) forum
```


## Очистка БД

Безвозвратное удаление всей пользовательской информации из базы данных.

Листинг 8 — Запрос на изменение сообщения на форуме
```sql
TRUNCATE person, forum, post, thread, vote, forum_person
```


## Удалить голос

Запрос на удаление голоса пользователя `:person` для темы `:thread`.

Листинг 9 — Запрос на удаление голоса
```sql
DELETE FROM vote
WHERE vote.person_id = :person AND vote.thread_id = :thread
```


## Сумма голосов для темы

Запрос на выдачу суммы голосов для темы `:thread`.

Листинг 10 — Запрос на выдачу суммы голосов
```sql
SELECT T.*, sum(V.voice)
FROM thread T JOIN vote V on T.id = v.thread_id
WHERE T.id = :thread
GROUP BY T.id
```


## Количество пользователей в форуме

Запрос на выдачу количества пользователей внутри форума `:forum`.

Листинг 11 — Запрос на количество пользователей в форуме
```sql
SELECT F.*, count(U.person_id)
FROM forum F JOIN forum_person U on F.id = U.forum_id
WHERE F.id = :forum
GROUP BY F.id
```


## Количество веток для каждого форума

Запрос на выдачу количества веток для каждого форума `:forum`.

Листинг 11 — Запрос на количество веток в каждом форуме
```sql
SELECT T.forum_id, count(T.id)
FROM thread T
GROUP BY T.forum_id
ORDER BY T.forum_id
```

