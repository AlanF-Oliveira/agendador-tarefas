package com.alan.agendadortarefas.business;

import com.alan.agendadortarefas.business.dto.TarefasDTO;
import com.alan.agendadortarefas.business.mapper.TarefaUpdateConverter;
import com.alan.agendadortarefas.business.mapper.TarefasConverter;
import com.alan.agendadortarefas.infrastructure.entity.TarefasEntity;
import com.alan.agendadortarefas.infrastructure.enums.StatusNotificacaoEnum;
import com.alan.agendadortarefas.infrastructure.exceptions.ResourceNotFoundException;
import com.alan.agendadortarefas.infrastructure.repository.TarefasRepository;
import com.alan.agendadortarefas.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TarefasService {

    private final TarefasRepository tarefasRepository;
    private final TarefasConverter tarefasConverter;
    private final JwtUtil jwtutil;
    private final TarefaUpdateConverter tarefaUpdateConverter;

    public TarefasDTO gravarTarefa(String token, TarefasDTO dto) {
        String email = jwtutil.extrairEmailToken(token.substring(7));
        dto.setDataCriacao(LocalDateTime.now());
        dto.setStatusNotificacaoEnum(StatusNotificacaoEnum.PENDENTE);
        dto.setEmailUsuario(email);
        TarefasEntity entity = tarefasConverter.paraTarefaEntity(dto);
        return tarefasConverter.paraTarefaDTO(tarefasRepository.save(entity));
    }

    public List<TarefasDTO> buscaTarefasAgendadasPorPeriodo(LocalDateTime dataInicio, LocalDateTime dataFinal) {
        return tarefasConverter.paraListaTarefasDTO(
                tarefasRepository.findByDataEventoBetweenAndStatusNotificacaoEnum(dataInicio, dataFinal, StatusNotificacaoEnum.PENDENTE));
    }

    public List<TarefasDTO> buscarTarefasPorEmail(String token) {
        String email = jwtutil.extrairEmailToken(token.substring(7));
//      return tarefasConverter.paraListaTarefasDTO(tarefasRepository.findByEmailUsuario(email)); MESMA COISA DO DE BAIXO
        List<TarefasEntity> listaTarefas = tarefasRepository.findByEmailUsuario(email);
        return tarefasConverter.paraListaTarefasDTO(listaTarefas);
    }

    public void deletaTarefaPorID(String id) {
        try {
            tarefasRepository.deleteById(id);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Erro ao deletar tarefa por id, id inexistente " + id, e.getCause());
        }
    }

    public TarefasDTO alteraStatus(StatusNotificacaoEnum status, String id) {
        try {
            TarefasEntity entity = tarefasRepository.findById(id).
                    orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada"));
            entity.setStatusNotificacaoEnum(status);
            return tarefasConverter.paraTarefaDTO(tarefasRepository.save(entity));
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException("Erro ao alterar o status da tarefa " + e.getCause());
        }
    }

    public TarefasDTO updateTarefas(TarefasDTO dto, String id) {
        try {
            TarefasEntity entity = tarefasRepository.findById(id).
                    orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada"));
            tarefaUpdateConverter.updateTarefas(dto, entity);
            return tarefasConverter.paraTarefaDTO(tarefasRepository.save(entity));
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException("Erro ao alterar o status da tarefa " + e.getCause());
        }
    }


}
