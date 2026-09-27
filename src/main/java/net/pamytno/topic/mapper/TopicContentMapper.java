package net.pamytno.topic.mapper;

import net.pamytno.topic.domain.TopicContent;
import net.pamytno.topic.rest.dto.TopicContentDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Преобразует версию единого текста в DTO REST API.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface TopicContentMapper {

    /**
     * Версия текста в DTO.
     *
     * @param content версия текста
     * @return DTO текста
     */
    TopicContentDto toDto(TopicContent content);
}
