package com.example.appthuexe;

public class Vehicle {

    private String tenXe;
    private String loaiXe;
    private String diaDiem;
    private String moTa;
    private String hinhAnh;
    private double giaThue;

    // Constructor mặc định bắt buộc cho Firebase/Firestore
    public Vehicle() {}

    // Constructor đầy đủ
    public Vehicle(String tenXe, String loaiXe, String diaDiem, String moTa, String hinhAnh, double giaThue) {
        this.tenXe = tenXe;
        this.loaiXe = loaiXe;
        this.diaDiem = diaDiem;
        this.moTa = moTa;
        this.hinhAnh = hinhAnh;
        this.giaThue = giaThue;
    }

    // Getter và Setter
    public String getTenXe() { return tenXe; }
    public void setTenXe(String tenXe) { this.tenXe = tenXe; }

    public String getLoaiXe() { return loaiXe; }
    public void setLoaiXe(String loaiXe) { this.loaiXe = loaiXe; }

    public String getDiaDiem() { return diaDiem; }
    public void setDiaDiem(String diaDiem) { this.diaDiem = diaDiem; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public double getGiaThue() { return giaThue; }
    public void setGiaThue(double giaThue) { this.giaThue = giaThue; }
}
