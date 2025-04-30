package com.techacademy.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.UserDetail;

import jakarta.validation.constraints.Pattern;

@Controller
@RequestMapping("employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    private ErrorKinds employeePasswordCheck(Employee employee) {
        // パスワードがnullまたは空の場合、エラーを返す
        if (employee.getPassword() == null || employee.getPassword().isEmpty()) {
            System.out.println("パスワードが入力されていません"); // デバッグ用ログ
            return ErrorKinds.BLANK_ERROR;
        }

        // 半角英数字チェック
        if (isHalfSizeCheckError(employee)) {
            return ErrorKinds.HALFSIZE_ERROR;
        }

        // 文字数チェック
        if (isOutOfRangePassword(employee)) {
            return ErrorKinds.RANGECHECK_ERROR;
        }

        Object passwordEncoder;
        // パスワードを暗号化

        return ErrorKinds.CHECK_OK;
    }

    private boolean isHalfSizeCheckError(Employee employee) {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

    private boolean isOutOfRangePassword(Employee employee) {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }
    // 従業員一覧画面
    @GetMapping
    public String list(Model model) {
        model.addAttribute("listSize", employeeService.findAll().size());
        model.addAttribute("employeeList", employeeService.findAll());
        return "employees/list";
    }
    @GetMapping(value = "/{code}/")
    public String detail(@PathVariable("code") String code, Model model) {
        // 該当する従業員データを取得
        model.addAttribute("employee", employeeService.findByCode(code));
        return "employees/detail"; // 適切なテンプレート名を返す
    }
    @GetMapping(value = "/{code}/update")
    public String update(@PathVariable("code") String code, Model model) {
        // 該当する従業員データを取得
        model.addAttribute("employee", employeeService.findByCode(code));
        // 必要な権限リストを設定
        model.addAttribute("roles", List.of("一般", "管理者"));
        return "employees/update"; // 更新画面テンプレート
    }
    @PostMapping(value = "/{code}/update")
    public String update(@PathVariable("code") String code, @Validated @ModelAttribute Employee employee, BindingResult result, Model model) {
        System.out.println("POSTリクエスト受信: code=" + code);

        if (result.hasErrors()) {
            model.addAttribute("employee", employee);
            model.addAttribute("roles", List.of("一般", "管理者")); // 必要な権限リスト
            return "employees/update"; // エラー時に更新画面を再表示
        }

        // 更新処理
        employeeService.save(employee);
        return "redirect:/employees"; // 正常時は一覧画面にリダイレクト
    }


}
