import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

public class Main {
    private static final String REGISTER_SENSOR_URL = "http://localhost:8080/sensors/registration";
    private static final String ADD_MEASUREMENT_URL = "http://localhost:8080/measurements/add";
    private static final String GET_MEASUREMENTS_URL = "http://localhost:8080/measurements";
    private static final String GET_RAINY_DAYS_URL = "http://localhost:8080/measurements/rainyDaysCount";


    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();

        Random random = new Random();
        float meanTemperature = 15;
        float standardDeviationTemperature = 5;
        double probabilityOfRain = 0.3;

        String sensorName = "VSP";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String newSensor =
                "{\n" +
                "\"name\": \"" + sensorName + "\"\n" +
                "}\n";

        HttpEntity<String> sensorPostRequestEntity = new HttpEntity<>(newSensor, headers);
        ResponseEntity<String> sensorPostResponseEntity = restTemplate.exchange(REGISTER_SENSOR_URL, HttpMethod.POST,
                sensorPostRequestEntity, String.class);
        HttpStatusCode sensorPostStatusCode = sensorPostResponseEntity.getStatusCode();
        if (sensorPostStatusCode != HttpStatus.OK) {
            System.out.println("Registering sensor failed");
            return;
        }

        for (int i=0; i<1000; i++) {
            float temperature = (float) (meanTemperature + standardDeviationTemperature * random.nextGaussian());
            boolean raining = random.nextDouble() < probabilityOfRain;

            String newMeasurement =
                    "{\n" +
                    "   \"value\": " + temperature + ",\n" +
                    "   \"raining\": " + (raining ? "true" : "false") + ",\n" +
                    "   \"sensor\": {\n" +
                    "       \"name\": \"" + sensorName +"\"\n" +
                    "   }\n" +
                    "}\n";

            HttpEntity<String> measurementPostRequestEntity = new HttpEntity<>(newMeasurement, headers);
            ResponseEntity<String> measurementPostResponseEntity = restTemplate.exchange(ADD_MEASUREMENT_URL,
                    HttpMethod.POST, measurementPostRequestEntity, String.class);
            HttpStatusCode measurementPostStatusCode = measurementPostResponseEntity.getStatusCode();
            if (measurementPostStatusCode != HttpStatus.OK) {
                System.out.println("Adding measurement " + i + " failed");
                return;
            }
        }

        HttpEntity<String> getRequestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> getResponseEntity = restTemplate.exchange(GET_RAINY_DAYS_URL, HttpMethod.GET, getRequestEntity, String.class);
        HttpStatusCode getStatusCode = getResponseEntity.getStatusCode();

        if (getStatusCode != HttpStatus.OK) {
            System.err.println("Get rainy days count request failed with status code: " + getStatusCode);
            return;
        } else {
            String responseData = getResponseEntity.getBody();
            System.out.println("Number of rainy days: " + responseData);
        }
    }
}
