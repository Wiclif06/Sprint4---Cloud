package br.com.fiap.petjourney.repositories;

import br.com.fiap.petjourney.models.VeterinarianAvailability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VeterinarianAvailabilityRepository extends JpaRepository<VeterinarianAvailability, Long> {
    Page<VeterinarianAvailability> findByVeterinarianIdAndStartTimeBetween(Long veterinarianId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    Optional<VeterinarianAvailability> findByIdAndVeterinarianClinicId(Long id, Long clinicId);
    boolean existsByVeterinarianIdAndStartTime(Long veterinarianId, LocalDateTime startTime);

    @Query("""
            select va from VeterinarianAvailability va
            where va.veterinarian.clinic.id = :clinicId
              and (:veterinarianId is null or va.veterinarian.id = :veterinarianId)
              and va.startTime between :start and :end
              and not exists (
                  select a.id from Appointment a
                  where a.veterinarian = va.veterinarian
                    and a.dateTime = va.startTime
                    and a.status <> br.com.fiap.petjourney.models.enums.AppointmentStatus.CANCELADO
              )
            """)
    Page<VeterinarianAvailability> findAvailableSlots(
            @Param("clinicId") Long clinicId,
            @Param("veterinarianId") Long veterinarianId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}

