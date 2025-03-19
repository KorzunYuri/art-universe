# Music universe: LastFm data collector

A Spring Boot application for collecting data about tags, artists, albums and tracks from last.fm public API
for further processing.

## Database initialization

Application uses _postgres_ as database. Required steps for DB initialization can be seen in
_src/main/resources/docker/compose/initdb/templates/init.sql.template_ - 
template for initial script used for initialization of _postgres_ container in local Docker environment:

- create database named _music_universe_ 
- under _music_universe_ db, create schema named _mu_raw_lastfm_
- create user _mu_raw_lastfm_dm_ (_dm_ stands for _data manager_)

## Application parameters

Application requires the following parameters to run:

| Parameter name            | Description                                       | Is mandatory | Default value |
|---------------------------|---------------------------------------------------|--------------|---------------|
| MURAW_LASTFM_DB_PASSWORD  | Password of the main user of Lastfm schema        | Yes          |               |
| MURAW_LASTFM_DB_HOST      | Database host                                     | No           | localhost     |
| MURAW_LASTFM_DB_PORT      | Database port                                     | No           | 5432          |
| MURAW_LASTFM_API_KEY      | API key obtained from Lastfm for public API calls | Yes          |               |
| MURAW_LASTFM_PORT         | Port to run the app's web server on               | No           | 8080          |
| SPRING_PROFILES_ACTIVE    | Spring active profile                             | No           |               |

Database host and port default values are currently pointing to a local database instance run via Docker.

## Build

Application tests are using TestContainers whenever DB is involved.
Thus, to build the app you need Docker engine to be up and running.

Now, from project root, run
```
./gradlew clean build
```

## Run 

### Locally

The most convenient way to run application environment locally is via Docker Compose.
Docker Compose for Spring Boot is configured for profile 'local' - with this profile active,
when you execute Gradle _bootRun_ task, it will launch containers from _src/main/resources/docker/compose/docker-compose.yml_.
For convenience, you can keep env variables in _.env_ file used by Docker Compose 
and read them to your terminal session whenever you need to run the app. 
Env variables not used by Docker Compose will do no harm there.

Here is how to do it:

For the first time, create _.env_ file under _src/main/resources/docker/compose/local/_ with the following variables at least:
```
MURAW_LASTFM_DB_PASSWORD='your_password'
MURAW_LASTFM_API_KEY='your_lastfm_api_key'
SPRING_PROFILES_ACTIVE='local'
```

Make sure the file is ignored by VCS!

Then, any time you open the new terminal window, you can read variables from _.env_ file.
Open the terminal in project's root and execute:

```
get-content music-universe/music-data-raw-lastfm/src/main/resources/docker/compose/local/.env | foreach {
  $name, $value = $_.split('=')
  set-content env:\$name $value
}
```

Then, to launch the application, execute:
```
./gradlew :music-universe:music-data-raw-lastfm:bootRun
```

### Locally, but 'prod' :)

At the moment 'production' environment is run via Docker Compose as well. 
* open terminal at _src/main/resources/docker/compose/prod_
* run `docker compose down --rmi 'all'` to remove previous setup
* run `docker compose up` (with optional ` -d` flag to run application in detached mode)
