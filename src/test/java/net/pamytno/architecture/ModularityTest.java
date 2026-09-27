package net.pamytno.architecture;

import net.pamytno.PamytnoApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Проверка структуры модулей средствами Spring Modulith.
 */
class ModularityTest {

    private final ApplicationModules modules = ApplicationModules.of(PamytnoApplication.class);

    @Test
    @DisplayName("Модули не имеют циклов и зависят только от разрешённых модулей")
    void modules_respectDeclaredDependencies() {
        modules.verify();
    }

    @Test
    @DisplayName("Документация модулей генерируется в build/spring-modulith-docs")
    void modules_documentationIsGenerated() {
        new Documenter(modules).writeDocumentation();
    }
}
