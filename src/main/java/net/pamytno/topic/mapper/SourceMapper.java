package net.pamytno.topic.mapper;

import net.pamytno.topic.domain.Source;
import net.pamytno.topic.domain.SourceType;
import net.pamytno.topic.rest.dto.SourceDto;
import net.pamytno.topic.rest.dto.SourceTextDto;
import net.pamytno.topic.rest.dto.TextSourceType;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.ValueMapping;

import java.util.List;

/**
 * Преобразует источники в DTO REST API и обратно — виды текстовых источников.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface SourceMapper {

    /**
     * Источник в DTO без текстов (для списков и опроса статуса).
     *
     * @param source источник
     * @return DTO источника
     */
    @Mapping(target = "extractedTextLength",
            expression = "java(source.getExtractedText() == null ? null : source.getExtractedText().length())")
    SourceDto toDto(Source source);

    /**
     * Список источников в DTO.
     *
     * @param sources источники
     * @return DTO источников
     */
    List<SourceDto> toDtos(List<Source> sources);

    /**
     * Исходный и извлечённый тексты источника.
     *
     * @param source источник
     * @return DTO текстов
     */
    @Mapping(target = "sourceId", source = "id")
    SourceTextDto toTextDto(Source source);

    /**
     * Вид текстового источника из запроса; по умолчанию — обычный текст.
     *
     * @param type вид из запроса, может быть {@code null}
     * @return вид источника
     */
    @ValueMapping(source = MappingConstants.NULL, target = "TEXT")
    SourceType toSourceType(TextSourceType type);
}
