package org.example.exception;

import org.example.enums.ServiceExceptionMessages;
import org.springframework.web.server.ResponseStatusException;

public class ServiceException extends ResponseStatusException {
  public ServiceException(ServiceExceptionMessages serviceExceptionMessages) {
    super(serviceExceptionMessages.getCode(), serviceExceptionMessages.getDescription());
  }
}
