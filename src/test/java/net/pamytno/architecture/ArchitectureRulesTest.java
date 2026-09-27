package net.pamytno.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import net.pamytno.common.error.ApplicationException;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

/**
 * Правила слоёв и кода внутри модулей (см. docs/rules/architecture.md).
 */
@AnalyzeClasses(packages = "net.pamytno", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureRulesTest {

    /** Контроллеры работают через сервисы, а не через репозитории. */
    @ArchTest
    static final ArchRule controllersDoNotUseRepositories = noClasses()
            .that().resideInAPackage("..rest..")
            .should().dependOnClassesThat().resideInAPackage("..repository..")
            .allowEmptyShould(true);

    /** Домен не знает о сервисах, REST, обработчиках событий и внешних системах. */
    @ArchTest
    static final ArchRule domainIsIndependent = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..service..", "..rest..", "..listener..", "..integration..", "org.springframework.web..")
            .allowEmptyShould(true);

    /** Сервисы не зависят от REST-слоя и его DTO. */
    @ArchTest
    static final ArchRule servicesDoNotDependOnRest = noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat().resideInAPackage("..rest..")
            .allowEmptyShould(true);

    /** Текущее время — только через внедрённый Clock. */
    @ArchTest
    static final ArchRule timeOnlyThroughClock = noClasses()
            .should().callMethod(Instant.class, "now")
            .orShould().callMethod(LocalDate.class, "now")
            .orShould().callMethod(LocalDateTime.class, "now")
            .orShould().callMethod(OffsetDateTime.class, "now")
            .orShould().callMethod(ZonedDateTime.class, "now")
            .because("время подменяется в тестах через Clock");

    /** Контроллеры называются *RestControllerV1 и лежат в пакете rest. */
    @ArchTest
    static final ArchRule controllersNaming = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().haveSimpleNameEndingWith("RestControllerV1")
            .andShould().resideInAPackage("..rest")
            .allowEmptyShould(true);

    /** JPA-сущности лежат в пакете domain. */
    @ArchTest
    static final ArchRule entitiesInDomain = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..domain..")
            .allowEmptyShould(true);

    /** Исключения модулей наследуют категории из common.error. */
    @ArchTest
    static final ArchRule exceptionsExtendApplicationException = classes()
            .that().resideInAPackage("..exception..")
            .should().beAssignableTo(ApplicationException.class)
            .allowEmptyShould(true);

    /** Интеграционные события — неизменяемые record. */
    @ArchTest
    static final ArchRule integrationEventsAreRecords = classes()
            .that().resideInAPackage("net.pamytno.common.event..")
            .and().doNotHaveSimpleName("package-info")
            .should().beAssignableTo(Record.class)
            .allowEmptyShould(true);

    /** Обработчики событий лежат в пакете listener. */
    @ArchTest
    static final ArchRule listenersInListenerPackage = methods()
            .that().areAnnotatedWith(TransactionalEventListener.class)
            .or().areAnnotatedWith(EventListener.class)
            .or().areMetaAnnotatedWith(TransactionalEventListener.class)
            .should().beDeclaredInClassesThat().resideInAPackage("..listener..")
            .allowEmptyShould(true);

    /** Внедрение зависимостей только через конструктор. */
    @ArchTest
    static final ArchRule noFieldInjection = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

    /** Логи только через SLF4J. */
    @ArchTest
    static final ArchRule noJavaUtilLogging = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

    /** Никакого System.out/err. */
    @ArchTest
    static final ArchRule noStandardStreams = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

    /** Бросаем осмысленные исключения, а не Exception/RuntimeException. */
    @ArchTest
    static final ArchRule noGenericExceptions = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
}
