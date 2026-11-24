package ksc.go.tz.masterData.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ksc.go.tz.masterData.entities.Service;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Service details")
public class ServiceResponseDto {

    @Schema(description = "Service ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String serviceId;
    @Schema(description = "Name of the service", example = "Cleaning")
    private String serviceName;
    @Schema(description = "Fixed price in TZS per site; null when the service has no fixed charge", example = "150000.00")
    private BigDecimal price;
    @Schema(description = "Record status", allowableValues = {"ACTIVE", "INACTIVE"}, example = "ACTIVE")
    private String status;

    public ServiceResponseDto(Service service) {
        this.serviceId = service.getId().toString();
        this.serviceName = service.getServiceName();
        this.price = service.getPrice();
        this.status = service.getStatus().name();
    }
}
