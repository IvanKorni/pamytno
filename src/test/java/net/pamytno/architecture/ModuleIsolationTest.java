package net.pamytno.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Изоляция DDD-модулей: модули не ходят друг в друга ни в основном коде, ни в тестах.
 * Единственная разрешённая зависимость — общее ядро {@code common}.
 */
@AnalyzeClasses(packages = "net.pamytno")
class ModuleIsolationTest {

    private static final String COMMON = "net.pamytno.common..";
    private static final String TEST_SUPPORT = "net.pamytno.support..";
    private static final String CROSS_MODULE_TESTS = "net.pamytno.integration..";
    private static final String ARCHITECTURE_TESTS = "net.pamytno.architecture..";

    /** Модули не зависят друг от друга: ни импортов, ни вызовов, ни общих классов. */
    @ArchTest
    static final ArchRule modulesDoNotDependOnEachOther = slices()
            .matching("net.pamytno.(*)..")
            .namingSlices("модуль $1")
            .should().notDependOnEachOther()
            .ignoreDependency(alwaysTrue(), resideInAnyPackage(COMMON, TEST_SUPPORT))
            .ignoreDependency(resideInAnyPackage(CROSS_MODULE_TESTS, ARCHITECTURE_TESTS), alwaysTrue())
            .because("модули общаются только событиями из common.event");

    /** Общее ядро не знает о модулях. */
    @ArchTest
    static final ArchRule commonDoesNotDependOnModules = noClasses()
            .that().resideInAPackage(COMMON)
            .should().dependOnClassesThat(resideInAPackage("net.pamytno..")
                    .and(not(resideInAnyPackage(COMMON, TEST_SUPPORT))))
            .because("common — общее ядро, от него зависят модули, а не наоборот");
}
