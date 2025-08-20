package za.co.backspace.whatsappintegration.integrations;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.annotation.JsonProperty;
import za.co.backspace.whatsappintegration.config.WhatsAppIntegrationApplicationConfig;

@Component
public class VTigerApiClient {

    private final RestTemplate restTemplate;
    private final WhatsAppIntegrationApplicationConfig config;

    public VTigerApiClient(WhatsAppIntegrationApplicationConfig config) {
        this.restTemplate = new RestTemplate();
        this.config = config;
    }

    public CaseDetail createCase() {
        var element = new CreateCaseRequestCaseDetail();
        CreateCaseRequest request = new CreateCaseRequest("Cases", element);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(config.getVTigerUsername(), config.getVTigerAccessKey());

        HttpEntity<CreateCaseRequest> httpEntity = new HttpEntity<>(request, headers);

        var response = restTemplate.exchange(
                config.getVTigerBaseUrl(),
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<VTigerApiResponse<CaseDetail>>() {
                });
        var body = response.getBody();
        if (body.isSuccess()) {
            return body.getResult();
        } else {
            throw new RuntimeException("Create case not success: " + response.getStatusCode());
        }
    }

    public CaseDetail getCaseById(String recordId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(config.getVTigerUsername(), config.getVTigerAccessKey());

        HttpEntity<CreateCaseRequest> httpEntity = new HttpEntity<>(headers);

        String url = config.getVTigerBaseUrl() + "/restapi/v1/vtiger/default/retrieve?id=" + recordId;

        ResponseEntity<VTigerApiResponse<CaseDetail>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<VTigerApiResponse<CaseDetail>>() {
                });

        var body = response.getBody();
        if (body.isSuccess()) {
            return body.getResult();
        } else {
            throw new RuntimeException("Create case not success: " + response.getStatusCode());
        }
    }

    public class VTigerApiResponse<T> {
        @JsonProperty("success")
        private boolean success;

        @JsonProperty("result")
        private T result;

        public boolean isSuccess() {
            return success;
        }

        public T getResult() {
            return result;
        }

    }

    public class CreateCaseRequest {
        @JsonProperty("elementType")
        private String elementType;

        @JsonProperty("element")
        private CreateCaseRequestCaseDetail element;

        public CreateCaseRequest(String elementType, CreateCaseRequestCaseDetail element) {
            this.elementType = elementType;
            this.element = element;
        }

        public String getElementType() {
            return elementType;
        }

        public CreateCaseRequestCaseDetail getElement() {
            return element;
        }
    }

    public class CreateCaseRequestCaseDetail {
        @JsonProperty("title")
        private String title;

        @JsonProperty("description")
        private String description;

        @JsonProperty("casestatus")
        private String casestatus;

        @JsonProperty("casepriority")
        private String casepriority;

        @JsonProperty("contact_id")
        private String contactId;

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getCasestatus() {
            return casestatus;
        }

        public String getCasepriority() {
            return casepriority;
        }

        public String getContactId() {
            return contactId;
        }

    }

    public class CaseDetail {
        @JsonProperty("id")
        private String id;

        @JsonProperty("case_no")
        private String caseNo;

        public String getId() {
            return id;
        }

        public String getCaseNo() {
            return caseNo;
        }

    }
}
