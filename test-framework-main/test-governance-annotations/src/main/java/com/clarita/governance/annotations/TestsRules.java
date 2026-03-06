package com.clarita.governance.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation for multiple {@link TestsRule} annotations on the same method.
 *
 * <p>This annotation is automatically used by the compiler when multiple
 * {@link TestsRule} annotations are applied to the same test method.</p>
 *
 * <h2>Usage</h2>
 * <p>You typically don't use this annotation directly. Instead, use multiple
 * {@link TestsRule} annotations:</p>
 *
 * <pre>{@code
 * @Test
 * @TestsRule(ruleId = "BR-PVM-15")
 * @TestsRule(ruleId = "BR-PVM-16")
 * void testOvertimeWorkflow() {
 *     // Tests both rules
 * }
 * }</pre>
 *
 * @see TestsRule
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TestsRules {

    /**
     * The array of {@link TestsRule} annotations.
     *
     * @return array of TestsRule annotations
     */
    TestsRule[] value();
}
