package com.demo.be.repository;

import com.demo.be.model.GiangVien;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GiangVienRepository extends JpaRepository<GiangVien, Long> {
    Optional<GiangVien> findByMaGiangVien(String maGiangVien);
}
