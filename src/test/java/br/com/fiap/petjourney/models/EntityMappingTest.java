package br.com.fiap.petjourney.models;

import br.com.fiap.petjourney.dtos.request.ClinicRequest;
import br.com.fiap.petjourney.dtos.request.VeterinarianRequest;
import br.com.fiap.petjourney.dtos.response.ClinicResponse;
import br.com.fiap.petjourney.dtos.response.VeterinarianResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntityMappingTest {

    private final ClinicRequest clinicRequest = new ClinicRequest(
            "Clinica Cloud", "11222333000181", "11999990001", "cloud@petjourney.com", "Avenida Paulista 1000");

    @Test
    @DisplayName("Clinic criada a partir do request nasce ativa e com os dados informados")
    void clinic_criadaDoRequest_nasceAtiva() {
        Clinic clinic = new Clinic(clinicRequest);
        assertThat(clinic.getActive()).isTrue();
        assertThat(clinic.getName()).isEqualTo("Clinica Cloud");
        assertThat(clinic.getCnpj()).isEqualTo("11222333000181");
    }

    @Test
    @DisplayName("Clinic.updateFrom altera os dados mas preserva o status ativo")
    void clinic_updateFrom_preservaStatus() {
        Clinic clinic = new Clinic(clinicRequest);
        ClinicRequest novo = new ClinicRequest(
                "Clinica Nova", "11222333000181", "11988880002", "nova@petjourney.com", "Rua Nova 10");
        clinic.updateFrom(novo);
        assertThat(clinic.getName()).isEqualTo("Clinica Nova");
        assertThat(clinic.getAddress()).isEqualTo("Rua Nova 10");
        assertThat(clinic.getActive()).isTrue();
    }

    @Test
    @DisplayName("ClinicResponse.fromEntity copia os campos da entidade")
    void clinicResponse_fromEntity_copiaCampos() {
        Clinic clinic = new Clinic(clinicRequest);
        clinic.setId(7L);
        ClinicResponse response = ClinicResponse.fromEntity(clinic);
        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getEmail()).isEqualTo("cloud@petjourney.com");
    }

    @Test
    @DisplayName("Veterinarian criado a partir do request fica vinculado a clinica e ativo")
    void veterinarian_criadoDoRequest_vinculadoAClinica() {
        Clinic clinic = new Clinic(clinicRequest);
        clinic.setId(1L);
        VeterinarianRequest request = new VeterinarianRequest(
                "Dr Lucas", "SP-90002", "11977770002", null, "Cardiologia Veterinaria", 1L);
        Veterinarian vet = new Veterinarian(request, clinic);
        assertThat(vet.getActive()).isTrue();
        assertThat(vet.getClinic()).isSameAs(clinic);
        assertThat(vet.getCrmv()).isEqualTo("SP-90002");
    }

    @Test
    @DisplayName("VeterinarianResponse.fromEntity expoe o nome da clinica vinculada")
    void veterinarianResponse_fromEntity_expoeNomeDaClinica() {
        Clinic clinic = new Clinic(clinicRequest);
        Veterinarian vet = Veterinarian.builder().id(3L).name("Dr Lucas").crmv("SP-90002").clinic(clinic).build();
        VeterinarianResponse response = VeterinarianResponse.fromEntity(vet);
        assertThat(response.getClinicName()).isEqualTo("Clinica Cloud");
    }

    @Test
    @DisplayName("VeterinarianResponse.fromEntity aceita veterinario sem clinica")
    void veterinarianResponse_semClinica_nomeDaClinicaNulo() {
        Veterinarian vet = Veterinarian.builder().id(4L).name("Dr Sem Clinica").build();
        VeterinarianResponse response = VeterinarianResponse.fromEntity(vet);
        assertThat(response.getClinicName()).isNull();
    }
}
