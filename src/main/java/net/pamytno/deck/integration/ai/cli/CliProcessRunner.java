package net.pamytno.deck.integration.ai.cli;

import lombok.extern.slf4j.Slf4j;
import net.pamytno.deck.config.AiProperties;
import net.pamytno.deck.integration.ai.AiGenerationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Запускает Claude Code или Codex без shell и возвращает ответ модели. */
@Slf4j
@Component
public class CliProcessRunner {

    private static final String CLAUDE = "claude";
    private static final String CODEX = "codex";
    private static final int MAX_ERROR_OUTPUT_LENGTH = 500;

    /** Выполняет выбранный CLI, передавая prompt через stdin. */
    public String run(AiProperties.Cli cli, String prompt) throws IOException, InterruptedException {
        var commandLine = commandLine(cli);
        var outputFile = createOutputFile(commandLine);
        try {
            var process = new ProcessBuilder(withOutputFile(commandLine, outputFile))
                    .redirectErrorStream(true).start();
            writePrompt(process, prompt);
            var processOutput = await(process, cli.timeoutSeconds());
            checkExitCode(process, processOutput);
            var output = outputFile == null ? processOutput : Files.readString(outputFile);
            log.debug("AI CLI [{}] вернул ответ длиной {} символов", cli.command(), output.length());
            return output;
        } finally {
            if (outputFile != null) {
                Files.deleteIfExists(outputFile);
            }
        }
    }

    /** Собирает аргументы без запуска shell. */
    private static List<String> commandLine(AiProperties.Cli cli) {
        var command = cli.command().strip().toLowerCase();
        var arguments = new ArrayList<String>();
        switch (command) {
            case CLAUDE -> arguments.addAll(List.of("-p", "--output-format", "text", "--no-session-persistence"));
            case CODEX -> arguments.addAll(List.of("exec", "--ephemeral", "--skip-git-repo-check", "--sandbox",
                    "read-only", "--color", "never", "-"));
            default -> throw new AiGenerationException("Неподдерживаемый AI_CLI: " + cli.command()
                    + ". Допустимы claude и codex");
        }
        addModel(arguments, cli.model());
        var commandLine = new ArrayList<String>();
        commandLine.add(cli.command());
        commandLine.addAll(arguments);
        return commandLine;
    }

    /** Передаёт prompt и закрывает stdin, чтобы CLI начал обработку. */
    private static void writePrompt(Process process, String prompt) throws IOException {
        try (var input = process.getOutputStream()) {
            input.write(prompt.getBytes(StandardCharsets.UTF_8));
        }
    }

    /** Считывает stdout и stderr, объединённые ProcessBuilder. */
    private static String readOutput(Process process) throws IOException {
        return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    /** Ждёт завершения процесса после того, как его stdout полностью прочитан. */
    private static String await(Process process, long timeoutSeconds) throws IOException, InterruptedException {
        var output = readOutput(process);
        if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new AiGenerationException("AI CLI не ответил за " + timeoutSeconds + " секунд");
        }
        return output;
    }

    /** Преобразует ненулевой код завершения в понятную ошибку приложения. */
    private static void checkExitCode(Process process, String output) {
        if (process.exitValue() != 0) {
            throw new AiGenerationException("AI CLI завершился с кодом " + process.exitValue() + ": "
                    + limit(output));
        }
    }

    /** Создаёт файл финального ответа для Codex, у которого stdout содержит transcript. */
    private static Path createOutputFile(List<String> commandLine) throws IOException {
        return CODEX.equals(commandLine.getFirst()) ? Files.createTempFile("pamytno-codex-", ".txt") : null;
    }

    /** Добавляет Codex путь для финального ответа до позиционного prompt из stdin. */
    private static List<String> withOutputFile(List<String> commandLine, Path outputFile) {
        if (outputFile == null) {
            return commandLine;
        }
        var result = new ArrayList<>(commandLine);
        var promptIndex = result.lastIndexOf("-");
        result.add(promptIndex, "--output-last-message");
        result.add(promptIndex + 1, outputFile.toString());
        return result;
    }

    /** Добавляет выбранную модель, если она задана. */
    private static void addModel(List<String> arguments, String model) {
        if (model != null && !model.isBlank()) {
            arguments.add("--model");
            arguments.add(model);
        }
    }

    /** Ограничивает вывод CLI, попадающий в сообщение об ошибке. */
    private static String limit(String value) {
        var normalized = value.strip();
        return normalized.length() <= MAX_ERROR_OUTPUT_LENGTH ? normalized
                : normalized.substring(0, MAX_ERROR_OUTPUT_LENGTH) + "…";
    }
}
