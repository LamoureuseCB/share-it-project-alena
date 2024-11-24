create table users
(
    id    serial primary key,
    name  varchar(122) not null,
    email varchar(122) not null
);
create table items
(
    id           serial primary key,
    name         varchar(122) not null,
    description  varchar(122) not null,
    is_available boolean      not null,
    owner_id     int references users (id) not null
);

create table bookings
(
    id         serial primary key,
    start_date timestamp without time zone not null,
    end_date   timestamp without time zone not null,
    item_id    int references items (id) not null,
    booker_id  int references users (id) not null,
    status     varchar       not null

);
create table comments
(
    id         serial primary key,
    description  varchar(122) not null,
    item_id    int references items (id) not null,
    author_id  int references users (id) not null,
    created timestamp without time zone not null


);
