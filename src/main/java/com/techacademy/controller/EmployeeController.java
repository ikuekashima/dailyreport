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

@Controller
@RequestMapping("employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // 従業員一覧画面
    @GetMapping
    public String list(Model model) {
        model.addAttribute("listSize", employeeService.findAll().size());
        model.addAttribute("employeeList", employeeService.findAll());
        return "employees/list";
    }

    // 従業員詳細画面
    @GetMapping(value = "/{code}/")
    public String detail(@PathVariable("code") String code, Model model) {
        model.addAttribute("employee", employeeService.findByCode(code));
        return "employees/detail";
    }

    // 従業員更新画面表示
    @GetMapping(value = "/{code}/update")
    public String update(@PathVariable("code") String code, Model model) {
        model.addAttribute("employee", employeeService.findByCode(code));
        model.addAttribute("roles", List.of("一般", "管理者"));
        return "employees/update";
    }

    // 従業員更新処理
    @PostMapping(value = "/{code}/update")
    public String update(@PathVariable("code") String code, @Validated @ModelAttribute Employee employee, BindingResult result, Model model) {
        System.out.println("POSTリクエスト受信: code=" + code);

        if (employee.getPassword() == null || employee.getPassword().isEmpty()) {
            System.out.println("エラー: パスワードが入力されていません");
            model.addAttribute("employee", employee);
            model.addAttribute("roles", List.of("一般", "管理者"));
            return "employees/update";
        }

        if (result.hasErrors()) {
            model.addAttribute("employee", employee);
            model.addAttribute("roles", List.of("一般", "管理者"));
            return "employees/update";
        }

        try {
            ErrorKinds resultStatus = employeeService.save(employee);
            if (resultStatus != ErrorKinds.SUCCESS) {
                System.out.println("エラー: " + resultStatus);
                model.addAttribute("errorMessage", "更新処理中にエラーが発生しました");
                return "employees/update";
            }
        } catch (Exception e) {
            System.out.println("エラー: " + e.getMessage());
            model.addAttribute("errorMessage", "更新処理中に例外が発生しました");
            return "employees/update";
        }

        System.out.println("更新成功");
        return "redirect:/employees";
    }

    // 従業員削除処理
    @PostMapping(value = "/{code}/delete")
    public String delete(@PathVariable("code") String code, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        ErrorKinds result = employeeService.delete(code, userDetail);
        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("employee", employeeService.findByCode(code));
            return detail(code, model);
        }
        return "redirect:/employees";
    }

    // 新規作成画面表示
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Employee employee) {
        return "employees/new";
    }

    // 従業員新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Employee employee, BindingResult res, Model model) {
        if (employee.getPassword() == null || employee.getPassword().isEmpty()) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.BLANK_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.BLANK_ERROR));
            return create(employee);
        }

        if (res.hasErrors()) {
            return create(employee);
        }

        try {
            ErrorKinds result = employeeService.save(employee);
            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(employee);
            }
        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(employee);
        }
        return "redirect:/employees";
    }
}

