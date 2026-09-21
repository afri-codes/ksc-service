package ksc.go.tz.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DistanceCalculatorService {

//    @Value("${google.maps.api.key}")
//    private String apiKey;
//
//    public double calculateDistanceInKm(String origin, String destination) {
//        String url = UriComponentsBuilder
//                .fromHttpUrl("https://maps.googleapis.com/maps/api/distancematrix/json")
//                .queryParam("origins", origin)
//                .queryParam("destinations", destination)
//                .queryParam("key", apiKey)
//                .toUriString();
//
//        RestTemplate restTemplate = new RestTemplate();
//        GoogleDistanceMatrixResponse response = restTemplate.getForObject(url, GoogleDistanceMatrixResponse.class);
//
//        if (response != null &&
//                !response.getRows().isEmpty() &&
//                !response.getRows().get(0).getElements().isEmpty() &&
//                "OK".equals(response.getRows().get(0).getElements().get(0).getStatus())) {
//
//            long distanceInMeters = response.getRows().get(0).getElements().get(0).getDistance().getValue();
//            return distanceInMeters / 1000.0; // Convert to km
//        }
//
//        throw new RuntimeException("Could not calculate distance from Google Maps API");
//    }
}
