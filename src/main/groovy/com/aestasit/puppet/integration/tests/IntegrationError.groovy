package com.aestasit.puppet.integration.tests

/**
 * Special exception for indicating Puppet errors.
 *
 * @author Aestas/IT
 *
 */
class IntegrationError extends RuntimeException {

  private static final long serialVersionUID = 1L

  IntegrationError() {
    super()
  }

  IntegrationError(String message, Throwable cause) {
    super(message, cause)
  }

  IntegrationError(String message) {
    super(message)
  }

  IntegrationError(Throwable cause) {
    super(cause)
  }
}
