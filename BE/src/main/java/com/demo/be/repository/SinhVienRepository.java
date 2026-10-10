package com.demo.be.repository;

import com.demo.be.model.SinhVien;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SinhVienRepository extends JpaRepository<SinhVien, Long> {
    Optional<SinhVien> findByMssv(String mssv);
    List<SinhVien> findByLop_Id(Long lopId);
    List<SinhVien> findByMssvStartingWith(String prefix);
}

