package com.techacademy.controller;

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

 // 従業員更新画面
    /*@GetMapping(value = "/{code}/update")
    public String update(@PathVariable("code") String code, Model model) {
        model.addAttribute("employee", employeeService.findByCode(code)); // 該当従業員データを設定
        return "employees/update"; // 更新画面テンプレート
    }
*/


    @PostMapping(value = "/{code}/update")
    public String update(@PathVariable("code") String code, @Validated @ModelAttribute Employee employee, BindingResult res, Model model) {
        if (res.hasErrors()) {
            // エラー発生時、再度更新画面に戻す
            model.addAttribute("employee", employee);
            return "employees/update";
        }

        try {
            // 更新処理
            employeeService.save(employee);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "更新処理中にエラーが発生しました。");
            model.addAttribute("employee", employee);
            return "employees/update";
        }

        // 正常時は一覧画面にリダイレクト
        return "redirect:/employees";
    }
    // 従業員詳細画面
    @GetMapping(value = "/{code}/")
    public String detail(@PathVariable("code") String code, Model model) {
        model.addAttribute("employee", employeeService.findByCode(code));
        return "employees/detail";
    }

    // 従業員新規登録画面
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Employee employee) {
        return "employees/new";
    }

    // 従業員新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Employee employee, BindingResult res, Model model) {
        if ("".equals(employee.getPassword())) {
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

    // 新規作成画面表示用アクション
    @GetMapping(value = "/newScreen")
    public String showNewScreen(Model model) {
        model.addAttribute("someAttribute", "value");
        return "employees/newScreen"; // 新規画面のテンプレートファイル名
    }
}