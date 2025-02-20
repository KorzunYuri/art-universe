FROM postgres:14

COPY mu-db-init.sql /docker-entrypoint-initdb.d/