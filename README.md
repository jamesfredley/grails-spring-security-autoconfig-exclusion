# Spring Security AutoConfiguration Exclusions Should Be Automatic

**Grails Version**: 7.0.7  
**Spring Security Plugin**: 7.0.x  
**Spring Boot**: 3.5.10

## Feature Description

The `grails-spring-security` plugin [README](https://github.com/apache/grails-spring-security/blob/7.0.x/README.md) documents that **every** Grails 7 user must manually add 7 `spring.autoconfigure.exclude` entries to their `application.yml`:

```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration
      - org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration
      - org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
      - org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
      - org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration
      - org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration
      - org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
```

This is **unnecessary boilerplate** that the plugin should handle automatically. Spring Boot provides the `AutoConfigurationImportFilter` SPI (stable since 1.5.0, used in Spring Boot 3.x) which allows libraries to filter out conflicting auto-configurations before they are even loaded — no user configuration needed.

## Steps to Reproduce

1. Clone this repository
2. Run `./gradlew bootRun`
3. Visit `http://localhost:8080/bugDemo/index`
4. Observe the JSON response showing which auto-configuration classes are on the classpath and which beans are registered

**Note**: This app intentionally does NOT include the manual exclusions, to show the current state of the classpath without them.

## Why This Matters

1. **Every Grails 7 + spring-security user** must copy 7 YAML lines from the README — easy to miss, hard to debug
2. **New users** hitting startup errors or unexpected behavior have no clear error message pointing them to the exclusions
3. **The exclusions are always needed** — they are not conditional or environment-specific
4. **Other libraries handle this automatically** — e.g., Redis OM Spring, TCC Transaction use `AutoConfigurationImportFilter`

## Proposed Fix

Add `SecurityAutoConfigurationExcluder` implementing `AutoConfigurationImportFilter` to the plugin, registered via `META-INF/spring.factories`. This runs before auto-configuration bytecode is loaded, cannot be overridden by user config, and has no property merging issues ([Spring Boot #41669](https://github.com/spring-projects/spring-boot/issues/41669)).

## Environment

- **Grails**: 7.0.7
- **Spring Boot**: 3.5.10
- **Groovy**: 4.0.30
- **JDK**: 17+

## Project Structure

| File | Purpose |
|------|---------|
| `grails-app/domain/com/example/User.groovy` | Spring Security user domain class |
| `grails-app/domain/com/example/Role.groovy` | Spring Security role domain class |
| `grails-app/domain/com/example/UserRole.groovy` | Join table for users and roles |
| `grails-app/controllers/com/example/BugDemoController.groovy` | Endpoint showing autoconfiguration and bean status |
| `grails-app/conf/application.yml` | Spring Security config **without** manual exclusions |
| `grails-app/controllers/com/example/UrlMappings.groovy` | Maps `/bugDemo/index` |
