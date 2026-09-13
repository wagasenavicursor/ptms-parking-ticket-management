package com.ptms.controller; import com.ptms.dto.*; import com.ptms.service.*; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/dashboard") public class DashboardController{private final DashboardService s;public DashboardController(DashboardService s){this.s=s;}@GetMapping public DashboardResponse summary(){return s.summary();}}
