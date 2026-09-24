package com.demo.be.repository;

import com.demo.be.model.Lop;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LopRepository extends JpaRepository<Lop, Long> {
    Optional<Lop> findByMaLop(String maLop);
}
