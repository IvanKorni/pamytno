package net.pamytno.deck.mapper;

import net.pamytno.deck.domain.GenerationJob;
import net.pamytno.deck.rest.dto.GenerationJobDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Преобразует задачи генерации в DTO REST API.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface GenerationJobMapper {

    /**
     * Задача в DTO.
     *
     * @param job задача
     * @return DTO задачи
     */
    GenerationJobDto toDto(GenerationJob job);
}
