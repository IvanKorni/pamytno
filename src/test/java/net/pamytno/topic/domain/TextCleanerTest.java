package net.pamytno.topic.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты {@link TextCleaner}: очистка техническая, смысл текста не меняется.
 */
class TextCleanerTest {

    @Test
    @DisplayName("Пустые строки удаляются, пробелы по краям строк обрезаются")
    void clean_removesEmptyLinesAndTrims() {
        assertThat(TextCleaner.clean("  Первая строка  \n\n   \nВторая строка\n"))
                .isEqualTo("Первая строка\nВторая строка");
    }

    @Test
    @DisplayName("Повторяющиеся пробелы и табуляции внутри строки схлопываются в один пробел")
    void clean_collapsesHorizontalWhitespace() {
        assertThat(TextCleaner.clean("JVM\t\tвыполняет    байткод")).isEqualTo("JVM выполняет байткод");
    }

    @Test
    @DisplayName("Переводы строк Windows и старого Mac приводятся к \\n")
    void clean_normalizesLineBreaks() {
        assertThat(TextCleaner.clean("a\r\nb\rc")).isEqualTo("a\nb\nc");
    }

    @Test
    @DisplayName("Служебные и невидимые символы удаляются, буквы и знаки остаются")
    void clean_removesControlAndInvisibleCharacters() {
        assertThat(TextCleaner.clean("﻿пере­нос\u0007 и​ слово — «кавычки»!"))
                .isEqualTo("перенос и слово — «кавычки»!");
    }

    @Test
    @DisplayName("Текст из одних пробелов становится пустым")
    void clean_returnsEmpty_forBlankText() {
        assertThat(TextCleaner.clean(" \n\t \n")).isEmpty();
    }
}
