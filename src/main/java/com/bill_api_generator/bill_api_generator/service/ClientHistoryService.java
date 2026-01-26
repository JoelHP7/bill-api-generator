package com.bill_api_generator.bill_api_generator.service;

import com.bill_api_generator.bill_api_generator.model.Client;
import com.bill_api_generator.bill_api_generator.model.ClientHistory;
import com.bill_api_generator.bill_api_generator.repository.ClientHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for managing client history/audit trail.
 * Tracks all changes made to client records for audit purposes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClientHistoryService {

    private final ClientHistoryRepository clientHistoryRepository;
    private final ObjectMapper objectMapper;

    /**
     * Registers a change in the client history.
     * Runs in an independent transaction to ensure it's saved even if the main operation fails.
     *
     * @param client The client being modified
     * @param operation The type of operation (INSERT, UPDATE, DELETE)
     * @param previousValues Previous field values (for UPDATE operations)
     * @param newValues New field values (for UPDATE operations)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerChange(Client client, String operation, 
                                 Map<String, Object> previousValues, 
                                 Map<String, Object> newValues) {
        try {
            String changeDetails = null;

            if ("UPDATE".equals(operation) && previousValues != null && newValues != null) {
                Map<String, Map<String, Object>> changes = new HashMap<>();
                
                for (String field : previousValues.keySet()) {
                    Object previousValue = previousValues.get(field);
                    Object newValue = newValues.get(field);
                    
                    if (!String.valueOf(previousValue).equals(String.valueOf(newValue))) {
                        Map<String, Object> change = new HashMap<>();
                        change.put("previous", previousValue);
                        change.put("new", newValue);
                        changes.put(field, change);
                    }
                }
                
                if (!changes.isEmpty()) {
                    changeDetails = objectMapper.writeValueAsString(changes);
                }
            }

            ClientHistory history = ClientHistory.builder()
                    .clientId(client.getId())
                    .ref(client.getRef())
                    .name(client.getName())
                    .taxId(client.getTaxId())
                    .address(client.getAddress())
                    .postalCode(client.getPostalCode())
                    .rate(client.getRate())
                    .operation(operation)
                    .changeDate(LocalDateTime.now())
                    .changeDetails(changeDetails)
                    .build();

            clientHistoryRepository.save(history);
            log.info("Change registered in history: Client ID={}, Operation={}", client.getId(), operation);

        } catch (Exception e) {
            log.error("Error registering change in history for client ID={}", client.getId(), e);
            // Don't throw exception to avoid affecting the main transaction
        }
    }

    @Transactional(readOnly = true)
    public List<ClientHistory> getClientHistory(Long clientId) {
        return clientHistoryRepository.findByClientIdOrderByChangeDateDesc(clientId);
    }

    @Transactional(readOnly = true)
    public List<ClientHistory> getHistoryByOperation(String operation) {
        return clientHistoryRepository.findByOperation(operation);
    }
}
