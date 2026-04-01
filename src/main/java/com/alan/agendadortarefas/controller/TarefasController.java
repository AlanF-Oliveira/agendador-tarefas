package com.alan.agendadortarefas.controller;

import com.alan.agendadortarefas.business.TarefasService;
import com.alan.agendadortarefas.business.dto.TarefasDTORecord;
import com.alan.agendadortarefas.infrastructure.enums.StatusNotificacaoEnum;
import com.alan.agendadortarefas.infrastructure.security.SecurityConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/tarefas")
@RequiredArgsConstructor
@Tag(name = "Tarefas", description = "Cadastra tarefas de usuários")
@SecurityRequirement(name = SecurityConfig.SECURITY_SCHEME)
public class TarefasController {
    private final TarefasService tarefasService;

    @Operation(summary = "Cadastrar tarefa", description = "Cadastra uma nova tarefa para o usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarefa cadastrada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Token inválido")
    })
    @PostMapping
    public ResponseEntity<TarefasDTORecord> gravarTarefas(@RequestBody TarefasDTORecord dto,
                                                          @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(tarefasService.gravarTarefa(token, dto));
    }

    @Operation(summary = "Buscar tarefas por período", description = "Retorna lista de tarefas entre duas datas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tarefas retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Data inválida")
    })
    @GetMapping("/eventos")
    public ResponseEntity<List<TarefasDTORecord>> buscaListaDeTarefasPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFinal) {
        return ResponseEntity.ok(tarefasService.buscaTarefasAgendadasPorPeriodo(dataInicial, dataFinal));
    }

    @Operation(summary = "Buscar tarefas do usuário", description = "Retorna todas as tarefas do usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarefas retornadas com sucesso"),
            @ApiResponse(responseCode = "403", description = "Token inválido")
    })
    @GetMapping
    public ResponseEntity<List<TarefasDTORecord>> buscaTarefasPorEmail(@RequestHeader("Authorization") String token) {
        List<TarefasDTORecord> tarefas = tarefasService.buscarTarefasPorEmail(token);
        return ResponseEntity.ok(tarefas);
    }

    @Operation(summary = "Deletar tarefa", description = "Deleta uma tarefa pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarefa deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    @DeleteMapping
    public ResponseEntity<Void> deletaPorId(@RequestParam("id") String id) {
        tarefasService.deletaTarefaPorID(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Alterar status da notificação", description = "Altera o status da notificação da tarefa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status alterado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    @PatchMapping
    public ResponseEntity<TarefasDTORecord> alteraStatusDeNotificacao(
            @RequestParam("status") StatusNotificacaoEnum status,
            @RequestParam("id") String id) {
        return ResponseEntity.ok(tarefasService.alteraStatus(status, id));
    }

    @Operation(summary = "Atualizar tarefa", description = "Atualiza os dados de uma tarefa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    @PutMapping
    public ResponseEntity<TarefasDTORecord> updateTarefas(@RequestBody TarefasDTORecord dto,
                                                          @RequestParam("id") String id) {
        return ResponseEntity.ok(tarefasService.updateTarefas(dto, id));
    }
}
