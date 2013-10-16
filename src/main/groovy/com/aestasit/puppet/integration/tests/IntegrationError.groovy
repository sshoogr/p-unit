/*
 * Copyright (C) 2011-2013 Aestas/IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
