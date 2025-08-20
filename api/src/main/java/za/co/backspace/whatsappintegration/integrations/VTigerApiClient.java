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
        var request = new CreateRecordRequest<CreateCaseRequestCaseDetail>("Cases", element);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(config.getVTigerUsername(), config.getVTigerAccessKey());
        HttpEntity<CreateRecordRequest<CreateCaseRequestCaseDetail>> httpEntity = new HttpEntity<>(request, headers);
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
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
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
            throw new RuntimeException("Get case not success: " + response.getStatusCode());
        }
    }

    public ContactDetail createContact() {
        var element = new CreateContactRequestContactDetail();
        var request = new CreateRecordRequest<CreateContactRequestContactDetail>("Contacts", element);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(config.getVTigerUsername(), config.getVTigerAccessKey());
        HttpEntity<CreateRecordRequest<CreateContactRequestContactDetail>> httpEntity = new HttpEntity<>(request,
                headers);
        var response = restTemplate.exchange(
                config.getVTigerBaseUrl(),
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<VTigerApiResponse<ContactDetail>>() {
                });
        var body = response.getBody();
        if (body.isSuccess()) {
            return body.getResult();
        } else {
            throw new RuntimeException("Create contact not success: " + response.getStatusCode());
        }
    }

    public ContactDetail lookupContactByMsisdn(String msisdn) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(config.getVTigerUsername(), config.getVTigerAccessKey());

        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        String url = config.getVTigerBaseUrl() + "/restapi/v1/vtiger/default/lookup?type=phone&value=" + msisdn
                + "&searchIn={\"Contacts\":[\"mobile\",\"phone\"]}";

        ResponseEntity<VTigerApiResponse<ContactDetail>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<VTigerApiResponse<ContactDetail>>() {
                });

        var body = response.getBody();
        if (body.isSuccess()) {
            return body.getResult();
        } else {
            throw new RuntimeException("Lookup contact not success: " + response.getStatusCode());
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

    public class CreateRecordRequest<T> {
        @JsonProperty("elementType")
        private String elementType;

        @JsonProperty("element")
        private T element;

        public CreateRecordRequest(String elementType, T element) {
            this.elementType = elementType;
            this.element = element;
        }

        public String getElementType() {
            return elementType;
        }

        public T getElement() {
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

    public class CreateContactRequestContactDetail {
        @JsonProperty("firstname")
        private String firstname;
        @JsonProperty("lastname")
        private String lastname;
        @JsonProperty("phone")
        private String phone;

        public String getPhone() {
            return phone;
        }

        public String getFirstname() {
            return firstname;
        }

        public String getLastname() {
            return lastname;
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

    public class ContactDetail {
        @JsonProperty("id")
        private String id;

        @JsonProperty("firstname")
        private String firstName;

        @JsonProperty("lastname")
        private String lastName;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }
}
