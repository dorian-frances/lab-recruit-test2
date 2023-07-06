package fraj.lab.recruit.test2.atm;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ATMTest {

    private AmountSelector amountSelectorMock;
    private CashManager cashManagerMock;
    private PaymentProcessor paymentProcessorMock;
    private ATM atm;

    @BeforeEach
    public void initEach() {
        amountSelectorMock = mock(AmountSelector.class);
        cashManagerMock = mock(CashManager.class);
        paymentProcessorMock = mock(PaymentProcessor.class);
        atm = new ATM(amountSelectorMock, cashManagerMock, paymentProcessorMock);
    }

    final Faker faker = new Faker();

    @Test
    void should_atm_raise_exception_for_invalid_amount_selection() {
        // Given
        final int amountOfCashSelected = faker.number().numberBetween(-100, 0);

        // When
        Exception expectedException = null;
        when(amountSelectorMock.selectAmount()).thenReturn(amountOfCashSelected);
        try {
            atm.runCashWithdrawal();
        } catch (Exception exception) {
            expectedException = exception;
        }

        // Then
        assertThat(expectedException).isInstanceOf(ATMTechnicalException.class);
    }

    @Test
    void should_atm_status_equal_to_cash_not_available() throws ATMTechnicalException {
        // Given
        final int amountOfCashSelected = faker.number().numberBetween(1, 500);

        // When
        when(amountSelectorMock.selectAmount()).thenReturn(amountOfCashSelected);
        when(cashManagerMock.canDeliver(amountOfCashSelected)).thenReturn(false);

        final ATMStatus atmStatus = atm.runCashWithdrawal();

        // Then
        assertThat(atmStatus).isEqualTo(ATMStatus.CASH_NOT_AVAILABLE);
    }

    @Test
    void should_atm_status_equal_to_payment_rejected_for_error_while_delivering_cash() throws ATMTechnicalException {
        // Given
        final int amountOfCashSelected = faker.number().numberBetween(1, 500);

        // When
        when(amountSelectorMock.selectAmount()).thenReturn(amountOfCashSelected);
        when(cashManagerMock.canDeliver(amountOfCashSelected)).thenReturn(true);
        when(paymentProcessorMock.pay(amountOfCashSelected)).thenReturn(PaymentStatus.FAILURE);

        final ATMStatus atmStatus = atm.runCashWithdrawal();

        // Then
        assertThat(atmStatus).isEqualTo(ATMStatus.PAYMENT_REJECTED);
    }

    @Test
    void should_atm_status_equal_to_done() throws ATMTechnicalException {
        // Given
        final int amountOfCashSelected = faker.number().numberBetween(1, 500);

        // When
        when(amountSelectorMock.selectAmount()).thenReturn(amountOfCashSelected);
        when(cashManagerMock.canDeliver(amountOfCashSelected)).thenReturn(true);
        when(paymentProcessorMock.pay(amountOfCashSelected)).thenReturn(PaymentStatus.SUCCESS);

        final ATMStatus atmStatus = atm.runCashWithdrawal();

        // Then
        verify(cashManagerMock, times(1)).deliver(amountOfCashSelected);
        assertThat(atmStatus).isEqualTo(ATMStatus.DONE);
    }
}
