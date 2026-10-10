package com.demo.be.service;

import java.io.File;

public interface JasperReportService {
    File generateDrlReport(Long dotId, Long lopId, String outputPath) throws Exception;
    File generateGpaReport(Long lopId, String outputPath) throws Exception;
}
