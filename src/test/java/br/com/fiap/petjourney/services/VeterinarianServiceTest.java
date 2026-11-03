package br.com.fiap.petjourney.services;

import br.com.fiap.petjourney.dtos.request.VeterinarianRequest;
import br.com.fiap.petjourney.dtos.response.VeterinarianResponse;
import br.com.fiap.petjourney.exceptions.ConflictOperationException;
import br.com.fiap.petjourney.exceptions.ForbiddenOperationException;
import br.com.fiap.petjourney.exceptions.ResourceNotFoundException;
import br.com.fiap.petjourney.models.Clinic;
import br.com.fiap.petjourney.models.UserAccount;
import br.com.fiap.petjourney.models.Veterinarian;
import br.com.fiap.petjourney.models.enums.UserRole;
import br.com.fiap.petjourney.repositories.ClinicRepository;
import br.com.fiap.petjourney.repositories.UserAccountRepository;
import br.com.fiap.petjourney.repositories.VeterinarianRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VeterinarianServiceTest {

    @Mock
    private VeterinarianRepository repository;
    @Mock
    private ClinicRepository clinicRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private AuthenticatedUserService authenticatedUser;

    @InjectMocks
    private VeterinarianService service;

    private Clinic clinic() {
        return Clinic.builder().id(1L).name("Clinica Cloud PetJourney").build();
    }

    private VeterinarianRequest request(String email, Long clinicId) {
        return new VeterinarianRequest("Dra Marina Cloud", "SP-90001", "11977770001", email, "Clinica Geral", clinicId);
    }

    private Veterinarian existingVet() {
        return Veterinarian.builder()
                .id(10L)
                .name("Dra Marina Cloud")
                .crmv("SP-90001")
                .phone("11977770001")
                .specialty("Clinica Geral")
                .clinic(clinic())
                .build();
    }

    @Test
    @DisplayName("create: ADMIN_CLINICA cadastra veterinario sem e-mail (sem criar acesso)")
    void create_adminClinicaSemEmail_salvaVeterinarioSemConta() {
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_CLINICA);
        when(authenticatedUser.clinicId()).thenReturn(1L);
        when(clinicRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(clinic()));
        when(repository.save(any(Veterinarian.class))).thenAnswer(invocation -> {
            Veterinarian vet = invocation.getArgument(0);
            vet.setId(10L);
            return vet;
        });
        VeterinarianResponse response = service.create(request(null, 1L));
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getCrmv()).isEqualTo("SP-90001");
        assertThat(response.getClinicName()).isEqualTo("Clinica Cloud PetJourney");
        verify(userAccountRepository, never()).save(any(UserAccount.class));
    }

    @Test
    @DisplayName("create: com e-mail cria acesso inativo e envia codigo de primeiro acesso")
    void create_adminClinicaComEmail_criaAcessoInativoEEnviaCodigo() {
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_CLINICA);
        when(authenticatedUser.clinicId()).thenReturn(1L);
        when(clinicRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(clinic()));
        when(repository.save(any(Veterinarian.class))).thenAnswer(invocation -> {
            Veterinarian vet = invocation.getArgument(0);
            vet.setId(10L);
            return vet;
        });
        when(passwordEncoder.encode(anyString())).thenReturn("senha-criptografada");
        service.create(request("Dra.Marina@Vet.com", 1L));
        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        UserAccount account = captor.getValue();
        assertThat(account.getUsername()).isEqualTo("dra.marina@vet.com");
        assertThat(account.getActive()).isFalse();
        assertThat(account.getRole()).isEqualTo(UserRole.VETERINARIO);
        verify(emailService).sendFirstAccessCode(
                eq("dra.marina@vet.com"), eq("Dra Marina Cloud"), anyString(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("create: clinica diferente da clinica do administrador lanca Forbidden")
    void create_clinicaDiferenteDaDoAdmin_lancaForbidden() {
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_CLINICA);
        when(authenticatedUser.clinicId()).thenReturn(1L);
        assertThatThrownBy(() -> service.create(request(null, 2L)))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(repository, never()).save(any(Veterinarian.class));
    }

    @Test
    @DisplayName("create: perfil diferente de ADMIN_CLINICA lanca Forbidden")
    void create_perfilNaoAdminClinica_lancaForbidden() {
        when(authenticatedUser.role()).thenReturn(UserRole.VETERINARIO);
        assertThatThrownBy(() -> service.create(request(null, 1L)))
                .isInstanceOf(ForbiddenOperationException.class);
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("create: e-mail ja cadastrado lanca ConflictOperationException")
    void create_emailJaCadastrado_lancaConflict() {
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_CLINICA);
        when(authenticatedUser.clinicId()).thenReturn(1L);
        when(userAccountRepository.existsByUsername("dra.marina@vet.com")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request("dra.marina@vet.com", 1L)))
                .isInstanceOf(ConflictOperationException.class);
        verify(repository, never()).save(any(Veterinarian.class));
    }

    @Test
    @DisplayName("findById: veterinario fora da clinica do usuario lanca ResourceNotFoundException")
    void findById_adminClinicaVeterinarioDeOutraClinica_lancaResourceNotFound() {
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_CLINICA);
        when(authenticatedUser.clinicId()).thenReturn(1L);
        when(repository.findByIdAndClinicIdAndActiveTrue(5L, 1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findById: ADMIN_SISTEMA encontra veterinario ativo")
    void findById_adminSistema_retornaVeterinario() {
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_SISTEMA);
        when(repository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(existingVet()));
        VeterinarianResponse response = service.findById(10L);
        assertThat(response.getName()).isEqualTo("Dra Marina Cloud");
        assertThat(response.getSpecialty()).isEqualTo("Clinica Geral");
    }

    @Test
    @DisplayName("findAll: sem filtro lista veterinarios ativos")
    void findAll_semFiltro_listaVeterinariosAtivos() {
        Pageable pageable = PageRequest.of(0, 10);
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_SISTEMA);
        when(repository.findByActiveTrue(pageable))
                .thenReturn(new PageImpl<>(List.of(existingVet()), pageable, 1));
        Page<VeterinarianResponse> page = service.findAll(null, pageable);
        assertThat(page.getTotalElements()).isEqualTo(1);
        verify(repository).findByActiveTrue(pageable);
    }

    @Test
    @DisplayName("findAll: com filtro por nome usa a busca ignorando maiusculas")
    void findAll_comFiltroPorNome_usaBuscaPorNome() {
        Pageable pageable = PageRequest.of(0, 10);
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_SISTEMA);
        when(repository.findByActiveTrueAndNameContainingIgnoreCase("mar", pageable))
                .thenReturn(new PageImpl<>(List.of(existingVet()), pageable, 1));
        Page<VeterinarianResponse> page = service.findAll("mar", pageable);
        assertThat(page.getContent()).hasSize(1);
        verify(repository).findByActiveTrueAndNameContainingIgnoreCase("mar", pageable);
    }

    @Test
    @DisplayName("update: atualiza a especialidade do veterinario")
    void update_adminClinica_atualizaEspecialidade() {
        Veterinarian vet = existingVet();
        VeterinarianRequest request = new VeterinarianRequest(
                "Dra Marina Cloud", "SP-90001", "11966660001", null, "Dermatologia Veterinaria", 1L);
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_CLINICA);
        when(authenticatedUser.clinicId()).thenReturn(1L);
        when(repository.findByIdAndClinicIdAndActiveTrue(10L, 1L)).thenReturn(Optional.of(vet));
        when(clinicRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(clinic()));
        when(repository.save(vet)).thenReturn(vet);
        VeterinarianResponse response = service.update(10L, request);
        assertThat(response.getSpecialty()).isEqualTo("Dermatologia Veterinaria");
        assertThat(vet.getPhone()).isEqualTo("11966660001");
        verify(repository).save(vet);
    }

    @Test
    @DisplayName("delete: exclusao logica desativa o veterinario e a conta de acesso")
    void delete_adminClinica_desativaVeterinarioEConta() {
        Veterinarian vet = existingVet();
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_CLINICA);
        when(authenticatedUser.clinicId()).thenReturn(1L);
        when(repository.findByIdAndClinicIdAndActiveTrue(10L, 1L)).thenReturn(Optional.of(vet));
        service.delete(10L);
        assertThat(vet.getActive()).isFalse();
        verify(userAccountRepository).deactivateVeterinarianAccount(10L);
        verify(repository).save(vet);
    }
}
