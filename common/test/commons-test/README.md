# Commons Test

The module provides basic test configuration and classes for all backend modules across the project.

## Features

- [BaseTest.java](src/main/java/yurykorzun/art/universe/common/test/archetypes/BaseTest.java)
  - Base class for all (semi-)integration tests (@WebMvcTest, @DataJpaTest, @SpringBootTest)
  - Forces usage of 'test' Spring profile
- [CommonTestConfig.java](src/main/java/yurykorzun/art/universe/common/test/config/CommonTestConfig.java)
  - provides common test objects (can be used as Spring beans)
