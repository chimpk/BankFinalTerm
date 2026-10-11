package vn.edu.tdtu.tdtbank.data.remote;

import vn.edu.tdtu.tdtbank.model.Branch;

import java.util.Arrays;
import java.util.List;

/**
 * Chi nhánh MẪU (hư cấu, tọa độ gần các khu vực TP.HCM) dùng khi collection "branches" trên Firestore còn trống.
 * UI luôn hiển thị nhãn "dữ liệu mẫu" khi dùng danh sách này.
 */
public final class SampleBranches {

    private SampleBranches() {
    }

    public static List<Branch> all() {
        return Arrays.asList(
                new Branch("sample-q7", "TDT Bank - Hội sở Quận 7", "19 Nguyễn Hữu Thọ, Tân Phong, Quận 7", "028 0000 0001", 10.7326, 106.6996),
                new Branch("sample-pmh", "TDT Bank - PGD Phú Mỹ Hưng", "Nguyễn Lương Bằng, Tân Phú, Quận 7", "028 0000 0002", 10.7290, 106.7195),
                new Branch("sample-q1", "TDT Bank - CN Quận 1", "Lê Lợi, Bến Nghé, Quận 1", "028 0000 0003", 10.7769, 106.7009),
                new Branch("sample-q4", "TDT Bank - PGD Quận 4", "Khánh Hội, Quận 4", "028 0000 0004", 10.7580, 106.7050),
                new Branch("sample-td", "TDT Bank - CN Thủ Đức", "Võ Văn Ngân, Thủ Đức", "028 0000 0005", 10.8494, 106.7537));
    }
}
