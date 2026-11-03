package br.com.fiap.petjourney.repositories;

import br.com.fiap.petjourney.models.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Page<Appointment> findByPetId(Long petId, Pageable pageable);
    Page<Appointment> findByPetIdAndPetTutorId(Long petId, Long tutorId, Pageable pageable);
    Page<Appointment> findByPetIdAndClinicId(Long petId, Long clinicId, Pageable pageable);
    Page<Appointment> findByPetIdAndVeterinarianId(Long petId, Long veterinarianId, Pageable pageable);
    Page<Appointment> findByDateTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<Appointment> findByClinicId(Long clinicId, Pageable pageable);
    Page<Appointment> findByPetTutorId(Long tutorId, Pageable pageable);
    Page<Appointment> findByVeterinarianId(Long veterinarianId, Pageable pageable);
    Page<Appointment> findByClinicIdAndDateTimeBetween(Long clinicId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<Appointment> findByPetTutorIdAndDateTimeBetween(Long tutorId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<Appointment> findByVeterinarianIdAndDateTimeBetween(Long veterinarianId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("""
            select count(a) > 0 from Appointment a
            where a.veterinarian.id = :veterinarianId
              and a.dateTime = :dateTime
              and a.status <> br.com.fiap.petjourney.models.enums.AppointmentStatus.CANCELADO
              and (:ignoredId is null or a.id <> :ignoredId)
            """)
    boolean existsScheduleConflict(
            @Param("veterinarianId") Long veterinarianId,
            @Param("dateTime") LocalDateTime dateTime,
            @Param("ignoredId") Long ignoredId
    );
}

