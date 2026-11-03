package br.com.fiap.petjourney.repositories;

import br.com.fiap.petjourney.models.ClinicTutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicTutorRepository extends JpaRepository<ClinicTutor, Long> {
    boolean existsByClinicIdAndTutorId(Long clinicId, Long tutorId);
    long countByTutorId(Long tutorId);
    void deleteByClinicIdAndTutorId(Long clinicId, Long tutorId);
}

