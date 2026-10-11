package vn.edu.tdtu.tdtbank.domain;

import static org.junit.Assert.assertEquals;

import vn.edu.tdtu.tdtbank.domain.ChatIntentEngine.Intent;

import org.junit.Test;

public class ChatIntentEngineTest {

    @Test
    public void detectsVietnameseWithAndWithoutDiacritics() {
        assertEquals(Intent.PAY_ELECTRICITY, ChatIntentEngine.detect("Tôi muốn thanh toán tiền điện"));
        assertEquals(Intent.PAY_ELECTRICITY, ChatIntentEngine.detect("thanh toan tien dien thang nay"));
        assertEquals(Intent.PAY_WATER, ChatIntentEngine.detect("đóng tiền nước"));
        assertEquals(Intent.PHONE_TOPUP, ChatIntentEngine.detect("nạp tiền điện thoại 100k"));
        assertEquals(Intent.MONTH_TRANSACTIONS, ChatIntentEngine.detect("Xem giao dịch tháng này"));
        assertEquals(Intent.TRANSFER, ChatIntentEngine.detect("chuyển tiền cho bạn"));
        assertEquals(Intent.NEAREST_BRANCH, ChatIntentEngine.detect("chi nhánh gần nhất ở đâu"));
        assertEquals(Intent.BALANCE, ChatIntentEngine.detect("số dư của tôi"));
        assertEquals(Intent.FLIGHT, ChatIntentEngine.detect("mua vé máy bay đi Hà Nội"));
        assertEquals(Intent.MOVIE, ChatIntentEngine.detect("đặt vé xem phim"));
        assertEquals(Intent.HOTEL, ChatIntentEngine.detect("đặt phòng khách sạn"));
    }

    @Test
    public void unknownAndNullInput() {
        assertEquals(Intent.UNKNOWN, ChatIntentEngine.detect("zzzz qqqq"));
        assertEquals(Intent.UNKNOWN, ChatIntentEngine.detect(null));
        assertEquals(Intent.UNKNOWN, ChatIntentEngine.detect(""));
    }
}
