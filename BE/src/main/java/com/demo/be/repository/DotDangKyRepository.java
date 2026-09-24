package com.demo.be.repository;

import com.demo.be.model.DotDangKy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DotDangKyRepository extends JpaRepository<DotDangKy, Long> {
    Optional<DotDangKy> findFirstByOrderByIdAsc();
}
