package net.pamytno.deck.mapper;

import net.pamytno.deck.domain.Question;
import net.pamytno.deck.domain.QuestionDecision;
import net.pamytno.deck.domain.QuestionStatus;
import net.pamytno.deck.rest.dto.QuestionDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Преобразует вопросы и решения между доменом и DTO REST API.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface QuestionMapper {

    /**
     * Вопрос в DTO.
     *
     * @param question вопрос
     * @return DTO вопроса
     */
    QuestionDto toDto(Question question);

    /**
     * Список вопросов в DTO.
     *
     * @param questions вопросы
     * @return DTO вопросов
     */
    List<QuestionDto> toDtos(List<Question> questions);

    /**
     * Статус из фильтра запроса.
     *
     * @param status статус DTO или {@code null}
     * @return доменный статус или {@code null}
     */
    QuestionStatus toStatus(net.pamytno.deck.rest.dto.QuestionStatus status);

    /**
     * Решение из запроса.
     *
     * @param decision решение DTO
     * @return доменное решение
     */
    QuestionDecision toDecision(net.pamytno.deck.rest.dto.QuestionDecision decision);
}
