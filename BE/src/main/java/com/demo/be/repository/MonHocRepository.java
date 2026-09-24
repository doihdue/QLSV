package com.demo.be.repository;

import com.demo.be.model.MonHoc;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonHocRepository extends JpaRepository<MonHoc, Long> {
    Optional<MonHoc> findByMaMonHoc(String maMonHoc);

    List<MonHoc> findByActiveTrueOrderByMaMonHocAsc();
}
