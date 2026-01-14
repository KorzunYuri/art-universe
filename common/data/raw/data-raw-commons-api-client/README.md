# Data Raw Commons API Client

The module provides HTTP client utilities and base classes for calling external APIs in ETL pipelines across the project.

## Key Components

- [ApiClient.java](src/main/java/yurykorzun/art/universe/data/raw/common/integration/ApiClient.java)
  - Standard interface that all API client implementations must follow
  - Uses [ApiCall.java](../data-raw-commons-jpa/src/main/java/yurykorzun/art/universe/data/raw/common/etl/entity/ApiCall.java) as the source of metadata for request creation. 
- [BaseHttpApiClient.java](src/main/java/yurykorzun/art/universe/data/raw/common/integration/BaseHttpApiClient.java)
  - Abstract base class for HTTP-based API clients
  - Utilizes `org.springframework.web.client.RestClient`
