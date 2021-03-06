package com.buschmais.jqassistant.core.report.api;

import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

/**
 * Provides utility functionality for creating reports.
 */
public final class ReportHelper {

    private static String CONSTRAINT_VIOLATION_HEADER
         = "--[ Constraint Violation ]-----------------------------------------";

    private static String CONCEPT_FAILED_HEADER
         = "--[ Concept Application Failure ]----------------------------------";

    private static String FOOTER
         = "-------------------------------------------------------------------";

    private Logger logger;

    /**
     * Constructor.
     *
     * @param log
     *            The logger to use for logging messages.
     */
    public ReportHelper(Logger log) {
        this.logger = log;
    }

    /**
     * Verifies the concept results returned by the
     * {@link com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter} .
     * <p>
     * An error message is logged for each concept which did not return a result
     * (i.e. has not been applied).
     * </p>
     *
     * @param inMemoryReportWriter
     *            The
     *            {@link com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter}
     *            .
     */
    public int verifyConceptResults(Severity violationSeverity, InMemoryReportWriter inMemoryReportWriter) {
        Collection<Result<Concept>> conceptResults = inMemoryReportWriter.getConceptResults().values();
        int violations = 0;

        for (Result<Concept> conceptResult : conceptResults) {
            if (Result.Status.FAILURE.equals(conceptResult.getStatus())) {
                Concept concept = conceptResult.getRule();
                logger.error(CONCEPT_FAILED_HEADER);
                logger.error("Concept: " + concept.getId());
                logger.error("Severity: " + concept.getSeverity().getInfo(conceptResult.getSeverity()));
                String description = concept.getDescription();

                StringTokenizer tokenizer = new StringTokenizer(description, "\n");

                while (tokenizer.hasMoreTokens()) {
                    logger.error(tokenizer.nextToken().replaceAll("(\\r|\\n|\\t)", ""));
                }

                logger.error(FOOTER);
                logger.error(System.lineSeparator());

                // severity level check
                if (conceptResult.getSeverity().getLevel() <= violationSeverity.getLevel()) {
                    violations++;
                }
            }
        }
        return violations;
    }

    /**
     * Verifies the constraint violations returned by the
     * {@link InMemoryReportWriter}. Returns the count of constraints having
     * severity higher than the provided severity level.
     *
     * @param violationSeverity
     *            severity level to use for verification
     * @param inMemoryReportWriter
     *            The {@link InMemoryReportWriter}.
     */
    public int verifyConstraintResults(Severity violationSeverity, InMemoryReportWriter inMemoryReportWriter) {
        Collection<Result<Constraint>> constraintResults = inMemoryReportWriter.getConstraintResults().values();
        int violations = 0;
        for (Result<Constraint> constraintResult : constraintResults) {
            if (Result.Status.FAILURE.equals(constraintResult.getStatus())) {
                Constraint constraint = constraintResult.getRule();

                logger.error(CONSTRAINT_VIOLATION_HEADER);
                logger.error("Constraint: " + constraint.getId());
                logger.error("Severity: " + constraint.getSeverity().getInfo(constraintResult.getSeverity()));
                String description = constraint.getDescription();

                StringTokenizer tokenizer = new StringTokenizer(description, "\n");

                while (tokenizer.hasMoreTokens()) {
                    logger.error(tokenizer.nextToken().replaceAll("(\\r|\\n|\\t)", ""));
                }

                for (Map<String, Object> columns : constraintResult.getRows()) {
                    StringBuilder message = new StringBuilder();
                    for (Map.Entry<String, Object> entry : columns.entrySet()) {
                        if (message.length() > 0) {
                            message.append(", ");
                        }
                        message.append(entry.getKey());
                        message.append('=');
                        String stringValue = getStringValue(entry.getValue());
                        message.append(stringValue);
                    }
                    logger.error("  " + message.toString());
                }

                logger.error(FOOTER);
                logger.error(System.lineSeparator());

                // severity level check
                if (constraintResult.getSeverity().getLevel() <= violationSeverity.getLevel()) {
                    violations++;
                }
            }
        }
        return violations;
    }

    /**
     * Converts a value to its string representation.
     *
     * @param value
     *            The value.
     * @return The string representation
     */
    public static String getStringValue(Object value) {
        if (value != null) {
            if (value instanceof Descriptor) {
                Descriptor descriptor = (Descriptor) value;
                LanguageElement elementValue = LanguageHelper.getLanguageElement(descriptor);
                if (elementValue != null) {
                    SourceProvider sourceProvider = elementValue.getSourceProvider();
                    return sourceProvider.getName(descriptor);
                }
            } else if (value instanceof Iterable) {
                StringBuffer sb = new StringBuffer();
                for (Object o : ((Iterable) value)) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(getStringValue(o));
                }
                return "[" + sb.toString() + "]";
            } else if (value instanceof Map) {
                StringBuffer sb = new StringBuffer();
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(entry.getKey());
                    sb.append(":");
                    sb.append(getStringValue(entry.getValue()));
                }
                return "{" + sb.toString() + "}";
            }
            return value.toString();
        }
        return null;
    }
}
