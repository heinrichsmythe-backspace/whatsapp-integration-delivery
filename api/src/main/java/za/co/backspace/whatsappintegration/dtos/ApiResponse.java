package za.co.backspace.whatsappintegration.dtos;

public class ApiResponse<D> {
    private final String message;
    private final D data;

    public ApiResponse(String message, D data) {
        this.message = message;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public D getData() {
        return data;
    }

}
