package itmo.andrey.lab_backend.service.transaction;

import itmo.andrey.lab_backend.domain.entitie.User;

public interface TransactionParticipant {
    boolean prepare();
    String commit(User user);
    void rollback() throws Exception;
}
