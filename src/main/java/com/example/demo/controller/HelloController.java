package com.example.demo.controller;

import com.example.demo.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by fupeng-ds on 2018/6/28.
 */
@RestController
public class HelloController {

    @Cacheable(key = "'hello' + #name")
    @GetMapping(value = "/hello/{name}")
    public Object hello(@PathVariable String name) {
        return "hello" + name;
    }

}
