package net.pamytno.deck.mapper;

import net.pamytno.deck.domain.Flashcard;
import net.pamytno.deck.rest.dto.FlashcardDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Преобразует карточки в DTO REST API.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface FlashcardMapper {

    /**
     * Карточка в DTO.
     *
     * @param card карточка
     * @return DTO карточки
     */
    FlashcardDto toDto(Flashcard card);

    /**
     * Список карточек в DTO.
     *
     * @param cards карточки
     * @return DTO карточек
     */
    List<FlashcardDto> toDtos(List<Flashcard> cards);
}
