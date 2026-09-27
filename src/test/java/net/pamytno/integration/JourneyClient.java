package net.pamytno.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * HTTP-клиент сквозных сценариев: ходит в запущенное приложение так же, как фронт, с токеном пользователя.
 * Ошибочные статусы не бросают исключений — их проверяет тест.
 */
final class JourneyClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private String token;

    /**
     * Создаёт клиента.
     *
     * @param port         порт приложения
     * @param objectMapper JSON-маппер
     */
    JourneyClient(int port, ObjectMapper objectMapper) {
        this.restClient = RestClient.create("http://localhost:" + port);
        this.objectMapper = objectMapper;
    }

    /**
     * Регистрирует пользователя и входит под ним: дальше все запросы идут с его токеном.
     *
     * @param email    email
     * @param password пароль
     */
    void signUp(String email, String password) {
        var credentials = Map.of("email", email, "password", password);
        post("/api/auth/register", credentials);
        token = post("/api/auth/login", credentials).text("accessToken");
    }

    /**
     * GET-запрос.
     *
     * @param uri       шаблон пути
     * @param variables переменные пути
     * @return ответ
     */
    ApiResponse get(String uri, Object... variables) {
        return exchange(restClient.get().uri(uri, variables));
    }

    /**
     * POST-запрос с JSON-телом.
     *
     * @param uri       шаблон пути
     * @param body      тело или {@code null}
     * @param variables переменные пути
     * @return ответ
     */
    ApiResponse post(String uri, Object body, Object... variables) {
        var request = restClient.post().uri(uri, variables);
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).body(body);
        }
        return exchange(request);
    }

    /**
     * Загрузка файла multipart-формой в поле {@code file}.
     *
     * @param uri       шаблон пути
     * @param fileName  имя файла
     * @param content   содержимое
     * @param variables переменные пути
     * @return ответ
     */
    ApiResponse upload(String uri, String fileName, byte[] content, Object... variables) {
        var parts = new LinkedMultiValueMap<String, Object>();
        parts.add("file", new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return fileName;
            }
        });
        return exchange(restClient.post().uri(uri, variables).contentType(MediaType.MULTIPART_FORM_DATA).body(parts));
    }

    /**
     * DELETE-запрос.
     *
     * @param uri       шаблон пути
     * @param variables переменные пути
     * @return ответ
     */
    ApiResponse delete(String uri, Object... variables) {
        return exchange(restClient.delete().uri(uri, variables));
    }

    /**
     * Выполняет запрос с токеном и читает ответ при любом статусе.
     *
     * @param request запрос
     * @return ответ
     */
    private ApiResponse exchange(RestClient.RequestHeadersSpec<?> request) {
        return request
                .headers(headers -> {
                    if (token != null) {
                        headers.setBearerAuth(token);
                    }
                })
                .exchange((req, response) -> {
                    var bytes = response.getBody().readAllBytes();
                    var body = bytes.length == 0 ? null : objectMapper.readTree(bytes);
                    return new ApiResponse(response.getStatusCode().value(), body);
                });
    }
}
