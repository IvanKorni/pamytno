package net.pamytno;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.modulith.Modulithic;

/**
 * Точка входа в монолит «Памятно».
 *
 * <p>Приложение разбито на DDD-модули (прямые подпакеты {@code net.pamytno}),
 * которые не зависят друг от друга. Общий код лежит в модуле {@code common},
 * а взаимодействие модулей идёт только через события из {@code common.event}.
 */
@Modulithic(systemName = "Памятно", sharedModules = "common")
@SpringBootApplication
@ConfigurationPropertiesScan
public class PamytnoApplication {

    /**
     * Запускает Spring Boot приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(PamytnoApplication.class, args);
    }
}
