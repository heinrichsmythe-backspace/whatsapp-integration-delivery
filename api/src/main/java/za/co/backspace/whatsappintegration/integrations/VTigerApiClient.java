package za.co.backspace.whatsappintegration.integrations;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.annotation.JsonProperty;
import za.co.backspace.whatsappintegration.config.WhatsAppIntegrationApplicationConfig;
import za.co.backspace.whatsappintegration.rest.RestTemplateLoggingInterceptor;
import za.co.backspace.whatsappintegration.utils.AuthUtil;

@Component
public class VTigerApiClient {

    private final WhatsAppIntegrationApplicationConfig config;

    public VTigerApiClient(WhatsAppIntegrationApplicationConfig config) {
        this.config = config;
    }

    public String makeModuleContactId(String contactId) {
        if (contactId.contains("x")) {
            return contactId;
        }
        return config.getVTigerContactsIdPrefix() + "x" + contactId;
    }

    public String makeModuleCaseId(String caseId) {
        if (caseId.contains("x")) {
            return caseId;
        }
        return config.getVTigerCasesIdPrefix() + "x" + caseId;
    }

    private RestTemplate restTemplate() {
        final RestTemplate restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        messageConverters.add(converter);
        restTemplate.setMessageConverters(messageConverters);
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateLoggingInterceptor()));
        return restTemplate;
    }

    public CaseDetail createCase(CreateCaseRequestCaseDetail newCase, String vTigerUsername, String vTigerAccessKey) {
        var request = new CreateRecordRequest<CreateCaseRequestCaseDetail>("Cases", newCase);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(vTigerUsername, vTigerAccessKey);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<CreateRecordRequest<CreateCaseRequestCaseDetail>> httpEntity = new HttpEntity<>(request, headers);
        var response = restTemplate().exchange(
                config.getVTigerBaseUrl() + "/restapi/v1/vtiger/default/create",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<VTigerApiResponse<CaseDetail>>() {
                });
        var body = response.getBody();
        if (body.isSuccess()) {
            return body.getResult();
        } else {
            throw new RuntimeException("Create case not success: " + response);
        }
    }

    public CaseDetail getCaseById(String recordId, String vTigerUsername, String vTigerAccessKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(vTigerUsername, vTigerAccessKey);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
        String url = config.getVTigerBaseUrl() + "/restapi/v1/vtiger/default/retrieve?id=" + recordId;
        ResponseEntity<VTigerApiResponse<CaseDetail>> response = restTemplate().exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<VTigerApiResponse<CaseDetail>>() {
                });
        var body = response.getBody();
        if (body.isSuccess()) {
            return body.getResult();
        } else {
            throw new RuntimeException("Get case not success: " + response);
        }
    }

    public ContactDetail createContact(CreateContactRequestContactDetail contactDetail, String vTigerUsername,
            String vTigerAccessKey) {
        var request = new CreateRecordRequest<CreateContactRequestContactDetail>("Contacts", contactDetail);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(vTigerUsername, vTigerAccessKey);
        HttpEntity<CreateRecordRequest<CreateContactRequestContactDetail>> httpEntity = new HttpEntity<>(request,
                headers);
        var response = restTemplate().exchange(
                config.getVTigerBaseUrl() + "/restapi/v1/vtiger/default/create",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<VTigerApiResponse<ContactDetail>>() {
                });
        var body = response.getBody();
        if (body.isSuccess()) {
            return body.getResult();
        } else {
            throw new RuntimeException("Create contact not success: " + response);
        }
    }

    public List<ContactDetail> lookupContactByMsisdn(String msisdn, String vTigerUsername, String vTigerAccessKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(vTigerUsername, vTigerAccessKey);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        URI uri;
        try {
            uri = new URI("https://anystack.od2.vtiger.com/restapi/v1/vtiger/default/lookup" +
                    "?type=phone&value=" + msisdn +
                    "&searchIn=%7B%22Contacts%22%3A%5B%22mobile%22%2C%22phone%22%5D%7D");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        ResponseEntity<VTigerApiResponse<List<ContactDetail>>> response = restTemplate().exchange(
                uri,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<VTigerApiResponse<List<ContactDetail>>>() {
                });

        var body = response.getBody();
        if (body.isSuccess()) {
            return body.getResult();
        } else {
            throw new RuntimeException("Lookup contact not success: " + response);
        }
    }

    public ValidUserAuth getMyUser(String vTigerUsername, String vTigerAccessKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(vTigerUsername, vTigerAccessKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        var response = restTemplate().exchange(
                config.getVTigerBaseUrl() + "/restapi/v1/vtiger/default/me",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<VTigerApiResponse<ContactDetail>>() {
                });

        var body = response.getBody();
        if (body != null && body.isSuccess()) {
            // Encrypt username:accessKey into a token
            String raw = vTigerUsername + ":" + vTigerAccessKey;
            var tokenUtil = new AuthUtil(config.getVTigerAuthInternalTokenKey());
            String token = tokenUtil.encrypt(raw);
            return new ValidUserAuth(token);
        } else {
            throw new RuntimeException("Auth not successful: " + response);
        }
    }

    public String[] decryptToken(String token) {
        var tokenUtil = new AuthUtil(config.getVTigerAuthInternalTokenKey());
        String decrypted = tokenUtil.decrypt(token);
        return decrypted.split(":");
    }

    public record ValidUserAuth(String token) {

    }

    public static class VTigerApiResponse<T> {
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

    public static class CreateRecordRequest<T> {
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

    public static class CreateCaseRequestCaseDetail {
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

        public CreateCaseRequestCaseDetail(String title, String description, String casestatus, String casepriority,
                String contactId) {
            this.title = title;
            this.description = description;
            this.casestatus = casestatus;
            this.casepriority = casepriority;
            this.contactId = contactId;
        }

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

    public static class CreateContactRequestContactDetail {
        @JsonProperty("firstname")
        private String firstname;
        @JsonProperty("lastname")
        private String lastname;
        @JsonProperty("phone")
        private String phone;

        public CreateContactRequestContactDetail(String firstname, String lastname, String phone) {
            this.firstname = firstname;
            this.lastname = lastname;
            this.phone = phone;
        }

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

    public static class CaseDetail {
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

    public static class ContactDetail {
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
