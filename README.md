# SCLX final record source set

This archive contains:

- final plain Java records for:
  - `BankStatementRecord`
  - `BudgetRecord`
  - `BankingItemRecord`
- Jackson deserializers for each record
- `fromSclx(...)` mapper classes that map from `JsonNode`

## Package layout

- `nonprofitbookkeeping.model.impex`
- `nonprofitbookkeeping.importer.sclx.jackson`
- `nonprofitbookkeeping.importer.sclx.mapping`

## Dependencies

You will need Jackson with JSR-310 support in your project, for example:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.17.2</version>
</dependency>

<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
    <version>2.17.2</version>
</dependency>
```

## Typical use

```java
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new JavaTimeModule());
mapper.registerModule(new SclxImpexJacksonModule());
```

Then either:

- deserialize directly into the final record types using the custom deserializers, or
- call the mapper classes explicitly from a `JsonNode`.

## Notes

- The mapper/deserializer pair is intentionally simple and focused on the current SCLX schema.
- Records normalize `extensions` and lists to immutable copies.
- Validation is performed in the record canonical constructors for required fields and conditional banking-item requirements.
