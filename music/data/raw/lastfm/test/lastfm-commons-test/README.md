# LastFM Commons Test

The module provides general test utilities, fixtures, and base test classes specific to the LastFM domain for unit and integration tests across all LastFM modules.

## Key Features

### Util Classes

- [AssertionUtils.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/common/utils/AssertionUtils.java) - contains helper methods for assertions
  - methods for verifying invocations captured by org.mockito.ArgumentCaptor
- [TestStringUtils.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/common/utils/TestStringUtils.java) - helper methods related to Strings
- [TimeTestUtils.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/common/utils/TimeTestUtils.java) - helper methods related to time

### Exposed Modules & Dependencies

- [:common:test:commons-test](../../../../../../common/test/commons-test/README.md)

## Related Documentation

- [LastFM Modules Overview](../../README.md)
- [Project Modules Index](../../../../../../docs/MODULES.md)

## TODOs
- unify names of String/Time test utils classes
