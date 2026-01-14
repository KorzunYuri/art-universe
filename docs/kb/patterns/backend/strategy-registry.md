# Strategy Registry Pattern

*Type-based strategy selection with Spring AOP proxy support*

## Purpose

Select and dispatch to appropriate strategy implementation based on type (API call type, DTO class, etc.) while ensuring Spring AOP aspects (observability, transactions, security) work correctly. Combines Strategy Pattern with registry lookup and solves the proxy trap where constructor-based registration stores raw instances instead of Spring-managed proxies.

## When to Use

Use for:
- **Strategy Pattern implementations**: Select algorithm/handler based on type or identifier
- **Plugin architectures**: API response processors, API call generators, extensible handlers
- **Polymorphic dispatching**: Route to different implementations based on runtime type

Do NOT use for:
- Simple dependency injection (use `@Autowired` or constructor injection)
- Single strategy implementation (no polymorphism needed)
- Strategies that don't need runtime selection or AOP aspects
- External library classes (can't modify constructors)

## The Proxy Trap Problem

**What goes wrong**: When strategies self-register in constructor, they register the raw instance before Spring creates AOP proxies. Result: AOP aspects don't intercept method calls.

**Spring bean lifecycle**:
1. Constructor runs → calls `register(type, this)` → stores raw instance
2. Spring creates AOP proxy wrapping the instance
3. Registry returns raw instance → AOP never triggers

**Solution**: Store class references instead of instances, retrieve Spring-managed proxies via `ApplicationContext`:
1. Constructor runs → calls `register(type, this.getClass())` → stores class reference
2. Spring creates AOP proxy
3. Registry calls `context.getBean(class)` → returns proxy → AOP works

## Infrastructure

**Registry Class**:
- Spring `@Component` receiving `ApplicationContext` via constructor
- Static `ConcurrentHashMap<Key, Class<? extends Strategy>>`
- Static registration methods (callable from constructors)
- Retrieval methods using `ApplicationContext.getBean()`

**Base Strategy Class**:
- Abstract base class for all strategy implementations
- Constructor calls `Registry.register(key, this.getClass())`

## Implementation Steps

### Step 1: Create Registry Component

See: [LastfmApiCallGeneratorsRegistry](../../../../../music/data/raw/lastfm/etl/lastfm-calls-generator/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/service/LastfmApiCallGeneratorsRegistry.java)

**Key elements**:
- `@Component` annotation
- `ApplicationContext` injected via constructor, stored in static field
- `Map<Key, Class<? extends Strategy>>` stores class references
- `register(key, strategyClass)` - static method for constructor use
- `get(key)` - returns `applicationContext.getBean(strategyClass)`
- `getRegistry()` - returns map of all proxies

### Step 2: Update Base Strategy Class

See: [BaseLastfmApiCallGenerator](../../../../music/data/raw/lastfm/etl/lastfm-calls-generator/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/call/generate/BaseLastfmApiCallGenerator.java)

**Key change**: Constructor registers `this.getClass()` instead of `this`

### Step 3: Use Registry for Dispatch

See: [LastfmApiCallGenerationScheduler](../../../../music/data/raw/lastfm/etl/lastfm-calls-generator/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/call/generate/LastfmApiCallGenerationScheduler.java)

Consumer code unchanged - `getRegistry()` returns map of Spring proxies, AOP intercepts method calls.

## Examples in Codebase

### API Call Generators Registry

- **Registry**: [LastfmApiCallGeneratorsRegistry](../../../../music/data/raw/lastfm/etl/lastfm-calls-generator/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/call/generate/LastfmApiCallGeneratorsRegistry.java)
- **Base**: [BaseLastfmApiCallGenerator](../../../../music/data/raw/lastfm/etl/lastfm-calls-generator/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/call/generate/BaseLastfmApiCallGenerator.java)
- **Implementations**: 17 generators (LastfmTagTopTagsApiCallGenerator, LastfmArtistGetInfoApiCallGenerator, EntityScopedApiCallGenerator + 14 subclasses)
- **Consumer**: [LastfmApiCallGenerationScheduler](../../../../music/data/raw/lastfm/etl/lastfm-calls-generator/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/call/generate/LastfmApiCallGenerationScheduler.java)
- **AOP**: [ApiCallGeneratorObservabilityAspect](../../../../music/data/raw/lastfm/etl/lastfm-calls-generator/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/call/generate/aspect/ApiCallGeneratorObservabilityAspect.java) measures `createApiCalls()` timing

### API Response Processors Registry

- **Registry**: [LastfmApiResponseProcessorsRegistry](../../../../music/data/raw/lastfm/etl/lastfm-response-parser/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/response/process/LastfmApiResponseProcessorsRegistry.java)
- **Base**: [LastfmApiResponseProcessor](../../../../music/data/raw/lastfm/etl/lastfm-response-parser/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/response/process/LastfmApiResponseProcessor.java)
- **Implementations**: 12 processors (LastfmAlbumGetInfoResponseProcessor, LastfmArtistGetInfoResponseProcessor, LastfmTrackGetInfoResponseProcessor, etc.)
- **Consumer**: [LastfmApiResponseServiceImpl](../../../../music/data/raw/lastfm/etl/lastfm-response-parser/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/service/impl/LastfmApiResponseServiceImpl.java)
- **AOP**: [ApiResponseProcessorObservabilityAspect](../../../../music/data/raw/lastfm/etl/lastfm-response-parser/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/response/process/aspect/ApiResponseProcessorObservabilityAspect.java) measures `process()` timing

### Coded Enum Registry (Different Pattern)

[CodedRegistry](../../../../common/commons-jpa/src/main/java/yurykorzun/art/universe/common/CodedRegistry.java) stores enum values (not Spring beans), needs no ApplicationContext or proxy support. See [Coded Enum Pattern](entities/coded-enums.md).

## Testing

**Key test scenarios**:
1. Registry returns correct strategy for each key
2. Registry returns Spring proxies (verify with `AopUtils.isAopProxy()`)
3. AOP aspects intercept strategy method calls (verify metrics/traces recorded)
4. Concurrent registration/retrieval works safely

**Test example locations**: See test files in lastfm-calls-generator and lastfm-response-parser modules.

## See Also

- [Strategy Pattern (GoF)](https://en.wikipedia.org/wiki/Strategy_pattern) - Classic pattern for algorithm selection
- [Coded Enum Pattern](entities/coded-enums.md) - Simpler registry for enums (no proxies needed)
- [State Machine Pattern](state-machine.md) - Often uses coded enums with registries
- [Spring AOP Documentation](https://docs.spring.io/spring-framework/reference/core/aop.html)
