# Music Quiz Module

The Music Quiz module is a Spring Boot web application that generates sets of tracks for music quizzes using a pipeline-based system.

For the list of available endpoints see [API Reference](docs/api.md)

For the list of pipeline steps see [Pipeline Steps Overview](docs/pipeline/overview.md)



## Data Flow

- user creates a **Game**
- user configures the sequence of steps for generating set of tracks - **Pipeline**
- user performs a **Generation** - a deep-copy of the pipeline is created and the results of generation are persisted (**GenerationTrack**)
- user approves a **Generation** - tracks from this generation are considered 'played'

There are no 'users' currently in the application: this is a planned feature, so the data are not owned by anyone.
When the users are implemented, ownership of existing data will move to the admin user.


## Data Model

- [Artist.java](src/main/java/yurykorzun/art/universe/music/quiz/entity/Artist.java), [Track.java](src/main/java/yurykorzun/art/universe/music/quiz/entity/Track.java)
  - Basically are just IDs to [master entities](../data/master/README.md#entities)
  - Serve as the source data for tracks generation
  - In the future, Artists and Tracks will be bound to user's 'datasets'
- [Game.java](src/main/java/yurykorzun/art/universe/music/quiz/entity/Game.java) - A single quiz game.
- [Pipeline.java](src/main/java/yurykorzun/art/universe/music/quiz/entity/Pipeline.java) - A sequence of steps defining the process of generating (filtering) a set of tracks for a game.
- [StepType.java](src/main/java/yurykorzun/art/universe/music/quiz/entity/StepType.java) - Defines a type of operation performed on a tracks dataset.
- [Step.java](src/main/java/yurykorzun/art/universe/music/quiz/entity/Step.java) - A single bit of track sets generation logic.
  - When executed, outputs a datasource of unified format optionally extended by extra fields
  - Execution is performed by StepProcessor (watch below)
  - Step of any type can be input for step of any other type
  - Stores preview (brief summary of expected results based on preflight execution)
  - Stores reference to the last execution (stats), if happened once
- [StepProcessor.java](src/main/java/yurykorzun/art/universe/music/quiz/service/step/process/StepProcessor.java) - Base interface for step processors, implementing Strategy pattern.
  - Takes a dataset of a unified format as the input
  - May additionally expect configuration depending on step type.
  - Outputs dataset of same format, optionally extended with additional fields.
- [PipelineStep.java](src/main/java/yurykorzun/art/universe/music/quiz/entity/PipelineStep.java) - Step in context of pipeline. `ord` field defines the order of steps in the pipeline.
- [Generation.java](src/main/java/yurykorzun/art/universe/music/quiz/entity/Generation.java) - Attempt of generating a pack of tracks for a game. 
  - Game can have multiple generations
  - Multiple generations can be approved - after that the tracks that got into the generation are considered 'played' and it may affect the chance of they appearing in the future generations.
  - Each generation stores immutable pipeline configuration snapshot captured before its execution (think of it as of a deep copy)
- [PipelineRun.java](src/main/java/yurykorzun/art/universe/music/quiz/entity/PipelineRun.java), [StepRun.java](src/main/java/yurykorzun/art/universe/music/quiz/entity/StepRun.java) - Execution history
- [GenerationTrack.java](src/main/java/yurykorzun/art/universe/music/quiz/entity/GenerationTrack.java) - Track that passed the steps pipeline, the result of generation.

Look for the remaining entities in the [entities package](src/main/java/yurykorzun/art/universe/music/quiz/entity).


## Patterns Used

This module follows these project-wide patterns:

- [Coded Enums](../../docs/kb/patterns/backend/entities/coded-enums.md) - StepType, ExecutionStatus, GenerationStatus with integer codes
- [Base Entity](../../docs/kb/patterns/backend/entities/base-entity.md) - All entities extend BaseEntity with @SuperBuilder
- [Environment Profiles](../../docs/kb/patterns/backend/configuration/environment-profiles.md) - dev, local, prod profiles
- [API Conventions](../../docs/kb/patterns/backend/api/conventions.md) - RESTful endpoint structure


## Build & Deployment

See: [Gradle Commands Guide](../../docs/kb/guides/gradle-commands.md) for standard build/test commands

See: [Docker Deployment Guide](../../env/docker/README.md) for deployment procedures


## Related Documentation

### Other Modules
- [Music Data Master](../../docs/kb/modules/mu-data-master/README.md): Provides master artist/track data via views
- [Music UI](../../docs/kb/modules/mu-ui/README.md): Frontend for quiz management
- [Commons Web](../../common/commons-web/README.md): Web utilities and CORS configuration
- [Commons JPA](../../common/commons-jpa/README.md): BaseEntity and JPA utilities

### Project Guides
- [Architecture Overview](../../docs/kb/guides/architecture-overview.md)
- [Development Workflow](../../docs/kb/guides/development-workflow.md)
- [Gradle Commands](../../docs/kb/guides/gradle-commands.md)
