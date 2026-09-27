package net.pamytno.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Часы для тестов: идут как системные, но их можно перевести вперёд — например, чтобы проверить,
 * что карточка снова к повторению через день. После теста сдвиг сбрасывается.
 */
public final class TestClock extends Clock {

    private final Clock base = Clock.systemUTC();
    private volatile Duration offset = Duration.ZERO;

    /**
     * Переводит часы вперёд.
     *
     * @param duration на сколько
     */
    public void advance(Duration duration) {
        offset = offset.plus(duration);
    }

    /**
     * Возвращает часы к системному времени.
     */
    public void reset() {
        offset = Duration.ZERO;
    }

    /**
     * Часовой пояс часов.
     *
     * @return UTC
     */
    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    /**
     * Часы в другом поясе не поддерживаются — тестам достаточно UTC.
     *
     * @param zone пояс
     * @return эти же часы
     */
    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    /**
     * Текущий момент со сдвигом.
     *
     * @return системное время плюс сдвиг
     */
    @Override
    public Instant instant() {
        return base.instant().plus(offset);
    }
}
