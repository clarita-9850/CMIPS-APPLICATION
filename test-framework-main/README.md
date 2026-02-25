# Test Governance Framework

A standalone Java library that enables organizations to enforce, track, and report on test coverage for critical business rules. Designed for compliance with federal requirements (CMS SMC, FISMA, NIST 800-53, FedRAMP, HIPAA).

## Features

- **Annotation-Based**: Embed test requirements directly in source code
- **Automated Validation**: No manual verification of test coverage
- **Audit-Ready Reports**: Generate compliance reports in JSON, HTML formats
- **CI/CD Integration**: Works with GitHub Actions, Jenkins, GitLab CI
- **Zero Runtime Impact**: Annotations are metadata only

## Quick Start

### 1. Add Dependencies

**For production code (annotations only - zero dependencies):**
```xml
<dependency>
    <groupId>com.clarita</groupId>
    <artifactId>test-governance-annotations</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**For Spring Boot applications (recommended):**
```xml
<dependency>
    <groupId>com.clarita</groupId>
    <artifactId>test-governance-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

**For non-Spring applications (JUnit 5):**
```xml
<dependency>
    <groupId>com.clarita</groupId>
    <artifactId>test-governance-junit5</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

### 2. Annotate Business Rules

```java
@BusinessRule(
    ruleId = "BR-PVM-15",
    description = "Calculate overtime violation penalties per IHSS regulations",
    owner = "provider-team",
    severity = Severity.CRITICAL,
    categories = {TestCategory.UNIT, TestCategory.COMPLIANCE},
    compliance = {ComplianceFramework.CMS_SMC, ComplianceFramework.STATE_CDSS}
)
public OvertimeViolationResult processOvertimeViolation(Provider provider, int count) {
    // Business logic
}
```

### 3. Link Tests to Rules

```java
@Test
@TestsRule(
    ruleId = "BR-PVM-15",
    scenarios = {"first_violation_warning", "penalty_calculation"},
    author = "mythreya",
    category = TestCategory.COMPLIANCE
)
void testOvertimeViolation_firstViolation_returnsWarning() {
    // Test implementation
}
```

### 4. Configure (Spring Boot)

```yaml
# application-governance.yml
governance:
  enabled: true
  scan:
    base-packages:
      - com.mycompany.myapp.service
      - com.mycompany.myapp.controller
  execution:
    phase: PR
  failure:
    on-critical: true
    on-high: true
  report:
    output-dir: target/governance-reports
    formats:
      - JSON
      - HTML
```

### 5. Run Validation

**Spring Boot:**
```java
@SpringBootTest
@ValidateGovernance(basePackages = {"com.mycompany.myapp"})
class ApplicationGovernanceTest {
    @Test
    void contextLoads() {}
}
```

**Maven Plugin:**
```bash
mvn test-governance:validate
```

## Modules

| Module | Description | Size |
|--------|-------------|------|
| `test-governance-annotations` | Annotations and enums (no dependencies) | ~20KB |
| `test-governance-core` | Validation engine, scanning, reporting | ~150KB |
| `test-governance-spring-boot-starter` | Spring Boot auto-configuration | ~50KB |
| `test-governance-junit5` | JUnit 5 extension | ~30KB |
| `test-governance-maven-plugin` | Maven build lifecycle integration | ~40KB |

## Compliance Frameworks

| Framework | Code | Description |
|-----------|------|-------------|
| CMS SMC | `CMS_SMC` | CMS Streamlined Modular Certification |
| FISMA | `FISMA` | Federal Information Security Management Act |
| NIST 800-53 | `NIST_800_53` | NIST Security and Privacy Controls |
| FedRAMP | `FEDRAMP` | Federal Risk and Authorization Management Program |
| HIPAA | `HIPAA` | Health Insurance Portability and Accountability Act |
| DISA STIG | `DISA_STIG` | DoD Security Technical Implementation Guides |

## Severity Levels

| Level | Build Behavior | Use For |
|-------|----------------|---------|
| `CRITICAL` | Fails immediately on commit | Financial, security, compliance-critical logic |
| `HIGH` | Fails on PR merge | Core business logic |
| `MEDIUM` | Warning only | Standard features |
| `LOW` | Nightly builds only | Edge cases, nice-to-haves |

## Generated Reports

The framework generates audit-ready reports:

- **JSON**: Machine-readable for CI/CD integration
- **HTML**: Human-readable with visual dashboard
- **Compliance Summary**: Coverage by framework
- **Ownership Summary**: Coverage by team

## Requirements

- Java 17+
- Spring Boot 3.x (for starter)
- JUnit 5.9+ (for extension)
- Maven 3.8+

## Building

```bash
# Build all modules
mvn clean install

# Skip tests
mvn clean install -DskipTests
```

## License

Apache License 2.0

## Documentation

See [TEST_GOVERNANCE_FRAMEWORK_DESIGN.md](docs/TEST_GOVERNANCE_FRAMEWORK_DESIGN.md) for the complete design document.
