package br.com.fiap.petjourney.repositories;

import br.com.fiap.petjourney.models.Medication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {
    Page<Medication> findByPetId(Long petId, Pageable pageable);
    Page<Medication> findByPetIdAndPetTutorId(Long petId, Long tutorId, Pageable pageable);
    Page<Medication> findByPetIdAndVeterinarianClinicId(Long petId, Long clinicId, Pageable pageable);
    Page<Medication> findByPetIdAndVeterinarianId(Long petId, Long veterinarianId, Pageable pageable);
    Page<Medication> findByPetTutorId(Long tutorId, Pageable pageable);
    Page<Medication> findByVeterinarianClinicId(Long clinicId, Pageable pageable);
    Page<Medication> findByVeterinarianId(Long veterinarianId, Pageable pageable);
    Page<Medication> findByMedicalRecordId(Long medicalRecordId, Pageable pageable);
}

