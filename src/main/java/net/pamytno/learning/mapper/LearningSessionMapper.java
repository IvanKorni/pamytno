package net.pamytno.learning.mapper;

import net.pamytno.learning.domain.LearningSession;
import net.pamytno.learning.rest.dto.LearningSessionDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Преобразует учебные сессии в DTO REST API.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface LearningSessionMapper {

    /**
     * Сессия в DTO.
     *
     * @param session сессия
     * @return DTO сессии
     */
    LearningSessionDto toDto(LearningSession session);
}
