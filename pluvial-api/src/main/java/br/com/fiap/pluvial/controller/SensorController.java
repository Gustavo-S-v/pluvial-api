package br.com.fiap.pluvial.controller;

import br.com.fiap.pluvial.dao.LeituraDAO;
import br.com.fiap.pluvial.dao.SensorDAO;
import br.com.fiap.pluvial.dto.LeituraRequestDTO;
import br.com.fiap.pluvial.dto.LeituraResponseDTO;
import br.com.fiap.pluvial.dto.SensorRequestDTO;
import br.com.fiap.pluvial.dto.SensorResponseDTO;
import br.com.fiap.pluvial.exception.RecursoNaoEncontradoException;
import br.com.fiap.pluvial.exception.RegraNegocioException;
import br.com.fiap.pluvial.model.Leitura;
import br.com.fiap.pluvial.model.Sensor;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/sensores")
public class SensorController {

    private final SensorDAO sensorDAO;
    private final LeituraDAO leituraDAO;
    private final ModelMapper modelMapper;

    public SensorController(SensorDAO sensorDAO, LeituraDAO leituraDAO, ModelMapper modelMapper) {
        this.sensorDAO = sensorDAO;
        this.leituraDAO = leituraDAO;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    public ResponseEntity<SensorResponseDTO> cadastrar(@Valid @RequestBody SensorRequestDTO dto) {
        Sensor salvo = sensorDAO.inserir(modelMapper.map(dto, Sensor.class));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(salvo.getId())
                .toUri();

        return ResponseEntity.created(location).body(modelMapper.map(salvo, SensorResponseDTO.class));
    }

    @GetMapping
    public ResponseEntity<List<SensorResponseDTO>> listar() {
        List<SensorResponseDTO> lista = sensorDAO.listar().stream()
                .map(s -> modelMapper.map(s, SensorResponseDTO.class))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SensorResponseDTO> buscarPorId(@PathVariable("id") Long id) {
        Sensor sensor = sensorDAO.buscarPorId(id).orElseThrow(() -> naoEncontrado(id));
        return ResponseEntity.ok(modelMapper.map(sensor, SensorResponseDTO.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SensorResponseDTO> atualizar(@PathVariable("id") Long id,
                                                       @Valid @RequestBody SensorRequestDTO dto) {
        Sensor existente = sensorDAO.buscarPorId(id).orElseThrow(() -> naoEncontrado(id));
        modelMapper.map(dto, existente);
        sensorDAO.atualizar(existente);
        return ResponseEntity.ok(modelMapper.map(existente, SensorResponseDTO.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable("id") Long id) {
        if (!sensorDAO.remover(id)) {
            throw naoEncontrado(id);
        }
        return ResponseEntity.noContent().build();
    }

    // Rotas aninhadas do relacionamento 1:N (sensor -> leituras)
    @GetMapping("/{id}/leituras")
    public ResponseEntity<List<LeituraResponseDTO>> listarLeituras(@PathVariable("id") Long id) {
        sensorDAO.buscarPorId(id).orElseThrow(() -> naoEncontrado(id));

        List<LeituraResponseDTO> lista = leituraDAO.listarPorSensor(id).stream()
                .map(l -> modelMapper.map(l, LeituraResponseDTO.class))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/{id}/leituras")
    public ResponseEntity<LeituraResponseDTO> registrarLeitura(@PathVariable("id") Long id,
                                                               @Valid @RequestBody LeituraRequestDTO dto) {
        Sensor sensor = sensorDAO.buscarPorId(id).orElseThrow(() -> naoEncontrado(id));

        // REGRA DE NEGÓCIO 1: sensor inativo não aceita leituras (HTTP 409)
        if (!sensor.estaAtivo()) {
            throw new RegraNegocioException("O sensor " + id + " está inativo e não aceita novas leituras.");
        }

        Leitura nova = modelMapper.map(dto, Leitura.class);
        nova.setSensorId(id);

        // REGRA DE NEGÓCIO 2: cada leitura recalcula o nível de risco da região (mesma transação)
        String nivelRisco = sensor.calcularNivelRisco(nova.getValor());
        Leitura salva = leituraDAO.registrar(nova, nivelRisco);

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/leituras/{id}")
                .buildAndExpand(salva.getId())
                .toUri();

        return ResponseEntity.created(location).body(modelMapper.map(salva, LeituraResponseDTO.class));
    }

    private RecursoNaoEncontradoException naoEncontrado(Long id) {
        return new RecursoNaoEncontradoException("Sensor não encontrado com o id " + id + ".");
    }
}
