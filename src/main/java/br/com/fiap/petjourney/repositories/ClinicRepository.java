package br.com.fiap.petjourney.repositories;

import br.com.fiap.petjourney.models.Clinic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {
    Page<Clinic> findByActiveTrue(Pageable pageable);
    Optional<Clinic> findByIdAndActiveTrue(Long id);

    @Query("select t.clinic from Tutor t where t.id = :tutorId and t.active = true and t.clinic.active = true")
    List<Clinic> findByTutorId(@Param("tutorId") Long tutorId);
}

