package org.istic.taa.todoapp.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.istic.taa.todoapp.domain.TODOItem;
import org.istic.taa.todoapp.repository.TODOItemRepository;
import org.istic.taa.todoapp.web.rest.util.HeaderUtil;
import org.istic.taa.todoapp.web.rest.util.PaginationUtil;
import org.istic.taa.todoapp.web.rest.dto.TODOItemDTO;
import org.istic.taa.todoapp.web.rest.mapper.TODOItemMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing TODOItem.
 */
@RestController
@RequestMapping("/api")
public class TODOItemResource {

    private final Logger log = LoggerFactory.getLogger(TODOItemResource.class);

    @Inject
    private TODOItemRepository tODOItemRepository;

    @Inject
    private TODOItemMapper tODOItemMapper;

    /**
     * POST  /tODOItems -> Create a new tODOItem.
     */
    @RequestMapping(value = "/tODOItems",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<TODOItemDTO> createTODOItem(@RequestBody TODOItemDTO tODOItemDTO) throws URISyntaxException {
        log.debug("REST request to save TODOItem : {}", tODOItemDTO);
        if (tODOItemDTO.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new tODOItem cannot already have an ID").body(null);
        }
        TODOItem tODOItem = tODOItemMapper.tODOItemDTOToTODOItem(tODOItemDTO);
        TODOItem result = tODOItemRepository.save(tODOItem);
        return ResponseEntity.created(new URI("/api/tODOItems/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert("tODOItem", result.getId().toString()))
                .body(tODOItemMapper.tODOItemToTODOItemDTO(result));
    }

    /**
     * PUT  /tODOItems -> Updates an existing tODOItem.
     */
    @RequestMapping(value = "/tODOItems",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<TODOItemDTO> updateTODOItem(@RequestBody TODOItemDTO tODOItemDTO) throws URISyntaxException {
        log.debug("REST request to update TODOItem : {}", tODOItemDTO);
        if (tODOItemDTO.getId() == null) {
            return createTODOItem(tODOItemDTO);
        }
        TODOItem tODOItem = tODOItemMapper.tODOItemDTOToTODOItem(tODOItemDTO);
        TODOItem result = tODOItemRepository.save(tODOItem);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert("tODOItem", tODOItemDTO.getId().toString()))
                .body(tODOItemMapper.tODOItemToTODOItemDTO(result));
    }

    /**
     * GET  /tODOItems -> get all the tODOItems.
     */
    @RequestMapping(value = "/tODOItems",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Transactional(readOnly = true)
    public ResponseEntity<List<TODOItemDTO>> getAllTODOItems(Pageable pageable)
        throws URISyntaxException {
        Page<TODOItem> page = tODOItemRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/tODOItems");
        return new ResponseEntity<>(page.getContent().stream()
            .map(tODOItemMapper::tODOItemToTODOItemDTO)
            .collect(Collectors.toCollection(LinkedList::new)), headers, HttpStatus.OK);
    }

    /**
     * GET  /tODOItems/:id -> get the "id" tODOItem.
     */
    @RequestMapping(value = "/tODOItems/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<TODOItemDTO> getTODOItem(@PathVariable Long id) {
        log.debug("REST request to get TODOItem : {}", id);
        return Optional.ofNullable(tODOItemRepository.findOne(id))
            .map(tODOItemMapper::tODOItemToTODOItemDTO)
            .map(tODOItemDTO -> new ResponseEntity<>(
                tODOItemDTO,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /tODOItems/:id -> delete the "id" tODOItem.
     */
    @RequestMapping(value = "/tODOItems/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteTODOItem(@PathVariable Long id) {
        log.debug("REST request to delete TODOItem : {}", id);
        tODOItemRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("tODOItem", id.toString())).build();
    }
}