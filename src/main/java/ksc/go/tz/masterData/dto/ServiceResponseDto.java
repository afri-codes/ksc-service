package ksc.go.tz.masterData.dto;

import ksc.go.tz.masterData.entities.Service;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceResponseDto {

    private String serviceId;
    private String serviceName;
    private String status;

    public ServiceResponseDto(Service service) {
        this.serviceId = service.getId().toString();
        this.serviceName = service.getServiceName();
        this.status = service.getStatus().name();
    }
}
