package vn.edu.tdtu.tdtbank.model;

import com.google.firebase.firestore.DocumentId;

/** Chi nhánh/phòng giao dịch (branches/{id}). */
public class Branch {
    @DocumentId
    public String id;
    public String name;
    public String address;
    public String phone;
    public double lat;
    public double lng;

    public Branch() {
    }

    public Branch(String id, String name, String address, String phone, double lat, double lng) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.lat = lat;
        this.lng = lng;
    }
}
