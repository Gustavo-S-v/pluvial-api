package br.com.fiap.pluvial.controller;

import br.com.fiap.pluvial.dao.LeituraDAO;
import br.com.fiap.pluvial.dto.LeituraRequestDTO;
import br.com.fiap.pluvial.dto.LeituraResponseDTO;
import br.com.fiap.pluvial.exception.RecursoNaoEncontradoException;
import br.com.fiap.pluvial.model.Leitura;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** O cadastro de leituras é feito pela rota aninhada POST /api/sensores/{id}/leituras. */
@RestController
@RequestMapping("/api/leituras")
public class LeituraController {

    private final LeituraDAO leituraDAO;
    private final ModelMapper modelMapper;

    public LeituraController(LeituraDAO leituraDAO, ModelMapper modelMapper) {
        this.leituraDAO = leituraDAO;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<List<LeituraResponseDTO>> listar() {
        List<LeituraResponseDTO> lista = leituraDAO.listar().stream()
                .map(l -> modelMapper.map(l, LeituraResponseDTO.class))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeituraResponseDTO> buscarPorId(@PathVariable("id") Long id) {
        Leitura leitura = leituraDAO.buscarPorId(id).orElseThrow(() -> naoEncontrada(id));
        return ResponseEntity.ok(modelMapper.map(leitura, LeituraResponseDTO.class));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeituraResponseDTO> atualizar(@PathVariable("id") Long id,
                                                        @Valid @RequestBody LeituraRequestDTO dto) {
        Leitura existente = leituraDAO.buscarPorId(id).orElseThrow(() -> naoEncontrada(id));
        modelMapper.map(dto, existente);
        leituraDAO.atualizar(existente);
        return ResponseEntity.ok(modelMapper.map(existente, LeituraResponseDTO.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable("id") Long id) {
        if (!leituraDAO.remover(id)) {
            throw naoEncontrada(id);
        }
        return ResponseEntity.noContent().build();
    }

    private RecursoNaoEncontradoException naoEncontrada(Long id) {
        return new RecursoNaoEncontradoException("Leitura não encontrada com o id " + id + ".");
    }
}
