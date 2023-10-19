package com.lkkkk.service;

import com.lkkkk.domain.Brand;

import java.util.List;

public interface BrandService {

    public Boolean save(Brand brand);
    public Boolean delete(Integer id);
    public Boolean update(Brand brand);
    public Brand selectById(Integer id);
    public List<Brand> selectAll();
    public List<Brand> selectPage(int start,int number);
    public List<Brand> selectByCondition(Brand brand);



}
