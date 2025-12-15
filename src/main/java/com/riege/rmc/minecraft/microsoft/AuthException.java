package com.riege.rmc.minecraft.microsoft;

public class AuthException extends Exception {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class HttpException extends AuthException {
        private final int statusCode;

        public HttpException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    public static class InvalidResponseException extends AuthException {
        public InvalidResponseException(String message) {
            super(message);
        }

        public InvalidResponseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class AuthenticationFailedException extends AuthException {
        public AuthenticationFailedException(String message) {
            super(message);
        }
    }
}
