package br.com.fiap.petjourney.services;

import br.com.fiap.petjourney.dtos.request.ClinicRequest;
import br.com.fiap.petjourney.dtos.response.ClinicResponse;
import br.com.fiap.petjourney.exceptions.ForbiddenOperationException;
import br.com.fiap.petjourney.exceptions.ResourceNotFoundException;
import br.com.fiap.petjourney.models.Clinic;
import br.com.fiap.petjourney.models.enums.UserRole;
import br.com.fiap.petjourney.repositories.ClinicRepository;
import br.com.fiap.petjourney.repositories.PetRepository;
import br.com.fiap.petjourney.repositories.TutorRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClinicServiceTest {

    @Mock
    private ClinicRepository repository;
    @Mock
    private TutorRepository tutorRepository;
    @Mock
    private PetRepository petRepository;
    @Mock
    private VeterinarianRepository veterinarianRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private AuthenticatedUserService authenticatedUser;

    @InjectMocks
    private ClinicService service;

    private ClinicRequest request(String name) {
        return new ClinicRequest(name, "11222333000181", "11999990001", "cloud@petjourney.com", "Avenida Paulista 1000");
    }

    private Clinic existingClinic() {
        return Clinic.builder()
                .id(1L)
                .name("Clinica Antiga")
                .cnpj("11222333000181")
                .phone("11900000000")
                .email("antiga@petjourney.com")
                .address("Rua Antiga 1")
                .build();
    }

    @Test
    @DisplayName("create: administrador do sistema cadastra clinica ativa")
    void create_adminSistema_salvaClinicaAtiva() {
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_SISTEMA);
        when(repository.save(any(Clinic.class))).thenAnswer(invocation -> {
            Clinic clinic = invocation.getArgument(0);
            clinic.setId(1L);
            return clinic;
        });
        ClinicResponse response = service.create(request("Clinica Cloud PetJourney"));
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Clinica Cloud PetJourney");
        ArgumentCaptor<Clinic> captor = ArgumentCaptor.forClass(Clinic.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getActive()).isTrue();
    }

    @Test
    @DisplayName("create: perfil diferente de ADMIN_SISTEMA recebe ForbiddenOperationException")
    void create_perfilNaoAdminSistema_lancaForbidden() {
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_CLINICA);
        assertThatThrownBy(() -> service.create(request("Clinica Cloud")))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(repository, never()).save(any(Clinic.class));
    }

    @Test
    @DisplayName("findById: clinica inexistente lanca ResourceNotFoundException")
    void findById_clinicaInexistente_lancaResourceNotFound() {
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_SISTEMA);
        when(repository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Clinica nao encontrada");
    }

    @Test
    @DisplayName("findById: ADMIN_CLINICA nao acessa dados de outra clinica")
    void findById_adminClinicaDeOutraClinica_lancaForbidden() {
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_CLINICA);
        when(authenticatedUser.clinicId()).thenReturn(2L);
        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(ForbiddenOperationException.class);
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("findById: retorna a clinica ativa encontrada")
    void findById_clinicaExistente_retornaResponse() {
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_SISTEMA);
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(existingClinic()));
        ClinicResponse response = service.findById(1L);
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Clinica Antiga");
    }

    @Test
    @DisplayName("findAll: ADMIN_SISTEMA lista clinicas ativas com paginacao")
    void findAll_adminSistema_retornaClinicasAtivasPaginadas() {
        Pageable pageable = PageRequest.of(0, 10);
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_SISTEMA);
        when(repository.findByActiveTrue(pageable))
                .thenReturn(new PageImpl<>(List.of(existingClinic()), pageable, 1));
        Page<ClinicResponse> page = service.findAll(pageable);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Clinica Antiga");
    }

    @Test
    @DisplayName("update: atualiza os campos da clinica existente")
    void update_adminSistema_atualizaCampos() {
        Clinic clinic = existingClinic();
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_SISTEMA);
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(clinic));
        when(repository.save(clinic)).thenReturn(clinic);
        ClinicResponse response = service.update(1L, request("Clinica Atualizada"));
        assertThat(response.getName()).isEqualTo("Clinica Atualizada");
        assertThat(clinic.getPhone()).isEqualTo("11999990001");
        verify(repository).save(clinic);
    }

    @Test
    @DisplayName("update: clinica inexistente lanca ResourceNotFoundException")
    void update_clinicaInexistente_lancaResourceNotFound() {
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_SISTEMA);
        when(repository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(99L, request("Qualquer")))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any(Clinic.class));
    }

    @Test
    @DisplayName("delete: exclusao logica desativa a clinica e os registros vinculados")
    void delete_adminSistema_fazExclusaoLogica() {
        Clinic clinic = existingClinic();
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_SISTEMA);
        when(repository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(clinic));
        service.delete(1L);
        ArgumentCaptor<Clinic> captor = ArgumentCaptor.forClass(Clinic.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getActive()).isFalse();
        verify(userAccountRepository).deactivateClinicAccounts(1L);
        verify(userAccountRepository).deactivateTutorAccountsByClinicId(1L);
        verify(userAccountRepository).deactivateVeterinarianAccountsByClinicId(1L);
        verify(petRepository).deactivateByClinicId(1L);
        verify(tutorRepository).deactivateByClinicId(1L);
        verify(veterinarianRepository).deactivateByClinicId(1L);
    }

    @Test
    @DisplayName("delete: clinica inexistente lanca ResourceNotFoundException e nao grava nada")
    void delete_clinicaInexistente_lancaResourceNotFound() {
        when(authenticatedUser.role()).thenReturn(UserRole.ADMIN_SISTEMA);
        when(repository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any(Clinic.class));
    }
}
