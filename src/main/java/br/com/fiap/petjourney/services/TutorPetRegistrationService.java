package br.com.fiap.petjourney.services;

import br.com.fiap.petjourney.dtos.request.PetRequest;
import br.com.fiap.petjourney.dtos.request.RegisterTutorWithPetRequest;
import br.com.fiap.petjourney.dtos.response.PetResponse;
import br.com.fiap.petjourney.dtos.response.RegisterTutorWithPetResponse;
import br.com.fiap.petjourney.dtos.response.TutorResponse;
import br.com.fiap.petjourney.repositories.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TutorPetRegistrationService {

    private final TutorService tutorService;
    private final PetService petService;
    private final UserAccountRepository userAccountRepository;

    @Transactional
    public RegisterTutorWithPetResponse register(RegisterTutorWithPetRequest request) {
        var tutor = tutorService.createTutorForAuthenticatedClinic(request.tutor());

        var petRequest = new PetRequest(
                request.pet().name(),
                request.pet().species(),
                request.pet().breed(),
                request.pet().sex(),
                request.pet().birthDate(),
                request.pet().weight(),
                tutor.getId()
        );
        PetResponse pet = petService.create(petRequest);

        return new RegisterTutorWithPetResponse(
                TutorResponse.fromEntity(tutor),
                pet,
                findFirstAccessCode(tutor.getId())
        );
    }

    private String findFirstAccessCode(Long tutorId) {
        return userAccountRepository.findByTutorId(tutorId)
                .map(user -> user.getFirstAccessCode())
                .orElse(null);
    }
}

