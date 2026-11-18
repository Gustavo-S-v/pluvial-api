package br.com.fiap.pluvial.controller;

import br.com.fiap.pluvial.dao.RegiaoDAO;
import br.com.fiap.pluvial.dao.SensorDAO;
import br.com.fiap.pluvial.dto.RegiaoRequestDTO;
import br.com.fiap.pluvial.dto.RegiaoResponseDTO;
import br.com.fiap.pluvial.dto.SensorResponseDTO;
import br.com.fiap.pluvial.exception.RecursoNaoEncontradoException;
import br.com.fiap.pluvial.model.Regiao;
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
@RequestMapping("/api/regioes")
public class RegiaoController {

    private final RegiaoDAO regiaoDAO;
    private final SensorDAO sensorDAO;
    private final ModelMapper modelMapper;

    public RegiaoController(RegiaoDAO regiaoDAO, SensorDAO sensorDAO, ModelMapper modelMapper) {
        this.regiaoDAO = regiaoDAO;
        this.sensorDAO = sensorDAO;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    public ResponseEntity<RegiaoResponseDTO> cadastrar(@Valid @RequestBody RegiaoRequestDTO dto) {
        Regiao salva = regiaoDAO.inserir(modelMapper.map(dto, Regiao.class));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(salva.getId())
                .toUri();

        return ResponseEntity.created(location).body(modelMapper.map(salva, RegiaoResponseDTO.class));
    }

    @GetMapping
    public ResponseEntity<List<RegiaoResponseDTO>> listar() {
        List<RegiaoResponseDTO> lista = regiaoDAO.listar().stream()
                .map(r -> modelMapper.map(r, RegiaoResponseDTO.class))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegiaoResponseDTO> buscarPorId(@PathVariable("id") Long id) {
        Regiao regiao = regiaoDAO.buscarPorId(id).orElseThrow(() -> naoEncontrada(id));
        return ResponseEntity.ok(modelMapper.map(regiao, RegiaoResponseDTO.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegiaoResponseDTO> atualizar(@PathVariable("id") Long id,
                                                       @Valid @RequestBody RegiaoRequestDTO dto) {
        Regiao existente = regiaoDAO.buscarPorId(id).orElseThrow(() -> naoEncontrada(id));
        modelMapper.map(dto, existente);   // o nível de risco só muda por meio das leituras
        regiaoDAO.atualizar(existente);
        return ResponseEntity.ok(modelMapper.map(existente, RegiaoResponseDTO.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable("id") Long id) {
        if (!regiaoDAO.remover(id)) {
            throw naoEncontrada(id);
        }
        return ResponseEntity.noContent().build();
    }

    // Rota aninhada do relacionamento 1:N (região -> sensores)
    @GetMapping("/{id}/sensores")
    public ResponseEntity<List<SensorResponseDTO>> listarSensores(@PathVariable("id") Long id) {
        regiaoDAO.buscarPorId(id).orElseThrow(() -> naoEncontrada(id));

        List<SensorResponseDTO> lista = sensorDAO.listarPorRegiao(id).stream()
                .map(s -> modelMapper.map(s, SensorResponseDTO.class))
                .toList();
        return ResponseEntity.ok(lista);
    }

    private RecursoNaoEncontradoException naoEncontrada(Long id) {
        return new RecursoNaoEncontradoException("Região não encontrada com o id " + id + ".");
    }
}
