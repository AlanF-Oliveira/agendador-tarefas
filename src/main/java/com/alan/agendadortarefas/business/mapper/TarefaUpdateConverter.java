package com.alan.agendadortarefas.business.mapper;

import com.alan.agendadortarefas.business.dto.TarefasDTORecord;
import com.alan.agendadortarefas.infrastructure.entity.TarefasEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TarefaUpdateConverter {
    void updateTarefas(TarefasDTORecord dto, @MappingTarget TarefasEntity entity);
}
