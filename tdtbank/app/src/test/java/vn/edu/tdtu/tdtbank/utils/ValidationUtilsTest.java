package vn.edu.tdtu.tdtbank.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

public class ValidationUtilsTest {

    @Test
    public void usernames() {
        assertTrue(ValidationUtils.isValidUsername("customer1"));
        assertTrue(ValidationUtils.isValidUsername("officer"));
        assertFalse(ValidationUtils.isValidUsername("Ab"));
        assertFalse(ValidationUtils.isValidUsername("user name"));
        assertFalse(ValidationUtils.isValidUsername("user@mail.com"));
        assertFalse(ValidationUtils.isValidUsername(null));
    }

    @Test
    public void phonesNationalIdsAccounts() {
        assertTrue(ValidationUtils.isValidPhone("0912345678"));
        assertFalse(ValidationUtils.isValidPhone("912345678"));
        assertTrue(ValidationUtils.isValidNationalId("079123456789"));
        assertTrue(ValidationUtils.isValidNationalId("123456789"));
        assertFalse(ValidationUtils.isValidNationalId("12345"));
        assertTrue(ValidationUtils.isValidAccountNumber("1234567890"));
        assertFalse(ValidationUtils.isValidAccountNumber("12345678901"));
    }

    @Test
    public void passwordsAndEmails() {
        assertTrue(ValidationUtils.isValidPassword("123456"));
        assertFalse(ValidationUtils.isValidPassword("12345"));
        assertTrue(ValidationUtils.isValidOptionalEmail(""));
        assertTrue(ValidationUtils.isValidOptionalEmail("a@b.vn"));
        assertFalse(ValidationUtils.isValidOptionalEmail("a@b"));
    }

    @Test
    public void adultDateOfBirth() {
        LocalDate today = LocalDate.of(2026, 10, 9);
        assertTrue(ValidationUtils.isAdultDateOfBirth("2008-10-09", today));
        assertFalse(ValidationUtils.isAdultDateOfBirth("2008-10-10", today));
        assertFalse(ValidationUtils.isAdultDateOfBirth("2030-01-01", today));
        assertFalse(ValidationUtils.isAdultDateOfBirth("09/10/2000", today));
    }

    @Test
    public void textNormalizer() {
        assertEquals("thanh toan tien dien", TextNormalizer.normalize("Thanh toán TIỀN điện!"));
        assertEquals("duong di bo", TextNormalizer.normalize("Đường đi bộ"));
        assertEquals("", TextNormalizer.normalize(null));
    }
}
