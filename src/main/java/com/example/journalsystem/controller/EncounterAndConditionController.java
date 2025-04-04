package com.example.journalsystem.controller;

import com.example.journalsystem.bo.Service.*;
import com.example.journalsystem.bo.model.*;
import com.example.journalsystem.config.JwtTokenUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/encounter")
@CrossOrigin(origins = "http://localhost:3000")
public class EncounterAndConditionController {

    private final EncounterService encounterService;
    private final ConditionService conditionService;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public EncounterAndConditionController(EncounterService encounterService, ConditionService conditionService, JwtTokenUtil jwtTokenUtil) {
        this.encounterService = encounterService;
        this.conditionService = conditionService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Data
    public static class EncounterDTO {
        private Long patientId;
        private String reason;
        private String notes;
    }

    @Data
    public static class ConditionDTO {
        private Long patientId;
        private String diagnosis;
        private Condition.Status status;
    }

    @PostMapping("/encounters/add")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('STAFF')")
    public ResponseEntity<?> addEncounter(
            @RequestBody EncounterDTO encounterDTO,
            @RequestHeader(value = "Authorization") String authHeader) {
        if (!authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }
        String token = authHeader.substring(7);
        try {
            String username = jwtTokenUtil.extractUsernameFromToken(token);
            if(username==null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
            }
            Encounter encounter = new Encounter(
                    LocalDateTime.now(),
                    encounterDTO.getReason(),
                    encounterDTO.getPatientId(),
                    encounterDTO.getNotes()
            );
            Encounter savedEncounter = encounterService.createEncounter(encounter);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEncounter);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add encounter due to server error: " + e.getMessage());
        }
    }

    @PutMapping("/encounters/{encounterId}/update-notes")
    public ResponseEntity<?> updateEncounterNotes(
            @PathVariable Long encounterId,
            @RequestBody String notes,
            @RequestHeader(value = "Authorization") String authHeader) {
        if (!authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }
        String token = authHeader.substring(7); // Extract token after "Bearer "
        try {
            String username = jwtTokenUtil.extractUsernameFromToken(token);
            if(username==null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
            }
            Optional<Encounter> encounterOpt = encounterService.findEncounterById(encounterId);
            if (encounterOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Encounter not found.");
            }
            Encounter encounter = encounterOpt.get();
            encounter.setNotes(notes);
            Encounter updatedEncounter = encounterService.createEncounter(encounter);
            return ResponseEntity.ok(updatedEncounter);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update encounter notes due to server error: " + e.getMessage());
        }
    }

    @GetMapping("/encounters/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('STAFF')")
    public ResponseEntity<?> getEncountersByPatient(
            @PathVariable Long patientId,
            @RequestHeader(value = "Authorization") String authHeader) {
        if (!authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }
        String token = authHeader.substring(7);
        try {
            String username = jwtTokenUtil.extractUsernameFromToken(token);
            if(username==null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
            }
            List<Encounter> encounters = encounterService.getEncountersByPatientId(patientId);
            if (encounters.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No encounters found for the given patient.");
            }
            return ResponseEntity.ok(encounters);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve encounters due to server error: " + e.getMessage());
        }
    }


    @GetMapping("/conditions/show/{patientId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('STAFF')")
    public ResponseEntity<?> getConditionByPatient(
            @PathVariable Long patientId,
            @RequestHeader(value = "Authorization") String authHeader) {
        if (!authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }
        String token = authHeader.substring(7);
        try {
            String username = jwtTokenUtil.extractUsernameFromToken(token);
            if(username==null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
            }
            List<Condition> conditions = conditionService.getConditionByPatientId(patientId);
            if (conditions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No conditions found for the given patient.");
            }
            return ResponseEntity.ok(conditions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve conditions due to server error: " + e.getMessage());
        }
    }

    @PostMapping("/conditions/add")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('STAFF')")
    public ResponseEntity<?> addCondition(
            @RequestBody ConditionDTO conditionDTO,
            @RequestHeader(value = "Authorization") String authHeader) {
        if (!authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }
        String token = authHeader.substring(7); // Extract token after "Bearer "
        try {
            String username = jwtTokenUtil.extractUsernameFromToken(token);
            if(username==null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
            }
            Condition condition = new Condition(
                    conditionDTO.getDiagnosis(),
                    conditionDTO.getStatus(),
                    conditionDTO.getPatientId()
            );
            Condition savedCondition = conditionService.createCondition(condition);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCondition);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid status value. Please use ACTIVE or RESOLVED.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add condition due to server error: " + e.getMessage());
        }
    }

    @PutMapping("/conditions/update/{conditionId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('STAFF')")
    public ResponseEntity<?> updateCondition(
            @PathVariable Long conditionId,
            @RequestBody ConditionDTO conditionDTO,
            @RequestHeader(value = "Authorization") String authHeader) {
        if (!authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
        }
        String token = authHeader.substring(7); // Extract token after "Bearer "
        try {
            String username = jwtTokenUtil.extractUsernameFromToken(token);
            if(username==null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token.");
            }
            String newDiagnosis = conditionDTO.getDiagnosis();
            Condition.Status newStatus = conditionDTO.getStatus();
            Condition updatedCondition = conditionService.updateCondition(conditionId, newDiagnosis, newStatus);
            return ResponseEntity.ok(updatedCondition);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid status value. Please use ACTIVE or RESOLVED.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating condition: " + e.getMessage());
        }
    }
}
