package com.lkkkk.controller;

import com.lkkkk.controller.utils.R;
import com.lkkkk.domain.Brand;
import com.lkkkk.service.BrandService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @program: springboot
 * @description:
 * @author: LKKKK
 * @create: 2023-03-28 09:05
 **/

@RestController
@RequestMapping("/books")
public class BookController {

    @Autowired
    BrandService brandService;

    @GetMapping
    public R selectAll(){
        return new R(true,brandService.selectAll());
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable int id){
        return new R(brandService.delete(id));
    }
    @PostMapping
    public R save(@RequestBody Brand brand){

        return new R(brandService.save(brand));
    }

    @PutMapping
    public R update(@RequestBody Brand brand){
        return new R(brandService.update(brand));
    }

    @GetMapping("/{id}")
    public R selectById(@PathVariable int id){
        return new R(true,brandService.selectById(id));
    }

    @GetMapping("/{start}/{number}")
    public R selectPage(@PathVariable int start, @PathVariable int number){

        return new R(true,brandService.selectPage(start,number));
    }


}
