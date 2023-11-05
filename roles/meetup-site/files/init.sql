create database meetup;
update pg_database set encoding = pg_char_to_encoding('UTF8') where datname = 'meetup';