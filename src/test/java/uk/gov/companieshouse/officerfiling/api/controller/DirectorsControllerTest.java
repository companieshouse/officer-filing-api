package uk.gov.companieshouse.officerfiling.api.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.officerfiling.api.exception.NotImplementedException;

class DirectorsControllerTest {

  private static final String TRANS_ID = "117524-754816-491724";
  @Mock
  private HttpServletRequest request;
  @Mock
  private Transaction transaction;


  @Test
  void getListActiveDirectorsDetails() {

    var testController = new DirectorsController(){};

    assertThrows(NotImplementedException.class,
        () -> testController.getListActiveDirectorsDetails(TRANS_ID, transaction, request));
  }
}