package br.com.fiap.petjourney.models;

import br.com.fiap.petjourney.dtos.request.ClinicRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_CLINICA")
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String cnpj;

    private String phone;

    private String email;

    private String address;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    public Clinic(ClinicRequest request) {
        this.name = request.name();
        this.cnpj = request.cnpj();
        this.phone = request.phone();
        this.email = request.email();
        this.address = request.address();
        this.active = true;
    }

    public void updateFrom(ClinicRequest request) {
        this.name = request.name();
        this.cnpj = request.cnpj();
        this.phone = request.phone();
        this.email = request.email();
        this.address = request.address();
    }
}

