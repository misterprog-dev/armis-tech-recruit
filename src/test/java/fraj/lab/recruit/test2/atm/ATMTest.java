package fraj.lab.recruit.test2.atm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static fraj.lab.recruit.test2.atm.PaymentStatus.FAILURE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


public class ATMTest {
    @Mock
    private AmountSelector amountSelector;
    @Mock
    private CashManager cashManager;
    @Mock
    private PaymentProcessor paymentProcessor;
    private ATM atm;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        atm = new ATM(amountSelector, cashManager, paymentProcessor);
    }

    @Test
    void should_runCashWithDrawal_withNegativeMoneySelected() {
        // Given
        when(amountSelector.selectAmount()).thenReturn(0);

        // When
        assertThrows(ATMTechnicalException.class, () -> atm.runCashWithdrawal());

        // Then
        verify(amountSelector).selectAmount();
    }

    @Test
    void should_runCashWithDrawal_withMoneySelectedNotAvalaible() throws ATMTechnicalException {
        // Given
        int amount = Integer.MAX_VALUE;
        when(amountSelector.selectAmount()).thenReturn(amount);
        when(cashManager.canDeliver(amount)).thenReturn(false);

        // When
        ATMStatus result = atm.runCashWithdrawal();

        // Then
        assertEquals(ATMStatus.CASH_NOT_AVAILABLE, result);
        verify(amountSelector).selectAmount();
        verify(cashManager).canDeliver(amount);
    }

    @Test
    void should_runCashWithDrawal_WithPaymentError() {
        // Given
        int amount = 100;
        when(amountSelector.selectAmount()).thenReturn(amount);
        when(cashManager.canDeliver(amount)).thenReturn(true);
        when(paymentProcessor.pay(amount)).thenReturn(FAILURE);

        // When
        assertThrows(ATMTechnicalException.class, () -> atm.runCashWithdrawal());

        // Then
        verify(amountSelector).selectAmount();
        verify(cashManager).canDeliver(amount);
        verify(paymentProcessor).pay(amount);
        verify(cashManager, never()).deliver(amount);
    }

    @Test
    void should_runCashWithDrawal() throws ATMTechnicalException {
        // Given
        int amount = 100;
        when(amountSelector.selectAmount()).thenReturn(amount);
        when(cashManager.canDeliver(amount)).thenReturn(true);

        // When
        ATMStatus result = atm.runCashWithdrawal();

        // Then
        assertEquals(ATMStatus.DONE, result);
        verify(amountSelector).selectAmount();
        verify(cashManager).canDeliver(amount);
        verify(paymentProcessor).pay(amount);
        verify(cashManager).deliver(amount);
    }
}
