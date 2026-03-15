package com.alan.agendadortarefas.infrastructure.repository;

import com.alan.agendadortarefas.infrastructure.entity.TarefasEntity;
import com.alan.agendadortarefas.infrastructure.enums.StatusNotificacaoEnum;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TarefasRepository extends MongoRepository<TarefasEntity, String> {
    List<TarefasEntity> findByDataEventoBetweenAndStatusNotificacaoEnum(LocalDateTime dataInicial,
                                                                        LocalDateTime dataFinal,
                                                                        StatusNotificacaoEnum status) ;

    List<TarefasEntity> findByEmailUsuario(String email);

}
