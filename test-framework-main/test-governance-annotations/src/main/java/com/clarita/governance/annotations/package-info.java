/**
 * Test Governance Framework Annotations.
 *
 * <p>This package contains all annotations and enums for the Test Governance Framework.
 * These annotations enable declarative test coverage enforcement for critical business rules.</p>
 *
 * <h2>Core Annotations</h2>
 * <table border="1">
 *   <tr><th>Annotation</th><th>Target</th><th>Purpose</th></tr>
 *   <tr><td>{@link com.clarita.governance.annotations.BusinessRule}</td><td>Method, Type</td><td>Marks code requiring test coverage</td></tr>
 *   <tr><td>{@link com.clarita.governance.annotations.TestsRule}</td><td>Method</td><td>Links tests to business rules</td></tr>
 *   <tr><td>{@link com.clarita.governance.annotations.Compliance}</td><td>Type</td><td>Class-level compliance metadata</td></tr>
 *   <tr><td>{@link com.clarita.governance.annotations.GovernanceIgnore}</td><td>Method, Type, Package</td><td>Excludes from scanning</td></tr>
 * </table>
 *
 * <h2>Enums</h2>
 * <table border="1">
 *   <tr><th>Enum</th><th>Purpose</th></tr>
 *   <tr><td>{@link com.clarita.governance.annotations.Severity}</td><td>Rule criticality (CRITICAL, HIGH, MEDIUM, LOW)</td></tr>
 *   <tr><td>{@link com.clarita.governance.annotations.TestCategory}</td><td>Test types (UNIT, INTEGRATION, COMPLIANCE, etc.)</td></tr>
 *   <tr><td>{@link com.clarita.governance.annotations.ExecutionPhase}</td><td>When validation runs (COMMIT, PR, NIGHTLY, RELEASE)</td></tr>
 *   <tr><td>{@link com.clarita.governance.annotations.ComplianceFramework}</td><td>Regulatory frameworks (CMS_SMC, FISMA, HIPAA, etc.)</td></tr>
 * </table>
 *
 * <h2>Zero Dependencies</h2>
 * <p>This module has no external dependencies and can be safely added to production code.
 * The annotations are metadata only and have no runtime behavior.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * <!-- Add to production code -->
 * <dependency>
 *     <groupId>com.clarita</groupId>
 *     <artifactId>test-governance-annotations</artifactId>
 *     <version>1.0.0</version>
 * </dependency>
 * }</pre>
 *
 * <h2>Federal Compliance</h2>
 * <p>These annotations support requirements from:</p>
 * <ul>
 *   <li>NIST SP 800-53 Rev. 5 - Security and Privacy Controls</li>
 *   <li>CMS Streamlined Modular Certification</li>
 *   <li>FISMA - Federal Information Security Management Act</li>
 *   <li>FedRAMP - Federal Risk and Authorization Management Program</li>
 * </ul>
 *
 * @since 1.0.0
 * @see com.clarita.governance.annotations.BusinessRule
 * @see com.clarita.governance.annotations.TestsRule
 */
package com.clarita.governance.annotations;
