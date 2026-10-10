package com.demo.be.repository;

import com.demo.be.model.SinhVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThongKeRepository extends JpaRepository<SinhVien, Long>, ThongKeRepositoryCustom {
}
