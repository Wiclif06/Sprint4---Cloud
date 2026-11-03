package br.com.fiap.petjourney.repositories;

import br.com.fiap.petjourney.models.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    Page<Pet> findByActiveTrue(Pageable pageable);
    Page<Pet> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Pet> findByTutorIdAndActiveTrue(Long tutorId, Pageable pageable);
    Page<Pet> findByTutorIdAndActiveTrueAndNameContainingIgnoreCase(Long tutorId, String name, Pageable pageable);
    Optional<Pet> findByIdAndActiveTrue(Long id);
    Optional<Pet> findByIdAndTutorIdAndActiveTrue(Long id, Long tutorId);

    @Query("select distinct p from Pet p where p.tutor.clinic.id = :clinicId and p.active = true and p.tutor.active = true and p.tutor.clinic.active = true")
    Page<Pet> findPatientsByClinicId(@Param("clinicId") Long clinicId, Pageable pageable);

    @Query("select distinct p from Pet p where p.tutor.clinic.id = :clinicId and p.active = true and p.tutor.active = true and p.tutor.clinic.active = true and lower(p.name) like lower(concat('%', :name, '%'))")
    Page<Pet> findPatientsByClinicIdAndName(@Param("clinicId") Long clinicId, @Param("name") String name, Pageable pageable);

    @Query("select p from Pet p where p.id = :id and p.tutor.clinic.id = :clinicId and p.active = true and p.tutor.active = true and p.tutor.clinic.active = true")
    Optional<Pet> findPatientByIdAndClinicId(@Param("id") Long id, @Param("clinicId") Long clinicId);

    @Modifying
    @Query("update Pet p set p.active = false where p.tutor.id = :tutorId")
    void deactivateByTutorId(@Param("tutorId") Long tutorId);

    @Modifying
    @Query("update Pet p set p.active = false where p.tutor.id in (select t.id from Tutor t where t.clinic.id = :clinicId)")
    void deactivateByClinicId(@Param("clinicId") Long clinicId);
}

