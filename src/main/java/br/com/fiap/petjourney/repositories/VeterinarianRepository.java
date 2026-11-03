package br.com.fiap.petjourney.repositories;

import br.com.fiap.petjourney.models.Veterinarian;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VeterinarianRepository extends JpaRepository<Veterinarian, Long> {
    Page<Veterinarian> findByActiveTrue(Pageable pageable);
    Page<Veterinarian> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Veterinarian> findByClinicIdAndActiveTrue(Long clinicId, Pageable pageable);
    Page<Veterinarian> findByClinicIdAndActiveTrueAndNameContainingIgnoreCase(Long clinicId, String name, Pageable pageable);
    Optional<Veterinarian> findByIdAndActiveTrue(Long id);
    Optional<Veterinarian> findByIdAndClinicIdAndActiveTrue(Long id, Long clinicId);

    @Modifying
    @Query("update Veterinarian v set v.active = false where v.clinic.id = :clinicId")
    void deactivateByClinicId(@Param("clinicId") Long clinicId);
}

