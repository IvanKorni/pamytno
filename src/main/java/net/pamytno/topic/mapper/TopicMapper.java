package net.pamytno.topic.mapper;

import net.pamytno.topic.domain.Topic;
import net.pamytno.topic.rest.dto.TopicDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Преобразует темы в DTO REST API.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface TopicMapper {

    /**
     * Тема в DTO.
     *
     * @param topic тема
     * @return DTO темы
     */
    TopicDto toDto(Topic topic);

    /**
     * Список тем в DTO.
     *
     * @param topics темы
     * @return DTO тем
     */
    List<TopicDto> toDtos(List<Topic> topics);
}
