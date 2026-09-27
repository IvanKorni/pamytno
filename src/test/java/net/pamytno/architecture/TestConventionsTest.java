package net.pamytno.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * Соглашения для тестов (см. docs/rules/testing.md).
 */
@AnalyzeClasses(packages = "net.pamytno", importOptions = ImportOption.OnlyIncludeTests.class)
class TestConventionsTest {

    /** Каждый тест описан на русском через @DisplayName. */
    @ArchTest
    static final ArchRule testsHaveDisplayName = methods()
            .that().areAnnotatedWith(Test.class)
            .or().areAnnotatedWith(ParameterizedTest.class)
            .should().beAnnotatedWith(DisplayName.class);
}
