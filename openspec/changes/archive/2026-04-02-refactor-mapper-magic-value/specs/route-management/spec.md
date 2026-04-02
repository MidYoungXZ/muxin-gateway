## MODIFIED Requirements

### Requirement: Mapper layer implementation
The Mapper layer SHALL use MyBatis-Flex QueryWrapper or BaseMapper built-in methods for database operations. The use of @Select, @Delete, @Update, @Insert annotations is NOT allowed, except for special cases approved by the team.

#### Scenario: Simple query uses BaseMapper
- **WHEN** a simple single-table query is needed (e.g., find by ID, find by name)
- **THEN** the implementation SHALL use BaseMapper built-in methods (selectOneById, selectListByQuery, etc.)

#### Scenario: Complex query uses QueryWrapper
- **WHEN** a query with conditions or joins is needed
- **THEN** the implementation SHALL use QueryWrapper chain methods in the Service layer

#### Scenario: Delete operation uses BaseMapper
- **WHEN** a delete operation is needed
- **THEN** the implementation SHALL use BaseMapper deleteByMap, deleteById, or deleteBatch method

### Requirement: Strongly typed return values
Mapper methods SHALL NOT return `Map<String, Object>`. All multi-table query results SHALL be defined as dedicated VO classes.

#### Scenario: Multi-table query returns VO
- **WHEN** a Mapper method queries multiple tables
- **THEN** it SHALL return a List of a dedicated VO class (e.g., RoutePluginDetailVO, RoutePredicateDetailVO)

#### Scenario: VO class naming
- **WHEN** a VO class is created for Mapper query result
- **THEN** the class name SHALL follow the pattern `<Entity>DetailVO` or `<Entity>SimpleVO`

## ADDED Requirements

### Requirement: No magic value strings
Service layer and Provider layer SHALL NOT use hardcoded string keys to access Map values. All configuration keys SHALL be defined as constants.

#### Scenario: Use constants for config keys
- **WHEN** accessing configuration values from a Map
- **THEN** the key SHALL be referenced from a constant class (e.g., PluginConfigKeys.PATTERN)

#### Scenario: Database result uses VO
- **WHEN** processing database query results
- **THEN** the results SHALL be accessed via VO getter methods, not Map.get("key")

### Requirement: Configuration key constants
All configuration parameter keys SHALL be centralized in constant classes for maintainability.

#### Scenario: Predicate config keys
- **WHEN** building predicate arguments
- **THEN** keys like "pattern", "matchType", "methods" SHALL be defined in PredicateConfigKeys class

#### Scenario: Plugin config keys
- **WHEN** building plugin configurations
- **THEN** keys like "pathRegex", "connectTimeout" SHALL be defined in PluginConfigKeys class