package br.com.fiap.petjourney.repositories;

import br.com.fiap.petjourney.models.Tutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, Long> {
    Page<Tutor> findByActiveTrue(Pageable pageable);
    Page<Tutor> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Tutor> findByIdAndActiveTrue(Long id);
    boolean existsByCpf(String cpf);
    boolean existsByCpfAndIdNot(String cpf, Long id);

    @Query("select distinct t from Tutor t where t.clinic.id = :clinicId and t.active = true and t.clinic.active = true")
    Page<Tutor> findClientsByClinicId(@Param("clinicId") Long clinicId, Pageable pageable);

    @Query("select distinct t from Tutor t where t.clinic.id = :clinicId and t.active = true and t.clinic.active = true and lower(t.name) like lower(concat('%', :name, '%'))")
    Page<Tutor> findClientsByClinicIdAndName(@Param("clinicId") Long clinicId, @Param("name") String name, Pageable pageable);

    @Query("select t from Tutor t where t.id = :id and t.clinic.id = :clinicId and t.active = true and t.clinic.active = true")
    Optional<Tutor> findClientByIdAndClinicId(@Param("id") Long id, @Param("clinicId") Long clinicId);

    @Modifying
    @Query("update Tutor t set t.active = false where t.clinic.id = :clinicId")
    void deactivateByClinicId(@Param("clinicId") Long clinicId);
}

