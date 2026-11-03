package br.com.fiap.petjourney.repositories;

import br.com.fiap.petjourney.models.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<UserAccount> findByUsernameAndFirstAccessCode(String username, String firstAccessCode);
    Optional<UserAccount> findByTutorId(Long tutorId);
    Optional<UserAccount> findByVeterinarianId(Long veterinarianId);

    @Modifying
    @Query("update UserAccount u set u.active = false where u.clinic.id = :clinicId")
    void deactivateClinicAccounts(@Param("clinicId") Long clinicId);

    @Modifying
    @Query("update UserAccount u set u.active = false where u.tutor.id = :tutorId")
    void deactivateTutorAccount(@Param("tutorId") Long tutorId);

    @Modifying
    @Query("update UserAccount u set u.active = false where u.veterinarian.id = :veterinarianId")
    void deactivateVeterinarianAccount(@Param("veterinarianId") Long veterinarianId);

    @Modifying
    @Query("update UserAccount u set u.active = false where u.tutor.id in (select t.id from Tutor t where t.clinic.id = :clinicId)")
    void deactivateTutorAccountsByClinicId(@Param("clinicId") Long clinicId);

    @Modifying
    @Query("update UserAccount u set u.active = false where u.veterinarian.id in (select v.id from Veterinarian v where v.clinic.id = :clinicId)")
    void deactivateVeterinarianAccountsByClinicId(@Param("clinicId") Long clinicId);
}

