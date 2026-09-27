package net.pamytno.support;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Единственный на все тесты WireMock-сервер, подменяющий внешние HTTP-системы (YouTube).
 * Тесты различают свои заглушки уникальными идентификаторами в URL.
 */
public final class TestWireMock {

    private static final WireMockServer SERVER = new WireMockServer(options().dynamicPort());

    /**
     * Запрещает создание экземпляров.
     */
    private TestWireMock() {
    }

    /**
     * Возвращает запущенный сервер, при необходимости запуская его.
     *
     * @return запущенный WireMock-сервер
     */
    public static synchronized WireMockServer started() {
        if (!SERVER.isRunning()) {
            SERVER.start();
        }
        return SERVER;
    }
}
