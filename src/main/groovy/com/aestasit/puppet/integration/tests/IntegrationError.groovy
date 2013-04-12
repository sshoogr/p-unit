package com.aestasit.puppet.integration.tests

public class IntegrationError extends RuntimeException {

  private static final long serialVersionUID = 1L

  public IntegrationError() {
    super()
  }

  public IntegrationError(String message, Throwable cause) {
    super(message, cause)
  }

  public IntegrationError(String message) {
    super(message)
  }

  public IntegrationError(Throwable cause) {
    super(cause)
  }

}
