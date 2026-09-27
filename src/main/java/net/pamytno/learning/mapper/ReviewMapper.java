package net.pamytno.learning.mapper;

import net.pamytno.learning.domain.CardProgress;
import net.pamytno.learning.domain.ReviewResult;
import net.pamytno.learning.rest.dto.DueCardDto;
import net.pamytno.learning.rest.dto.ReviewResultDto;
import net.pamytno.learning.service.ReviewOutcome;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Преобразует прогресс карточек и итоги ответов в DTO REST API.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ReviewMapper {

    /**
     * Карточка к повторению.
     *
     * @param progress прогресс карточки
     * @return DTO карточки с текстом
     */
    @Mapping(target = "front", source = "card.front")
    @Mapping(target = "back", source = "card.back")
    DueCardDto toDueCard(CardProgress progress);

    /**
     * Список карточек к повторению.
     *
     * @param progress прогресс карточек
     * @return DTO карточек
     */
    List<DueCardDto> toDueCards(List<CardProgress> progress);

    /**
     * Итог ответа.
     *
     * @param outcome итог
     * @return DTO итога
     */
    @Mapping(target = "cardId", source = "progress.cardId")
    @Mapping(target = "stage", source = "progress.stage")
    @Mapping(target = "nextReviewAt", source = "progress.nextReviewAt")
    @Mapping(target = "mastered", source = "progress.mastered")
    @Mapping(target = "returnToSession", expression = "java(outcome.returnToSession())")
    ReviewResultDto toDto(ReviewOutcome outcome);

    /**
     * Ответ из запроса.
     *
     * @param result ответ DTO
     * @return доменный ответ
     */
    ReviewResult toResult(net.pamytno.learning.rest.dto.ReviewResult result);
}
