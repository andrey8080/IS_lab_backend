package itmo.andrey.lab_backend.service.transaction;

import itmo.andrey.lab_backend.domain.entitie.HistoryImports;
import itmo.andrey.lab_backend.service.HistoryService;
import itmo.andrey.lab_backend.service.UserService;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class TransactionCoordinator {
    private final List<TransactionParticipant> participants = new ArrayList<>();
    private final UserService userService;
    private final HistoryService historyService;

    public TransactionCoordinator(UserService userService, HistoryService historyService) {
        this.userService = userService;
        this.historyService = historyService;
    }

    public void registerParticipant(TransactionParticipant participant) {
        participants.add(participant);
    }

    @SneakyThrows
    public void executeTransaction(String userName) {
        boolean allPrepared = true;

        HistoryImports historyImport = new HistoryImports();
        historyImport.setUser(userService.getUserByUsername(userName));

        for (TransactionParticipant participant : participants) {
            if (!participant.prepare()) {
                allPrepared = false;
                break;
            }
        }

        if (allPrepared) {
            try {
                Collections.reverse(participants);
                participants.forEach(participant -> participant.commit(historyImport.getUser()));
            } catch (Exception e1) {
                try {
                    participants.get(0).rollback();
                } catch (Exception e2) {
                    System.err.println("Ошибка при откате транзакции: " + e2.getMessage());
                }
                historyService.saveFailureHistory(historyImport, "Error during commit: " + e1.getMessage());
                throw e1;
            }
        } else {
            System.out.println("Ошибка подготовки. Откат транзакции...");
            participants.forEach(participant -> {
                try {
                    participant.rollback();
                } catch (Exception e) {
                    System.err.println("Ошибка при откате транзакции: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            historyService.saveFailureHistory(historyImport, "Error during prepare");
        }

        participants.clear();
    }
}