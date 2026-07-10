package org.openmrs.module.mohbilling.irembo.util;


import java.util.ArrayList;
import java.util.List;

public class IremboPayResponse<T> {
    public String message;
    public boolean success;
    public T data;
    public List<Error> errors = new ArrayList<>();

    public List<Error> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return "IremboPayResponse{" +
                "message='" + message + '\'' +
                ", success=" + success +
                ", data=" + data +
                ", errors=" + errors +
                '}';
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    // Constructor
    public IremboPayResponse(String message, boolean success, T data) {
        this.message = message;
        this.success = success;
        this.data = data;
    }

    // Optional: Default constructor
    public IremboPayResponse() {
    }

    // Optional: Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
    public  void  addError(Error error) {
        errors.add(error);
    }

   public static class Error{
       @Override
       public String toString() {
           return "Error{" +
                   "code='" + code + '\'' +
                   ", detail='" + detail + '\'' +
                   '}';
       }

       public String code;
        public String detail;

        public Error(String code, String detail) {
            this.code = code;
            this.detail = detail;
        }
    }
}