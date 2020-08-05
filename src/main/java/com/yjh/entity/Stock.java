package com.yjh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @Author mua
 * @Date 2020/8/4 9:39
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
public class Stock {

    private Integer id;
    private String name;
    private Integer count;
    private Integer sale;
    private Integer version;
}
