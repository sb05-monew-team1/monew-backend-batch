DROP TABLE IF EXISTS app.comment_likes;
DROP TABLE IF EXISTS app.notifications;
DROP TABLE IF EXISTS app.article_views;
DROP TABLE IF EXISTS app.comments;
DROP TABLE IF EXISTS app.article_interests;
DROP TABLE IF EXISTS app.articles;
DROP TABLE IF EXISTS app.interest_subscriptions;
DROP TABLE IF EXISTS app.interest_keywords;
DROP TABLE IF EXISTS app.interests;
DROP TABLE IF EXISTS app.users;

CREATE TABLE app.users
(
    id                 uuid PRIMARY KEY,
    email              varchar(320) NOT NULL UNIQUE,
    nickname           varchar(20)  NOT NULL,
    password           varchar(255) NOT NULL,
    created_at         timestamptz  NOT NULL DEFAULT now(),
    updated_at         timestamptz  NOT NULL DEFAULT now(),
    deleted_at         timestamptz,
    purge_scheduled_at timestamptz
);

CREATE UNIQUE INDEX users_nickname_unq ON app.users (nickname);

CREATE TABLE app.interests
(
    id               uuid PRIMARY KEY,
    name             varchar(50) NOT NULL,
    subscriber_count bigint      NOT NULL DEFAULT 0,
    created_at       timestamptz NOT NULL DEFAULT now(),
    updated_at       timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX interests_name_unq ON app.interests (lower(name));

CREATE TABLE app.interest_keywords
(
    id          uuid PRIMARY KEY,
    interest_id uuid         NOT NULL REFERENCES app.interests (id) ON DELETE CASCADE,
    keyword     varchar(100) NOT NULL,
    created_at  timestamptz  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX interest_keywords_interest_keyword_unq
    ON app.interest_keywords (interest_id, lower(keyword));

CREATE TABLE app.interest_subscriptions
(
    id          uuid PRIMARY KEY,
    interest_id uuid        NOT NULL REFERENCES app.interests (id) ON DELETE CASCADE,
    user_id     uuid        NOT NULL REFERENCES app.users (id) ON DELETE CASCADE,
    created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX interest_subscriptions_user_interest_unq
    ON app.interest_subscriptions (user_id, interest_id);

CREATE TABLE app.articles
(
    id            uuid PRIMARY KEY,
    source        varchar(20)  NOT NULL,
    source_url    varchar(500) NOT NULL UNIQUE,
    title         varchar(500) NOT NULL,
    publish_date  timestamptz  NOT NULL,
    summary       varchar(500),
    comment_count bigint       NOT NULL DEFAULT 0,
    view_count    bigint       NOT NULL DEFAULT 0,
    collected_at  timestamptz  NOT NULL DEFAULT now(),
    created_at    timestamptz  NOT NULL DEFAULT now(),
    updated_at    timestamptz  NOT NULL DEFAULT now(),
    deleted_at    timestamptz
);

CREATE INDEX articles_publish_date_idx ON app.articles (publish_date DESC);
CREATE INDEX articles_comment_count_idx ON app.articles (comment_count DESC);
CREATE INDEX articles_view_count_idx ON app.articles (view_count DESC);

CREATE TABLE app.article_interests
(
    article_id  uuid NOT NULL,
    interest_id uuid NOT NULL,
    PRIMARY KEY (article_id, interest_id),
    FOREIGN KEY (article_id) REFERENCES app.articles (id) ON DELETE CASCADE,
    FOREIGN KEY (interest_id) REFERENCES app.interests (id) ON DELETE CASCADE
);

CREATE TABLE app.article_views
(
    id         uuid PRIMARY KEY,
    article_id uuid        NOT NULL REFERENCES app.articles (id) ON DELETE CASCADE,
    user_id    uuid        NOT NULL REFERENCES app.users (id) ON DELETE CASCADE,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX article_views_article_user_unq
    ON app.article_views (article_id, user_id);

CREATE TABLE app.comments
(
    id         uuid PRIMARY KEY,
    user_id    uuid         NOT NULL REFERENCES app.users (id) ON DELETE CASCADE,
    article_id uuid         NOT NULL REFERENCES app.articles (id) ON DELETE CASCADE,
    content    varchar(500) NOT NULL,
    like_count bigint       NOT NULL DEFAULT 0,
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz  NOT NULL DEFAULT now(),
    deleted_at timestamptz
);

CREATE INDEX comments_article_created_idx
    ON app.comments (article_id, created_at DESC);

CREATE TABLE app.comment_likes
(
    id         uuid PRIMARY KEY,
    user_id    uuid        NOT NULL REFERENCES app.users (id) ON DELETE CASCADE,
    comment_id uuid        NOT NULL REFERENCES app.comments (id) ON DELETE CASCADE,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX comment_likes_comment_user_unq
    ON app.comment_likes (comment_id, user_id);

CREATE TABLE app.notifications
(
    id            uuid PRIMARY KEY,
    user_id       uuid         NOT NULL REFERENCES app.users (id) ON DELETE CASCADE,
    confirmed     boolean      NOT NULL DEFAULT false,
    content       varchar(500) NOT NULL,
    resource_type varchar(20)  NOT NULL,
    resource_id   uuid         NOT NULL,
    created_at    timestamptz  NOT NULL DEFAULT now(),
    updated_at    timestamptz  NOT NULL DEFAULT now()
);

CREATE INDEX notifications_user_confirmed_idx
    ON app.notifications (user_id, created_at DESC)
    WHERE confirmed = false;
