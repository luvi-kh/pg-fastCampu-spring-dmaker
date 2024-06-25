package com.fastcampus.programming.dmaker.service;

import com.fastcampus.programming.dmaker.entity.Developer;
import com.fastcampus.programming.dmaker.repository.DeveloperRepository;
import com.fastcampus.programming.dmaker.type.DeveloperLevel;
import com.fastcampus.programming.dmaker.type.DeveloperSkillType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DMakerService {
    private  final DeveloperRepository developerRepository;
    private final EntityManager em;

    // ACID
    // Atomic
    // Consistency
    // Isolation
    // Durability
    @Transactional
    public  void createDeveloper() {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            //business logic start
            Developer developer = Developer.builder()
                    .developerLevel(DeveloperLevel.JUNIOR)
                    .developerSkillType(DeveloperSkillType.FRONT_END)
                    .experienceYears(2)
                    .name("Olaf")
                    .age(5)
                    .build();

            /* A -> B 1만원 송금 */
            // A 계좌에서 1만원 줄임
            developerRepository.save(developer);
            // B 계좌에서 1만원 늘림
            developerRepository.delete(developer1);
            // business logic end

            transaction.commit();
        }catch (Exception e) {
            transaction.rollback();
            throw  e;
        }
    }
}
