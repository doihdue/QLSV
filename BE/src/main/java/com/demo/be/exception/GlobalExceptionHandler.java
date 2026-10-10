package com.demo.be.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Bắt lỗi validation dữ liệu đầu vào (@Valid trên DTO / RequestBody)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<String> details = new ArrayList<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            String msg = fieldError.getDefaultMessage();
            if (msg != null && !msg.isBlank()) {
                details.add(msg);
            }
        }
        for (ObjectError globalError : exception.getBindingResult().getGlobalErrors()) {
            String msg = globalError.getDefaultMessage();
            if (msg != null && !msg.isBlank()) {
                details.add(msg);
            }
        }
        details = details.stream().distinct().collect(Collectors.toList());

        String mainMessage = details.isEmpty()
                ? "Dữ liệu nhập vào không hợp lệ. Vui lòng kiểm tra lại."
                : String.join("; ", details);

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                mainMessage,
                details
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Bắt lỗi ràng buộc tham số (@Validated ở tầng Service hoặc Controller)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        List<String> details = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .filter(msg -> msg != null && !msg.isBlank())
                .distinct()
                .collect(Collectors.toList());

        String mainMessage = details.isEmpty()
                ? "Dữ liệu tham số không hợp lệ."
                : String.join("; ", details);

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                mainMessage,
                details
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Bắt lỗi dữ liệu JSON không đọc được (sai kiểu dữ liệu số, sai định dạng ngày tháng YYYY-MM-DD...)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        String message = "Dữ liệu gửi lên không đúng định dạng.";
        Throwable rootCause = exception.getMostSpecificCause();
        if (rootCause instanceof DateTimeParseException) {
            message = "Định dạng ngày tháng năm không hợp lệ. Vui lòng nhập đúng định dạng YYYY-MM-DD (Ví dụ: 2004-10-25).";
        } else if (rootCause != null && rootCause.getMessage() != null) {
            String causeMsg = rootCause.getMessage();
            if (causeMsg.contains("LocalDate") || causeMsg.contains("date")) {
                message = "Định dạng ngày tháng năm không hợp lệ. Vui lòng nhập đúng định dạng YYYY-MM-DD.";
            } else if (causeMsg.contains("Cannot deserialize") || causeMsg.contains("NumberFormatException")) {
                message = "Kiểu dữ liệu nhập vào không hợp lệ (sai định dạng số hoặc chuỗi).";
            }
        }

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                List.of(message)
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Bắt lỗi tham số URL / Path Variable sai kiểu dữ liệu
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String paramName = exception.getName();
        String requiredType = exception.getRequiredType() != null ? exception.getRequiredType().getSimpleName() : "hợp lệ";
        String message = String.format("Tham số '%s' có giá trị không hợp lệ (yêu cầu kiểu: %s).", paramName, requiredType);

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                List.of(message)
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Bắt lỗi thiếu tham số bắt buộc trong Request (@RequestParam required = true)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(MissingServletRequestParameterException exception) {
        String message = String.format("Thiếu tham số bắt buộc: '%s'.", exception.getParameterName());

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                List.of(message)
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Bắt lỗi xung đột cơ sở dữ liệu (trùng khóa chính, trùng mã duy nhất, vi phạm khóa ngoại liên kết)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException exception) {
        String message = "Dữ liệu không hợp lệ do xung đột ràng buộc cơ sở dữ liệu.";
        String detail = exception.getMostSpecificCause() != null ? exception.getMostSpecificCause().getMessage() : exception.getMessage();

        if (detail != null) {
            String lower = detail.toLowerCase();
            if (lower.contains("duplicate") || lower.contains("unique") || lower.contains("violation of unique key") || lower.contains("trùng")) {
                if (lower.contains("mssv")) {
                    message = "Mã số sinh viên (MSSV) đã tồn tại trong hệ thống. Vui lòng chọn mã khác.";
                } else if (lower.contains("ma_giang_vien") || lower.contains("magiangvien")) {
                    message = "Mã giảng viên đã tồn tại trong hệ thống. Vui lòng chọn mã khác.";
                } else if (lower.contains("email")) {
                    message = "Địa chỉ email này đã được sử dụng. Vui lòng nhập email khác.";
                } else if (lower.contains("ma_mon_hoc") || lower.contains("mamonhoc")) {
                    message = "Mã môn học đã tồn tại trong hệ thống.";
                } else if (lower.contains("ma_lop") || lower.contains("malop")) {
                    message = "Mã lớp đã tồn tại trong hệ thống.";
                } else if (lower.contains("ma_khoa") || lower.contains("makhoa")) {
                    message = "Mã khoa đã tồn tại trong hệ thống.";
                } else {
                    message = "Dữ liệu bị trùng lặp (mã hoặc email đã tồn tại trong hệ thống).";
                }
            } else if (lower.contains("foreign key") || lower.contains("reference") || lower.contains("khóa ngoại")) {
                message = "Không thể xóa hoặc thay đổi dữ liệu do đang có dữ liệu khác liên kết (ràng buộc khóa ngoại).";
            }
        }

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                message,
                List.of(message)
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Bắt lỗi logic nghiệp vụ
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(RuntimeException exception) {
        String msg = exception.getMessage() != null && !exception.getMessage().isBlank()
                ? exception.getMessage()
                : "Yêu cầu không hợp lệ.";

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                msg,
                List.of(msg)
        );
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Bắt lỗi không có quyền truy cập (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException exception) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Bạn không có quyền thực hiện thao tác này.",
                List.of("Truy cập bị từ chối do không đủ quyền hạn.")
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Bắt lỗi xác thực tài khoản (401 Unauthorized)
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(RuntimeException exception) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Tên đăng nhập hoặc mật khẩu không chính xác.",
                List.of("Tên đăng nhập hoặc mật khẩu không chính xác.")
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Bắt lỗi không tìm thấy tài nguyên (404 Not Found)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception) {
        String msg = exception.getMessage() != null && !exception.getMessage().isBlank()
                ? exception.getMessage()
                : "Không tìm thấy dữ liệu yêu cầu.";

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                msg,
                List.of(msg)
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Bắt lỗi phương thức HTTP không được hỗ trợ (405 Method Not Allowed)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        String message = String.format("Phương thức HTTP '%s' không được hỗ trợ cho đường dẫn này.", exception.getMethod());
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),
                message,
                List.of(message)
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    /**
     * Bắt các lỗi hệ thống không xác định khác (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
        log.error("Unhandled exception: ", exception);
        String message = "Đã xảy ra lỗi hệ thống không mong muốn. Vui lòng thử lại sau.";
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                message,
                List.of(exception.getMessage() != null ? exception.getMessage() : "Lỗi máy chủ nội bộ")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
