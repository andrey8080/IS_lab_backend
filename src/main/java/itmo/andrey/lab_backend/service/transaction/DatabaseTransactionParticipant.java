package itmo.andrey.lab_backend.service.transaction;

import itmo.andrey.lab_backend.domain.dto.SpaceMarineDTO;
import itmo.andrey.lab_backend.domain.entitie.SpaceMarine;
import itmo.andrey.lab_backend.domain.entitie.User;
import itmo.andrey.lab_backend.repository.SpaceMarineRepository;
import itmo.andrey.lab_backend.service.SpaceMarineService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseTransactionParticipant implements TransactionParticipant {
    private final SpaceMarineService spaceMarineService;
    private final SpaceMarineRepository spaceMarineRepository;
    @Setter
    private List<SpaceMarineDTO> spaceMarineDTOList;
    @Setter
    private String userName;
    @Setter
    private boolean prepared = false;

    private final List<SpaceMarine> addedSpaceMarines = new ArrayList<>();

    @Autowired
    public DatabaseTransactionParticipant(SpaceMarineService spaceMarineService, SpaceMarineRepository spaceMarineRepository, List<SpaceMarineDTO> spaceMarineDTOList, String userName) {
        this.spaceMarineService = spaceMarineService;
        this.spaceMarineRepository = spaceMarineRepository;
        this.spaceMarineDTOList = spaceMarineDTOList;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean prepare() {
        try {
            spaceMarineRepository.count();
            prepared = true;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String commit(User user) {
        if (prepared) {
            try {
                for (SpaceMarineDTO dto : spaceMarineDTOList) {
                    SpaceMarine spaceMarine = spaceMarineService.addFromFile(dto, user.getName(), LocalDateTime.now());
                    addedSpaceMarines.add(spaceMarine);
                }
                return "DB commit success";
            } catch (Exception e) {
                return "DB commit failed: " + e.getMessage();
            }
        }
        return "DB commit failed";
    }

    @Override
    public void rollback() {
        if (prepared) {
            try {
                for (SpaceMarine spaceMarine : addedSpaceMarines) {
                    spaceMarineService.deleteSpaceMarine(spaceMarine.getId(), userName);
                }
                System.out.println("DB: Changes rolled back");
            } catch (Exception e) {
                System.out.println("DB rollback failed: " + e.getMessage());
            }
        }
    }
}