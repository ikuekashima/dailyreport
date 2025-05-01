package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.repository.EmployeeRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 従業員保存
    @Transactional
    public ErrorKinds save(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee オブジェクトが null です");
        }

        // パスワードチェック
        ErrorKinds result = employeePasswordCheck(employee);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        // 従業員番号重複チェック
        if (findByCode(employee.getCode()) != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }

        employee.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);

        // **パスワード暗号化**
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        employeeRepository.save(employee);
        return ErrorKinds.SUCCESS;
    }

    // 従業員削除
    @Transactional
    public ErrorKinds delete(String code, UserDetail userDetail) {
        Employee emp = findByCode(code);
        if (emp == null) {
            return ErrorKinds.BLANK_ERROR;
        }

        LocalDateTime now = LocalDateTime.now();
        emp.setUpdatedAt(now);
        emp.setDeleteFlg(true);

        employeeRepository.save(emp);
        return ErrorKinds.SUCCESS;
    }

    // 従業員一覧取得
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    // 1件取得
    public Employee findByCode(String code) {
        Optional<Employee> option = employeeRepository.findById(code);
        return option.orElse(null);
    }

    // パスワードチェック
    private ErrorKinds employeePasswordCheck(Employee employee) {
        if (employee.getPassword() == null || employee.getPassword().isEmpty()) {
            System.out.println("パスワードが入力されていません");
            return ErrorKinds.BLANK_ERROR;
        }

        if (isHalfSizeCheckError(employee.getPassword())) {
            return ErrorKinds.HALFSIZE_ERROR;
        }

        if (isOutOfRangePassword(employee.getPassword())) {
            return ErrorKinds.RANGECHECK_ERROR;
        }

        return ErrorKinds.CHECK_OK;
    }

    // 半角英数字チェック
    private boolean isHalfSizeCheckError(String password) {
        if (password == null || password.isEmpty()) {
            return true;
        }

        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
        Matcher matcher = pattern.matcher(password);
        return !matcher.matches();
    }

    // 文字数チェック（8文字～16文字）
    private boolean isOutOfRangePassword(String password) {
        if (password == null || password.isEmpty()) {
            return true;
        }

        int passwordLength = password.length();
        return passwordLength < 8 || passwordLength > 16;
    }
}

