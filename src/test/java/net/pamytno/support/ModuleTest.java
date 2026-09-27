package net.pamytno.support;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Модульный тест: поднимает только модуль, в пакете которого лежит тест, и общее ядро {@code common}.
 * Другие модули в контекст не попадают — так проверяется, что модуль самодостаточен.
 * БД — общий контейнер PostgreSQL, HTTP — MockMvc, события записывает {@link CapturedEvents}.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag("module")
@ApplicationModuleTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
@Import(CapturedEvents.class)
public @interface ModuleTest {
}
