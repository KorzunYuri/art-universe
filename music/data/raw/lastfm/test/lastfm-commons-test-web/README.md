# LastFM Commons Test Web

The module provides web/REST controller test utilities specific to LastFM REST APIs.

## Key Components

### Test Configuration

**Purpose**: Providing common Spring configuration and beans.

**Class**: [TestExceptionHandlerConfig.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/common/config/TestExceptionHandlerConfig.java)

Automatically adds [CommonGlobalExceptionHandler.java](../../../../../../common/commons-web/src/main/java/yurykorzun/art/universe/common/exception/CommonGlobalExceptionHandler.java), a @RestControllerAdvice used across the project, to the Spring context loaded during integration tests.

### Exposed Modules & Dependencies

- [:common:commons-web](../../../../../../common/commons-web/README.md) - common Web configuration and beans for Spring
- [:common:test:commons-test-web](../../../../../../common/test/commons-test-web/README.md) - common Web configuration and beans for Spring for tests
- [:music:data:raw:lastfm:test:lastfm-commons-test](../lastfm-commons-test/README.md) - common Lastfm tests utilities and features 

## Related Documentation

- [LastFM Modules Overview](../../README.md)
- [LastFM REST API](../../../../../../docs/kb/modules/lastfm/lastfm-rest-api/README.md)
- [LastFM ETL REST API](../../../../../../docs/kb/modules/lastfm/lastfm-etl-rest-api/README.md)
- [Project Modules Index](../../../../../../docs/MODULES.md)
