package net.pamytno.learning.mapper;

import net.pamytno.learning.domain.Dashboard;
import net.pamytno.learning.domain.TopicProgress;
import net.pamytno.learning.rest.dto.DashboardDto;
import net.pamytno.learning.rest.dto.TopicProgressDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Преобразует прогресс в DTO REST API.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ProgressMapper {

    /**
     * Прогресс темы.
     *
     * @param progress прогресс темы
     * @return DTO прогресса
     */
    @Mapping(target = ".", source = "stats")
    TopicProgressDto toDto(TopicProgress progress);

    /**
     * Общий прогресс.
     *
     * @param dashboard dashboard
     * @return DTO dashboard
     */
    @Mapping(target = ".", source = "totals")
    DashboardDto toDto(Dashboard dashboard);
}
