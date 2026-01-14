# Commons Test Web

The module provides web/MVC test utilities and base classes for testing REST controllers and web endpoints across all modules in the project.

## Features

### 1. Base Mvc Test Class

[BaseMvcTest.java](src/main/java/yurykorzun/art/universe/common/test/archetypes/BaseMvcTest.java) is designed as the base class for MVC tests in Spring Boot.

It adds [CommonGlobalExceptionHandler.java](../../commons-web/src/main/java/yurykorzun/art/universe/common/web/exception/CommonGlobalExceptionHandler.java) to Spring context to comply with exception handling standard used in the project

**When to use**: when you need to test a Spring controller with MVC-layer. 

**How to use**:
- extend your test class from BaseMvcTest.java
- annotate your test class with @WebMvcTest(MyController.class)

Examples of usage:
- [Lastfm controller example](../../../music/data/raw/lastfm/etl/lastfm-etl-rest-api/src/test/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/controller/LastfmApiResponseControllerMvcTest.java)
- [Example with an intermediate class](../../../music/data/master/src/test/java/yurykorzun/art/universe/music/data/master/controller/AlbumControllerMvcTest.java)
