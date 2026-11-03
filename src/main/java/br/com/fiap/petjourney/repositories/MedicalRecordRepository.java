package br.com.fiap.petjourney.repositories;

import br.com.fiap.petjourney.models.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    Page<MedicalRecord> findByPetId(Long petId, Pageable pageable);
    Page<MedicalRecord> findByPetIdAndPetTutorId(Long petId, Long tutorId, Pageable pageable);
    Page<MedicalRecord> findByPetIdAndVeterinarianClinicId(Long petId, Long clinicId, Pageable pageable);
    Page<MedicalRecord> findByPetIdAndVeterinarianId(Long petId, Long veterinarianId, Pageable pageable);
    Page<MedicalRecord> findByRegistrationDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<MedicalRecord> findByPetTutorId(Long tutorId, Pageable pageable);
    Page<MedicalRecord> findByVeterinarianClinicId(Long clinicId, Pageable pageable);
    Page<MedicalRecord> findByVeterinarianId(Long veterinarianId, Pageable pageable);
    Page<MedicalRecord> findByPetTutorIdAndRegistrationDateBetween(Long tutorId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<MedicalRecord> findByVeterinarianClinicIdAndRegistrationDateBetween(Long clinicId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<MedicalRecord> findByVeterinarianIdAndRegistrationDateBetween(Long veterinarianId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);
    boolean existsByAppointmentId(Long appointmentId);
}

