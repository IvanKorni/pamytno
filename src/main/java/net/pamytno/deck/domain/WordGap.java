package net.pamytno.deck.domain;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Пропуск на месте выражения в предложении с подсказкой — первыми двумя буквами той формы, которая стоит
 * в предложении: «I had to ma_____ quickly».
 */
public final class WordGap {

    /** Пропуск после букв подсказки. */
    public static final String GAP = "_____";

    private static final int HINT_LETTERS = 2;
    private static final Pattern MODEL_GAP = Pattern.compile("_{3,}");
    private static final String NOT_LETTER_BEFORE = "(?<![\\p{L}\\p{N}])";
    private static final String NOT_LETTER_AFTER = "(?![\\p{L}\\p{N}])";

    /**
     * Запрещает создание экземпляров.
     */
    private WordGap() {
    }

    /**
     * Заменяет выражение пропуском с подсказкой. Ищет форму из предложения, затем словарную форму целым словом
     * без учёта регистра; если модель сама оставила пропуск, дополняет его подсказкой.
     *
     * @param example предложение
     * @param answer  форма выражения в предложении; может быть пустой
     * @param word    выражение в словарной форме
     * @return предложение с пропуском или пустое значение, если выражения в предложении нет
     */
    public static Optional<String> apply(String example, String answer, String word) {
        return replaceForm(example, answer)
                .or(() -> replaceForm(example, word))
                .or(() -> replaceModelGap(example, answer.isEmpty() ? word : answer));
    }

    /**
     * Первые буквы формы и пропуск; у короткой формы остаётся хотя бы одна скрытая буква.
     *
     * @param form выражение в том виде, в каком оно стоит в предложении
     * @return подсказка с пропуском, например {@code ma_____}
     */
    public static String hinted(String form) {
        var letters = Math.max(0, Math.min(HINT_LETTERS, form.length() - 1));
        return form.substring(0, letters) + GAP;
    }

    /**
     * Заменяет первое вхождение формы целым словом.
     *
     * @param example предложение
     * @param form    искомая форма
     * @return предложение с пропуском или пустое значение, если формы нет
     */
    private static Optional<String> replaceForm(String example, String form) {
        if (form.isEmpty()) {
            return Optional.empty();
        }
        var matcher = Pattern.compile(NOT_LETTER_BEFORE + Pattern.quote(form) + NOT_LETTER_AFTER,
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(example);
        return matcher.find() ? Optional.of(example.substring(0, matcher.start()) + hinted(matcher.group())
                + example.substring(matcher.end())) : Optional.empty();
    }

    /**
     * Дополняет подсказкой пропуск, который оставила модель.
     *
     * @param example предложение с пропуском из подчёркиваний
     * @param form    форма, по которой строится подсказка
     * @return предложение с пропуском или пустое значение, если пропуска нет
     */
    private static Optional<String> replaceModelGap(String example, String form) {
        var matcher = MODEL_GAP.matcher(example);
        return matcher.find() ? Optional.of(matcher.replaceFirst(Matcher.quoteReplacement(hinted(form))))
                : Optional.empty();
    }
}
